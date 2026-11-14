package com.budgeteer.app.data.workflow

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import com.budgeteer.app.data.repository.CategoryRepositoryImpl
import com.budgeteer.app.data.repository.FinancialTransactionRunnerImpl
import com.budgeteer.app.data.repository.MonthlyBudgetRepositoryImpl
import com.budgeteer.app.data.repository.SavingsTransferRepositoryImpl
import com.budgeteer.app.data.repository.TransactionRepositoryImpl
import com.budgeteer.app.data.repository.UserSettingsRepositoryImpl
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.usecase.CloseMonthlyPeriodUseCase
import com.budgeteer.app.domain.usecase.CreateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.DeleteTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ProcessPendingMonthlyPeriodsUseCase
import com.budgeteer.app.domain.usecase.RecalculateClosedMonthUseCase
import com.budgeteer.app.domain.usecase.UpdateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ValidateTransactionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class RoomWorkflowIntegrationTest {

    private lateinit var db: BudgeteerDatabase
    private lateinit var categoryRepo: CategoryRepositoryImpl
    private lateinit var budgetRepo: MonthlyBudgetRepositoryImpl
    private lateinit var txRepo: TransactionRepositoryImpl
    private lateinit var savingsRepo: SavingsTransferRepositoryImpl
    private lateinit var settingsRepo: UserSettingsRepositoryImpl
    private lateinit var transactionRunner: FinancialTransactionRunnerImpl

    private lateinit var recalculateClosedMonthUseCase: RecalculateClosedMonthUseCase
    private lateinit var closeMonthlyPeriodUseCase: CloseMonthlyPeriodUseCase
    private lateinit var createTransactionWorkflowUseCase: CreateTransactionWorkflowUseCase
    private lateinit var updateTransactionWorkflowUseCase: UpdateTransactionWorkflowUseCase
    private lateinit var deleteTransactionWorkflowUseCase: DeleteTransactionWorkflowUseCase

    private val fixedClock = Clock.fixed(Instant.parse("2026-03-15T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BudgeteerDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        transactionRunner = FinancialTransactionRunnerImpl(db)
        categoryRepo = CategoryRepositoryImpl(db.categoryDao())
        budgetRepo = MonthlyBudgetRepositoryImpl(db.monthlyBudgetDao())
        txRepo = TransactionRepositoryImpl(db.transactionDao())
        savingsRepo = SavingsTransferRepositoryImpl(db.savingsTransferDao())
        settingsRepo = UserSettingsRepositoryImpl(db.userSettingsDao())

        recalculateClosedMonthUseCase = RecalculateClosedMonthUseCase(budgetRepo, txRepo, savingsRepo)
        closeMonthlyPeriodUseCase = CloseMonthlyPeriodUseCase(budgetRepo, txRepo, savingsRepo, settingsRepo)

        val validateTx = ValidateTransactionUseCase()
        createTransactionWorkflowUseCase = CreateTransactionWorkflowUseCase(validateTx, txRepo, budgetRepo, recalculateClosedMonthUseCase, transactionRunner, ZoneOffset.UTC)
        updateTransactionWorkflowUseCase = UpdateTransactionWorkflowUseCase(validateTx, txRepo, budgetRepo, recalculateClosedMonthUseCase, transactionRunner, ZoneOffset.UTC)
        deleteTransactionWorkflowUseCase = DeleteTransactionWorkflowUseCase(txRepo, budgetRepo, recalculateClosedMonthUseCase, transactionRunner)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRoomTransactionRollbackOnFailure() = runBlocking {
        val catId = categoryRepo.insertCategory(Category(name = "Test", type = CategoryType.INCOME, iconIdentifier = "ic", colorHex = "#0"))

        try {
            transactionRunner.runInTransaction {
                txRepo.insertTransaction(
                    Transaction(
                        enteredAmount = Money.eur(BigDecimal("500.00")),
                        type = TransactionType.INCOME,
                        baseValueEur = Money.eur(BigDecimal("500.00")),
                        exchangeRate = BigDecimal.ONE,
                        description = "Rollback test",
                        categoryId = catId,
                        timestamp = Instant.now(),
                        period = YearMonth.of(2026, 1)
                    )
                )
                // Force failure inside transaction
                throw RuntimeException("Simulated Transaction Failure")
            }
        } catch (e: RuntimeException) {
            assertEquals("Simulated Transaction Failure", e.message)
        }

        // Verify that inserted transaction was completely rolled back
        val txs = txRepo.getTransactionsByMonth(YearMonth.of(2026, 1))
        assertEquals(0, txs.size)
    }

    @Test
    fun testCatchUpWorkflowRoomIntegration() = runBlocking {
        val jan = YearMonth.of(2026, 1)
        val feb = YearMonth.of(2026, 2)

        budgetRepo.saveMonthlyBudget(MonthlyBudget(period = jan, baseBudgetEur = Money.eur(BigDecimal("2000.00"))))

        val processPending = ProcessPendingMonthlyPeriodsUseCase(
            closeMonthlyPeriodUseCase, budgetRepo, txRepo, transactionRunner, fixedClock, ZoneOffset.UTC
        )

        val processedCount = processPending()
        assertEquals(2, processedCount) // Jan and Feb processed and closed in Room

        val janBudget = budgetRepo.getMonthlyBudget(jan)
        val febBudget = budgetRepo.getMonthlyBudget(feb)

        assertNotNull(janBudget)
        assertNotNull(febBudget)
        assertTrue(janBudget!!.isClosed)
        assertTrue(febBudget!!.isClosed)

        val janTransfer = savingsRepo.getSavingsTransfer(jan)
        val febTransfer = savingsRepo.getSavingsTransfer(feb)
        assertNotNull(janTransfer)
        assertNotNull(febTransfer)
        assertEquals(Money.eur(BigDecimal("0.00")), janTransfer!!.savedAmountEur)
        assertEquals(Money.eur(BigDecimal("0.00")), febTransfer!!.savedAmountEur)
    }

    @Test
    fun testTransactionMoveBetweenClosedMonthsRecalculatesBoth() = runBlocking {
        val jan = YearMonth.of(2026, 1)
        val feb = YearMonth.of(2026, 2)

        closeMonthlyPeriodUseCase(jan, Instant.now())
        closeMonthlyPeriodUseCase(feb, Instant.now())

        val catIncome = Category(name = "Salary", type = CategoryType.INCOME, iconIdentifier = "ic", colorHex = "#0")
        val catId = categoryRepo.insertCategory(catIncome)
        val savedCategory = catIncome.copy(id = catId)

        // Create 1000 EUR income in Jan
        val txJan = Transaction(
            enteredAmount = Money.eur(BigDecimal("1000.00")),
            type = TransactionType.INCOME,
            baseValueEur = Money.eur(BigDecimal("1000.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Jan Income",
            categoryId = catId,
            timestamp = Instant.parse("2026-01-10T10:00:00Z"),
            period = jan
        )
        val txId = createTransactionWorkflowUseCase(txJan, savedCategory)

        assertEquals(BigDecimal("1000.00"), savingsRepo.getSavingsTransfer(jan)!!.savedAmountEur.amount)
        assertEquals(BigDecimal("0.00"), savingsRepo.getSavingsTransfer(feb)!!.savedAmountEur.amount)

        // Move transaction to Feb
        val txFeb = txJan.copy(id = txId, period = feb, timestamp = Instant.parse("2026-02-10T10:00:00Z"))
        updateTransactionWorkflowUseCase(txFeb, savedCategory)

        // Jan recalculated to 0.00, Feb recalculated to 1000.00
        assertEquals(BigDecimal("0.00"), savingsRepo.getSavingsTransfer(jan)!!.savedAmountEur.amount)
        assertEquals(BigDecimal("1000.00"), savingsRepo.getSavingsTransfer(feb)!!.savedAmountEur.amount)
    }
}
