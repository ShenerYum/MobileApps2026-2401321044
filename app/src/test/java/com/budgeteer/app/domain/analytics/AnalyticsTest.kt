package com.budgeteer.app.domain.analytics

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriodType
import com.budgeteer.app.domain.model.analytics.MetricComparison
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.usecase.analytics.GetFinancialOverviewUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsTest {

    private val zoneId = ZoneId.of("UTC")
    private val refDate = LocalDate.of(2026, 11, 15)

    private class FakeTransactionRepository(val txList: List<Transaction>) : TransactionRepository {
        override fun observeAllTransactions(): Flow<List<Transaction>> = flowOf(txList)
        override fun observeTransactionsByMonth(period: YearMonth): Flow<List<Transaction>> = flowOf(txList)
        override suspend fun getTransactionsByMonth(period: YearMonth): List<Transaction> = txList
        override suspend fun getTransactionById(id: Long): Transaction? = txList.find { it.id == id }
        override suspend fun getEarliestTransaction(): Transaction? = txList.minByOrNull { it.timestamp }
        override suspend fun insertTransaction(transaction: Transaction): Long = 1L
        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: Long) {}
    }

    private class FakeCategoryRepository(val catList: List<Category>) : CategoryRepository {
        override fun observeActiveCategoriesByType(type: CategoryType): Flow<List<Category>> = flowOf(catList)
        override fun observeAllActiveCategories(): Flow<List<Category>> = flowOf(catList)
        override suspend fun getCategoryById(id: Long): Category? = catList.find { it.id == id }
        override suspend fun insertCategory(category: Category): Long = 1L
        override suspend fun updateCategory(category: Category) {}
        override suspend fun archiveCategory(id: Long) {}
    }

    @Test
    fun testAnalyticsPeriodCreationAndComparison() {
        val period = AnalyticsPeriod.create(AnalyticsPeriodType.THIS_MONTH, referenceDate = refDate, zoneId = zoneId)
        assertEquals(LocalDate.of(2026, 11, 1), period.startDate)
        assertEquals(LocalDate.of(2026, 11, 30), period.endDateInclusive)

        val prevPeriod = period.comparisonPeriod
        assertEquals(LocalDate.of(2026, 10, 1), prevPeriod.startDate)
        assertEquals(LocalDate.of(2026, 10, 31), prevPeriod.endDateInclusive)
    }

    @Test
    fun testMetricComparisonZeroBaseline() {
        val current = Money.eur(BigDecimal("500.00"))
        val previous = Money.eur(BigDecimal("0.00"))

        val comp = MetricComparison.calculate(current, previous)
        assertFalse(comp.hasBaseline)
        assertNull(comp.percentageChange)
        assertEquals(BigDecimal("500.00"), comp.absoluteDifferenceEur.amount)
    }

    @Test
    fun testMetricComparisonValidPercentage() {
        val current = Money.eur(BigDecimal("1500.00"))
        val previous = Money.eur(BigDecimal("1000.00"))

        val comp = MetricComparison.calculate(current, previous)
        assertTrue(comp.hasBaseline)
        assertEquals(BigDecimal("50.00"), comp.percentageChange)
        assertTrue(comp.isIncrease)
    }

    @Test
    fun testFinancialOverviewUseCase() = runTest {
        val txs = listOf(
            Transaction(
                id = 1L,
                enteredAmount = Money.eur(BigDecimal("1000.00")),
                type = TransactionType.INCOME,
                baseValueEur = Money.eur(BigDecimal("1000.00")),
                exchangeRate = BigDecimal.ONE,
                description = "Salary",
                categoryId = 1L,
                timestamp = LocalDate.of(2026, 11, 5).atStartOfDay(zoneId).toInstant(),
                period = YearMonth.of(2026, 11)
            ),
            Transaction(
                id = 2L,
                enteredAmount = Money.eur(BigDecimal("200.00")),
                type = TransactionType.EXPENSE,
                baseValueEur = Money.eur(BigDecimal("200.00")),
                exchangeRate = BigDecimal.ONE,
                description = "Groceries",
                categoryId = 2L,
                timestamp = LocalDate.of(2026, 11, 10).atStartOfDay(zoneId).toInstant(),
                period = YearMonth.of(2026, 11)
            )
        )

        val txRepo = FakeTransactionRepository(txs)
        val overviewUseCase = GetFinancialOverviewUseCase(txRepo)
        val period = AnalyticsPeriod.create(AnalyticsPeriodType.THIS_MONTH, referenceDate = refDate, zoneId = zoneId)

        val overview = overviewUseCase(period).first()
        assertEquals(BigDecimal("1000.00"), overview.totalIncomeEur.amount)
        assertEquals(BigDecimal("200.00"), overview.totalExpensesEur.amount)
        assertEquals(BigDecimal("800.00"), overview.netCashFlowEur.amount)
        assertEquals(2, overview.transactionCount)
    }
}
