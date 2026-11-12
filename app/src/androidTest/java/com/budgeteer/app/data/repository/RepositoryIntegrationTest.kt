package com.budgeteer.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.UserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {

    private lateinit var db: BudgeteerDatabase
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var currencyRateRepository: CurrencyRateRepositoryImpl
    private lateinit var monthlyBudgetRepository: MonthlyBudgetRepositoryImpl
    private lateinit var savingsTransferRepository: SavingsTransferRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var userSettingsRepository: UserSettingsRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BudgeteerDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        categoryRepository = CategoryRepositoryImpl(db.categoryDao())
        currencyRateRepository = CurrencyRateRepositoryImpl(db.currencyRateDao())
        monthlyBudgetRepository = MonthlyBudgetRepositoryImpl(db.monthlyBudgetDao())
        savingsTransferRepository = SavingsTransferRepositoryImpl(db.savingsTransferDao())
        transactionRepository = TransactionRepositoryImpl(db.transactionDao())
        userSettingsRepository = UserSettingsRepositoryImpl(db.userSettingsDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserSettingsRepositoryCoordination() = runBlocking {
        val defaultSettings = userSettingsRepository.getUserSettings()
        assertEquals(CurrencyCode.EUR, defaultSettings.displayCurrency)

        val updatedSettings = UserSettings(displayCurrency = CurrencyCode.USD, defaultMonthlyBudgetEur = Money.eurFromMinor(300000L))
        userSettingsRepository.saveUserSettings(updatedSettings)

        val retrieved = userSettingsRepository.getUserSettings()
        assertEquals(CurrencyCode.USD, retrieved.displayCurrency)
        assertEquals(Money.eurFromMinor(300000L), retrieved.defaultMonthlyBudgetEur)
    }

    @Test
    fun testCategoryRepositoryCoordination() = runBlocking {
        val cat = Category(name = "Salary", type = CategoryType.INCOME, iconIdentifier = "ic_income", colorHex = "#00FF00")
        val id = categoryRepository.insertCategory(cat)
        assertTrue(id > 0)

        val active = categoryRepository.observeActiveCategoriesByType(CategoryType.INCOME).first()
        assertEquals(1, active.size)
        assertEquals("Salary", active[0].name)

        categoryRepository.archiveCategory(id)
        val activeAfterArchive = categoryRepository.observeActiveCategoriesByType(CategoryType.INCOME).first()
        assertEquals(0, activeAfterArchive.size)

        val archived = categoryRepository.getCategoryById(id)
        assertNotNull(archived)
        assertEquals(false, archived?.isActive)
    }

    @Test
    fun testMonthlyBudgetRepositoryCoordination() = runBlocking {
        val period = YearMonth.of(2026, 3)
        val budget = MonthlyBudget(period = period, baseBudgetEur = Money.eurFromMinor(200000L))
        val id1 = monthlyBudgetRepository.saveMonthlyBudget(budget)
        assertTrue(id1 > 0)

        // Duplicate save returns existing ID without crash
        val duplicate = MonthlyBudget(period = period, baseBudgetEur = Money.eurFromMinor(250000L))
        val id2 = monthlyBudgetRepository.saveMonthlyBudget(duplicate)
        assertEquals(id1, id2)

        val retrieved = monthlyBudgetRepository.getMonthlyBudget(period)
        assertNotNull(retrieved)
        assertEquals(Money.eurFromMinor(200000L), retrieved?.baseBudgetEur)
    }

    @Test
    fun testSavingsTransferRepositoryCoordination() = runBlocking {
        val period1 = YearMonth.of(2026, 1)
        val period2 = YearMonth.of(2026, 2)

        savingsTransferRepository.saveSavingsTransfer(SavingsTransfer(period = period1, savedAmountEur = Money.eurFromMinor(10000L)))
        savingsTransferRepository.saveSavingsTransfer(SavingsTransfer(period = period2, savedAmountEur = Money.eurFromMinor(-2000L)))

        val total = savingsTransferRepository.getTotalSavingsBalanceEur()
        assertEquals(Money.eurFromMinor(8000L), total)
    }

    @Test
    fun testTransactionRepositoryCoordination() = runBlocking {
        val catId = categoryRepository.insertCategory(Category(name = "General", type = CategoryType.EXPENSE, iconIdentifier = "ic", colorHex = "#0"))

        val period = YearMonth.of(2026, 3)
        val tx = Transaction(
            enteredAmount = Money.eurFromMinor(5000L),
            type = TransactionType.EXPENSE,
            baseValueEur = Money.eurFromMinor(5000L),
            exchangeRate = BigDecimal.ONE,
            description = "Coffee",
            categoryId = catId,
            timestamp = Instant.now(),
            period = period
        )

        val txId = transactionRepository.insertTransaction(tx)
        assertTrue(txId > 0)

        val byMonth = transactionRepository.getTransactionsByMonth(period)
        assertEquals(1, byMonth.size)
        assertEquals("Coffee", byMonth[0].description)

        transactionRepository.deleteTransaction(txId)
        val afterDelete = transactionRepository.getTransactionsByMonth(period)
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testCurrencyRateRepositoryCoordination() = runBlocking {
        val rate = CurrencyRate(
            baseCurrency = CurrencyCode.EUR,
            quoteCurrency = CurrencyCode.USD,
            rate = BigDecimal("1.08"),
            rateDate = LocalDate.of(2026, 3, 31),
            provider = "ECB",
            fetchedAt = Instant.now()
        )

        currencyRateRepository.saveRates(listOf(rate))

        val retrieved = currencyRateRepository.getLatestRate(CurrencyCode.EUR, CurrencyCode.USD)
        assertNotNull(retrieved)
        assertEquals("USD", retrieved?.quoteCurrency?.code)
        assertEquals(0, BigDecimal("1.08").compareTo(retrieved?.rate))
    }
}
