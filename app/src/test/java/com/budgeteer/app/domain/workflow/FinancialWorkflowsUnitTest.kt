package com.budgeteer.app.domain.workflow

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.domain.usecase.CloseMonthlyPeriodUseCase
import com.budgeteer.app.domain.usecase.CreateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.DeleteTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ProcessPendingMonthlyPeriodsUseCase
import com.budgeteer.app.domain.usecase.RecalculateClosedMonthUseCase
import com.budgeteer.app.domain.usecase.UpdateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ValidateTransactionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class FinancialWorkflowsUnitTest {

    private val directRunner = object : FinancialTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }

    private class FakeMonthlyBudgetRepository : MonthlyBudgetRepository {
        val budgets = mutableMapOf<YearMonth, MonthlyBudget>()

        override fun observeMonthlyBudget(period: YearMonth): Flow<MonthlyBudget?> = flowOf(budgets[period])
        override suspend fun getMonthlyBudget(period: YearMonth): MonthlyBudget? = budgets[period]
        override suspend fun getMostRecentMonthlyBudget(): MonthlyBudget? = budgets.values.maxByOrNull { it.period }
        override suspend fun getEarliestMonthlyBudget(): MonthlyBudget? = budgets.values.minByOrNull { it.period }
        override suspend fun saveMonthlyBudget(monthlyBudget: MonthlyBudget): Long {
            budgets[monthlyBudget.period] = monthlyBudget
            return 1L
        }
        override suspend fun updateMonthlyBudget(monthlyBudget: MonthlyBudget) {
            budgets[monthlyBudget.period] = monthlyBudget
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        val transactions = mutableMapOf<Long, Transaction>()
        private var nextId = 1L

        override fun observeAllTransactions(): Flow<List<Transaction>> = flowOf(transactions.values.toList())
        override fun observeTransactionsByMonth(period: YearMonth): Flow<List<Transaction>> = flowOf(transactions.values.filter { it.period == period })
        override suspend fun getTransactionsByMonth(period: YearMonth): List<Transaction> = transactions.values.filter { it.period == period }
        override suspend fun getTransactionById(id: Long): Transaction? = transactions[id]
        override suspend fun getEarliestTransaction(): Transaction? = transactions.values.minByOrNull { it.timestamp }
        override suspend fun insertTransaction(transaction: Transaction): Long {
            val id = if (transaction.id == 0L) nextId++ else transaction.id
            val saved = transaction.copy(id = id)
            transactions[id] = saved
            return id
        }
        override suspend fun updateTransaction(transaction: Transaction) {
            transactions[transaction.id] = transaction
        }
        override suspend fun deleteTransaction(id: Long) {
            transactions.remove(id)
        }
    }

    private class FakeSavingsTransferRepository : SavingsTransferRepository {
        val transfers = mutableMapOf<YearMonth, SavingsTransfer>()

        override fun observeSavingsTransfer(period: YearMonth): Flow<SavingsTransfer?> = flowOf(transfers[period])
        override suspend fun getSavingsTransfer(period: YearMonth): SavingsTransfer? = transfers[period]
        override fun observeAllSavingsTransfers(): Flow<List<SavingsTransfer>> = flowOf(transfers.values.toList())
        override fun observeTotalSavingsBalanceEur(): Flow<Money> {
            val total = transfers.values.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.savedAmountEur.amount) }
            return flowOf(Money.eur(total))
        }
        override suspend fun getTotalSavingsBalanceEur(): Money {
            val total = transfers.values.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.savedAmountEur.amount) }
            return Money.eur(total)
        }
        override suspend fun saveSavingsTransfer(transfer: SavingsTransfer): Long {
            transfers[transfer.period] = transfer
            return 1L
        }
    }

    private class FakeUserSettingsRepository : UserSettingsRepository {
        var settings = UserSettings()
        override fun observeUserSettings(): Flow<UserSettings> = flowOf(settings)
        override suspend fun getUserSettings(): UserSettings = settings
        override suspend fun saveUserSettings(settings: UserSettings) { this.settings = settings }
    }

    private lateinit var budgetRepo: FakeMonthlyBudgetRepository
    private lateinit var txRepo: FakeTransactionRepository
    private lateinit var savingsRepo: FakeSavingsTransferRepository
    private lateinit var settingsRepo: FakeUserSettingsRepository

    private lateinit var recalculateClosedMonthUseCase: RecalculateClosedMonthUseCase
    private lateinit var closeMonthlyPeriodUseCase: CloseMonthlyPeriodUseCase
    private lateinit var createTransactionWorkflowUseCase: CreateTransactionWorkflowUseCase
    private lateinit var updateTransactionWorkflowUseCase: UpdateTransactionWorkflowUseCase
    private lateinit var deleteTransactionWorkflowUseCase: DeleteTransactionWorkflowUseCase

    private val fixedClock = Clock.fixed(Instant.parse("2026-03-15T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setup() {
        budgetRepo = FakeMonthlyBudgetRepository()
        txRepo = FakeTransactionRepository()
        savingsRepo = FakeSavingsTransferRepository()
        settingsRepo = FakeUserSettingsRepository()

        recalculateClosedMonthUseCase = RecalculateClosedMonthUseCase(budgetRepo, txRepo, savingsRepo)
        closeMonthlyPeriodUseCase = CloseMonthlyPeriodUseCase(budgetRepo, txRepo, savingsRepo, settingsRepo)

        val validateTx = ValidateTransactionUseCase()
        createTransactionWorkflowUseCase = CreateTransactionWorkflowUseCase(validateTx, txRepo, budgetRepo, recalculateClosedMonthUseCase, directRunner, ZoneOffset.UTC)
        updateTransactionWorkflowUseCase = UpdateTransactionWorkflowUseCase(validateTx, txRepo, budgetRepo, recalculateClosedMonthUseCase, directRunner, ZoneOffset.UTC)
        deleteTransactionWorkflowUseCase = DeleteTransactionWorkflowUseCase(txRepo, budgetRepo, recalculateClosedMonthUseCase, directRunner)
    }

    @Test
    fun testCloseMonthlyPeriodUseCaseCreatesTransferAndClosesBudget() = runBlocking {
        val period = YearMonth.of(2026, 1)
        budgetRepo.saveMonthlyBudget(MonthlyBudget(period = period, baseBudgetEur = Money.eur(BigDecimal("2000.00"))))

        txRepo.insertTransaction(Transaction(enteredAmount = Money.eur(BigDecimal("1000.00")), type = TransactionType.INCOME, baseValueEur = Money.eur(BigDecimal("1000.00")), exchangeRate = BigDecimal.ONE, description = "Pay", categoryId = 1L, timestamp = Instant.parse("2026-01-10T10:00:00Z"), period = period))
        txRepo.insertTransaction(Transaction(enteredAmount = Money.eur(BigDecimal("400.00")), type = TransactionType.EXPENSE, baseValueEur = Money.eur(BigDecimal("400.00")), exchangeRate = BigDecimal.ONE, description = "Food", categoryId = 2L, timestamp = Instant.parse("2026-01-15T10:00:00Z"), period = period))

        closeMonthlyPeriodUseCase(period, Instant.now())

        val closedBudget = budgetRepo.getMonthlyBudget(period)
        assertNotNull(closedBudget)
        assertTrue(closedBudget!!.isClosed)

        val transfer = savingsRepo.getSavingsTransfer(period)
        assertNotNull(transfer)
        assertEquals(BigDecimal("600.00"), transfer!!.savedAmountEur.amount) // 1000 - 400 = 600
    }

    @Test
    fun testProcessPendingMonthlyPeriodsSequentialCatchUp() = runBlocking {
        // Clock is March 2026. Last completed month is Feb 2026.
        // Seed earliest budget in Jan 2026
        val jan = YearMonth.of(2026, 1)
        val feb = YearMonth.of(2026, 2)
        budgetRepo.saveMonthlyBudget(MonthlyBudget(period = jan, baseBudgetEur = Money.eur(BigDecimal("2000.00"))))

        val processPending = ProcessPendingMonthlyPeriodsUseCase(
            closeMonthlyPeriodUseCase, budgetRepo, txRepo, directRunner, fixedClock, ZoneOffset.UTC
        )

        val processedCount = processPending()
        assertEquals(2, processedCount) // Jan and Feb processed

        assertTrue(budgetRepo.getMonthlyBudget(jan)!!.isClosed)
        assertTrue(budgetRepo.getMonthlyBudget(feb)!!.isClosed)
        // Feb inherited Jan's 2000.00 base budget
        assertEquals(BigDecimal("2000.00"), budgetRepo.getMonthlyBudget(feb)!!.baseBudgetEur.amount)
    }

    @Test
    fun testTransactionMutationInClosedMonthTriggersRecalculation() = runBlocking {
        val period = YearMonth.of(2026, 1)
        closeMonthlyPeriodUseCase(period, Instant.now())
        assertEquals(BigDecimal("0.00"), savingsRepo.getSavingsTransfer(period)!!.savedAmountEur.amount)

        val catIncome = Category(1L, "Salary", CategoryType.INCOME, "ic", "#0")
        val tx = Transaction(enteredAmount = Money.eur(BigDecimal("1500.00")), type = TransactionType.INCOME, baseValueEur = Money.eur(BigDecimal("1500.00")), exchangeRate = BigDecimal.ONE, description = "Bonus", categoryId = 1L, timestamp = Instant.parse("2026-01-20T10:00:00Z"), period = period)

        val txId = createTransactionWorkflowUseCase(tx, catIncome)
        assertTrue(txId > 0)

        // Transfer recalculated to +1500.00
        assertEquals(BigDecimal("1500.00"), savingsRepo.getSavingsTransfer(period)!!.savedAmountEur.amount)

        // Delete transaction
        deleteTransactionWorkflowUseCase(txId)
        // Transfer recalculated back to 0.00
        assertEquals(BigDecimal("0.00"), savingsRepo.getSavingsTransfer(period)!!.savedAmountEur.amount)
    }
}
