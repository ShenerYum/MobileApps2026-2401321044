package com.budgeteer.app.domain.calculator

import com.budgeteer.app.domain.exception.PeriodMismatchException
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth

class BudgetCalculatorTest {

    private val period = YearMonth.of(2026, 3)

    private fun createTx(
        amountEur: BigDecimal,
        type: TransactionType,
        txPeriod: YearMonth = period
    ): Transaction {
        val money = Money.eur(amountEur)
        return Transaction(
            id = 1L,
            enteredAmount = money,
            type = type,
            baseValueEur = money,
            exchangeRate = BigDecimal.ONE,
            description = "Test",
            categoryId = 1L,
            timestamp = Instant.now(),
            period = txPeriod
        )
    }

    @Test
    fun testIncomeAndExpensesTotals() {
        val txs = listOf(
            createTx(BigDecimal("1000.00"), TransactionType.INCOME),
            createTx(BigDecimal("500.00"), TransactionType.INCOME),
            createTx(BigDecimal("300.00"), TransactionType.EXPENSE)
        )

        val totalIncome = BudgetCalculator.calculateTotalIncome(txs, period)
        val totalExpenses = BudgetCalculator.calculateTotalExpenses(txs, period)

        assertEquals(BigDecimal("1500.00"), totalIncome.amount)
        assertEquals(BigDecimal("300.00"), totalExpenses.amount)
    }

    @Test
    fun testEmptyTransactionsGivesZeroTotals() {
        val totalIncome = BudgetCalculator.calculateTotalIncome(emptyList(), period)
        val totalExpenses = BudgetCalculator.calculateTotalExpenses(emptyList(), period)

        assertEquals(BigDecimal("0"), totalIncome.amount)
        assertEquals(BigDecimal("0"), totalExpenses.amount)
    }

    @Test
    fun testCurrentBudgetAndSavedAmount() {
        val baseBudget = Money.eur(BigDecimal("2000.00"))
        val totalIncome = Money.eur(BigDecimal("1500.00"))
        val totalExpenses = Money.eur(BigDecimal("800.00"))

        val currentBudget = BudgetCalculator.calculateCurrentBudget(baseBudget, totalIncome, totalExpenses)
        assertEquals(BigDecimal("2700.00"), currentBudget.amount)

        val savedAmount = BudgetCalculator.calculateSavedAmount(currentBudget, baseBudget)
        assertEquals(BigDecimal("700.00"), savedAmount.amount)
    }

    @Test
    fun testNegativeCurrentBudgetAndSavedAmount() {
        val baseBudget = Money.eur(BigDecimal("1000.00"))
        val totalIncome = Money.eur(BigDecimal("200.00"))
        val totalExpenses = Money.eur(BigDecimal("1500.00"))

        val currentBudget = BudgetCalculator.calculateCurrentBudget(baseBudget, totalIncome, totalExpenses)
        assertEquals(BigDecimal("-300.00"), currentBudget.amount)

        val savedAmount = BudgetCalculator.calculateSavedAmount(currentBudget, baseBudget)
        assertEquals(BigDecimal("-1300.00"), savedAmount.amount)
    }

    @Test
    fun testNewCurrentBudgetFormulaIncreaseAndDecrease() {
        val oldBase = Money.eur(BigDecimal("2000.00"))
        val current = Money.eur(BigDecimal("1500.00"))

        // Increase base budget by 500
        val newBaseIncrease = Money.eur(BigDecimal("2500.00"))
        val newCurrent1 = BudgetCalculator.calculateNewCurrentBudget(current, oldBase, newBaseIncrease)
        assertEquals(BigDecimal("2000.00"), newCurrent1.amount)

        // Decrease base budget by 500
        val newBaseDecrease = Money.eur(BigDecimal("1500.00"))
        val newCurrent2 = BudgetCalculator.calculateNewCurrentBudget(current, oldBase, newBaseDecrease)
        assertEquals(BigDecimal("1000.00"), newCurrent2.amount)
    }

    @Test(expected = PeriodMismatchException::class)
    fun testTransactionsFromAnotherPeriodAreRejected() {
        val wrongPeriod = YearMonth.of(2026, 4)
        val txs = listOf(createTx(BigDecimal("100.00"), TransactionType.INCOME, wrongPeriod))
        BudgetCalculator.calculateTotalIncome(txs, period)
    }

    @Test
    fun testCreateMonthlySummary() {
        val budget = MonthlyBudget(period = period, baseBudgetEur = Money.eur(BigDecimal("2000.00")))
        val txs = listOf(
            createTx(BigDecimal("1000.00"), TransactionType.INCOME),
            createTx(BigDecimal("400.00"), TransactionType.EXPENSE)
        )

        val summary = BudgetCalculator.createMonthlySummary(budget, txs)
        assertEquals(period, summary.period)
        assertEquals(BigDecimal("2000.00"), summary.baseBudgetEur.amount)
        assertEquals(BigDecimal("1000.00"), summary.totalIncomeEur.amount)
        assertEquals(BigDecimal("400.00"), summary.totalExpensesEur.amount)
        assertEquals(BigDecimal("2600.00"), summary.currentBudgetEur.amount)
        assertEquals(BigDecimal("600.00"), summary.savedAmountEur.amount)
    }
}
