package com.budgeteer.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budgeteer.app.data.local.dao.CategoryDao
import com.budgeteer.app.data.local.dao.CurrencyRateDao
import com.budgeteer.app.data.local.dao.MonthlyBudgetDao
import com.budgeteer.app.data.local.dao.SavingsTransferDao
import com.budgeteer.app.data.local.dao.TransactionDao
import com.budgeteer.app.data.local.dao.UserSettingsDao
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import com.budgeteer.app.data.local.database.DatabaseConverters
import com.budgeteer.app.data.local.entity.CategoryEntity
import com.budgeteer.app.data.local.entity.CategoryType
import com.budgeteer.app.data.local.entity.CurrencyRateEntity
import com.budgeteer.app.data.local.entity.MonthlyBudgetEntity
import com.budgeteer.app.data.local.entity.SavingsTransferEntity
import com.budgeteer.app.data.local.entity.TransactionEntity
import com.budgeteer.app.data.local.entity.TransactionType
import com.budgeteer.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class BudgeteerDatabaseTest {

    private lateinit var db: BudgeteerDatabase
    private lateinit var userSettingsDao: UserSettingsDao
    private lateinit var monthlyBudgetDao: MonthlyBudgetDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var savingsTransferDao: SavingsTransferDao
    private lateinit var currencyRateDao: CurrencyRateDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BudgeteerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userSettingsDao = db.userSettingsDao()
        monthlyBudgetDao = db.monthlyBudgetDao()
        categoryDao = db.categoryDao()
        transactionDao = db.transactionDao()
        savingsTransferDao = db.savingsTransferDao()
        currencyRateDao = db.currencyRateDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // 1. Each entity can be inserted and read
    @Test
    fun testInsertAndReadEntities() = runBlocking {
        // User Settings
        val settings = UserSettingsEntity(displayCurrency = "USD", defaultMonthlyBudgetEurMinor = 250000L)
        userSettingsDao.upsertUserSettings(settings)
        val readSettings = userSettingsDao.getUserSettings()
        assertNotNull(readSettings)
        assertEquals("USD", readSettings?.displayCurrency)
        assertEquals(250000L, readSettings?.defaultMonthlyBudgetEurMinor)

        // Category
        val category = CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconIdentifier = "ic_salary", colorHex = "#00FF00")
        val catId = categoryDao.insertCategory(category)
        val readCategory = categoryDao.getCategoryById(catId)
        assertNotNull(readCategory)
        assertEquals("Salary", readCategory?.name)

        // Monthly Budget
        val budget = MonthlyBudgetEntity(year = 2026, month = 3, baseBudgetEurMinor = 200000L)
        val budgetId = monthlyBudgetDao.insertMonthlyBudget(budget)
        assertTrue(budgetId > 0)
        val readBudget = monthlyBudgetDao.getMonthlyBudget(2026, 3)
        assertNotNull(readBudget)
        assertEquals(200000L, readBudget?.baseBudgetEurMinor)

        // Transaction
        val tx = TransactionEntity(
            enteredAmountMinor = 10000L,
            originalCurrencyCode = "EUR",
            type = TransactionType.INCOME,
            baseValueEurMinor = 10000L,
            exchangeRate = 1.0,
            description = "Monthly Salary",
            categoryId = catId,
            year = 2026,
            month = 3,
            transactionTimestampEpochMs = 1774915200000L
        )
        val txId = transactionDao.insertTransaction(tx)
        val readTx = transactionDao.getTransactionById(txId)
        assertNotNull(readTx)
        assertEquals(10000L, readTx?.enteredAmountMinor)

        // Savings Transfer
        val transfer = SavingsTransferEntity(year = 2026, month = 3, savedAmountEurMinor = 50000L)
        savingsTransferDao.upsertSavingsTransfer(transfer)
        val readTransfer = savingsTransferDao.getSavingsTransfer(2026, 3)
        assertNotNull(readTransfer)
        assertEquals(50000L, readTransfer?.savedAmountEurMinor)

        // Currency Rate
        val rate = CurrencyRateEntity(baseCurrency = "EUR", quoteCurrency = "USD", rate = 1.08, rateDate = "2026-03-31", provider = "ECB")
        currencyRateDao.upsertRate(rate)
        val readRate = currencyRateDao.getLatestRate("EUR", "USD")
        assertNotNull(readRate)
        assertEquals(1.08, readRate?.rate ?: 0.0, 0.0001)
    }

    // 2. Unique (year, month) constraint prevents duplicate monthly budgets
    @Test
    fun testUniqueMonthlyBudgetConstraint() = runBlocking {
        val b1 = MonthlyBudgetEntity(year = 2026, month = 4, baseBudgetEurMinor = 200000L)
        val id1 = monthlyBudgetDao.insertMonthlyBudget(b1)
        assertTrue(id1 > 0)

        // Duplicate insert should be ignored by OnConflictStrategy.IGNORE
        val b2 = MonthlyBudgetEntity(year = 2026, month = 4, baseBudgetEurMinor = 300000L)
        val id2 = monthlyBudgetDao.insertMonthlyBudget(b2)
        assertEquals(-1L, id2)

        val retrieved = monthlyBudgetDao.getMonthlyBudget(2026, 4)
        assertEquals(200000L, retrieved?.baseBudgetEurMinor)
    }

    // 3. Unique monthly savings-transfer constraint prevents duplicate transfers
    @Test
    fun testUniqueSavingsTransferConstraint() = runBlocking {
        val t1 = SavingsTransferEntity(year = 2026, month = 5, savedAmountEurMinor = 15000L)
        savingsTransferDao.upsertSavingsTransfer(t1)

        // Upserting same year/month should replace existing transfer
        val t2 = SavingsTransferEntity(year = 2026, month = 5, savedAmountEurMinor = 20000L)
        savingsTransferDao.upsertSavingsTransfer(t2)

        val allTransfers = savingsTransferDao.observeAllSavingsTransfers().first()
        assertEquals(1, allTransfers.size)
        assertEquals(20000L, allTransfers[0].savedAmountEurMinor)
    }

    // 4. Positive, zero, and negative savings amounts are persisted correctly
    @Test
    fun testSavingsTransferAmounts() = runBlocking {
        val positive = SavingsTransferEntity(year = 2026, month = 1, savedAmountEurMinor = 10000L)
        val zero = SavingsTransferEntity(year = 2026, month = 2, savedAmountEurMinor = 0L)
        val negative = SavingsTransferEntity(year = 2026, month = 3, savedAmountEurMinor = -5000L)

        savingsTransferDao.upsertSavingsTransfer(positive)
        savingsTransferDao.upsertSavingsTransfer(zero)
        savingsTransferDao.upsertSavingsTransfer(negative)

        val total = savingsTransferDao.getTotalSavingsBalanceEurMinor()
        assertEquals(5000L, total) // 10000 + 0 - 5000
    }

    // 5. Transaction-to-category foreign-key behavior is correct
    @Test
    fun testTransactionCategoryForeignKeyRestriction() = runBlocking {
        val cat = CategoryEntity(name = "Groceries", type = CategoryType.EXPENSE, iconIdentifier = "ic_food", colorHex = "#FF0000")
        val catId = categoryDao.insertCategory(cat)

        val tx = TransactionEntity(
            enteredAmountMinor = 5000L,
            originalCurrencyCode = "EUR",
            type = TransactionType.EXPENSE,
            baseValueEurMinor = 5000L,
            exchangeRate = 1.0,
            description = "Supermarket",
            categoryId = catId,
            year = 2026,
            month = 3,
            transactionTimestampEpochMs = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(tx)

        // Archive/Deactivate should succeed
        val updatedRows = categoryDao.archiveCategory(catId, System.currentTimeMillis())
        assertEquals(1, updatedRows)

        val archivedCategory = categoryDao.getCategoryById(catId)
        assertEquals(false, archivedCategory?.isActive)
    }

    // 6. Transaction date ordering and date-range queries work
    @Test
    fun testTransactionDateOrderingAndRange() = runBlocking {
        val catId = categoryDao.insertCategory(CategoryEntity(name = "Misc", type = CategoryType.EXPENSE, iconIdentifier = "ic", colorHex = "#0"))

        val tx1 = TransactionEntity(enteredAmountMinor = 100L, originalCurrencyCode = "EUR", type = TransactionType.EXPENSE, baseValueEurMinor = 100L, exchangeRate = 1.0, description = "T1", categoryId = catId, year = 2026, month = 3, transactionTimestampEpochMs = 1000L)
        val tx2 = TransactionEntity(enteredAmountMinor = 200L, originalCurrencyCode = "EUR", type = TransactionType.EXPENSE, baseValueEurMinor = 200L, exchangeRate = 1.0, description = "T2", categoryId = catId, year = 2026, month = 3, transactionTimestampEpochMs = 2000L)
        val tx3 = TransactionEntity(enteredAmountMinor = 300L, originalCurrencyCode = "EUR", type = TransactionType.EXPENSE, baseValueEurMinor = 300L, exchangeRate = 1.0, description = "T3", categoryId = catId, year = 2026, month = 3, transactionTimestampEpochMs = 3000L)

        transactionDao.insertTransactions(listOf(tx1, tx2, tx3))

        val ordered = transactionDao.observeAllTransactions().first()
        assertEquals(3, ordered.size)
        assertEquals("T3", ordered[0].description) // Descending timestamp

        val inRange = transactionDao.observeTransactionsByDateRange(1500L, 2500L).first()
        assertEquals(1, inRange.size)
        assertEquals("T2", inRange[0].description)
    }

    // 7. Category type and transaction type query filtering
    @Test
    fun testCategoryAndTransactionTypeFiltering() = runBlocking {
        categoryDao.insertCategory(CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconIdentifier = "ic", colorHex = "#0"))
        categoryDao.insertCategory(CategoryEntity(name = "Food", type = CategoryType.EXPENSE, iconIdentifier = "ic", colorHex = "#0"))

        val incomeCats = categoryDao.observeActiveCategoriesByType(CategoryType.INCOME).first()
        assertEquals(1, incomeCats.size)
        assertEquals("Salary", incomeCats[0].name)

        val expenseCats = categoryDao.observeActiveCategoriesByType(CategoryType.EXPENSE).first()
        assertEquals(1, expenseCats.size)
        assertEquals("Food", expenseCats[0].name)
    }

    // 8. User settings singleton behavior is enforced
    @Test
    fun testUserSettingsSingletonEnforcement() = runBlocking {
        val s1 = UserSettingsEntity(id = 1L, displayCurrency = "EUR", defaultMonthlyBudgetEurMinor = 200000L)
        userSettingsDao.upsertUserSettings(s1)

        val s2 = UserSettingsEntity(id = 1L, displayCurrency = "USD", defaultMonthlyBudgetEurMinor = 250000L)
        userSettingsDao.upsertUserSettings(s2)

        val retrieved = userSettingsDao.getUserSettings()
        assertNotNull(retrieved)
        assertEquals(1L, retrieved?.id)
        assertEquals("USD", retrieved?.displayCurrency)
        assertEquals(250000L, retrieved?.defaultMonthlyBudgetEurMinor)
    }

    // 9. Currency-rate lookup returns the expected latest matching pair
    @Test
    fun testCurrencyRateLookupLatest() = runBlocking {
        val r1 = CurrencyRateEntity(baseCurrency = "EUR", quoteCurrency = "USD", rate = 1.05, rateDate = "2026-03-01", provider = "ECB", fetchedAtEpochMs = 1000L)
        val r2 = CurrencyRateEntity(baseCurrency = "EUR", quoteCurrency = "USD", rate = 1.08, rateDate = "2026-03-31", provider = "ECB", fetchedAtEpochMs = 2000L)

        currencyRateDao.upsertRates(listOf(r1, r2))

        val latest = currencyRateDao.getLatestRate("EUR", "USD")
        assertNotNull(latest)
        assertEquals("2026-03-31", latest?.rateDate)
        assertEquals(1.08, latest?.rate ?: 0.0, 0.0001)
    }

    // 10. Type converters round-trip values correctly
    @Test
    fun testTypeConvertersRoundTrip() {
        val converters = DatabaseConverters()

        // Enum conversions
        assertEquals("INCOME", converters.fromCategoryType(CategoryType.INCOME))
        assertEquals(CategoryType.INCOME, converters.toCategoryType("INCOME"))
        assertEquals("EXPENSE", converters.fromTransactionType(TransactionType.EXPENSE))
        assertEquals(TransactionType.EXPENSE, converters.toTransactionType("EXPENSE"))

        // Instant conversions
        val now = Instant.now()
        val epochMs = converters.fromInstant(now)
        val convertedInstant = converters.toInstant(epochMs)
        assertEquals(now.toEpochMilli(), convertedInstant?.toEpochMilli())
        assertNull(converters.fromInstant(null))
        assertNull(converters.toInstant(null))
    }
}
