package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class UseCasesTest {

    private val calculateMonthlySummaryUseCase = CalculateMonthlySummaryUseCase()
    private val calculateSavingsBalanceUseCase = CalculateSavingsBalanceUseCase()
    private val changeMonthlyBudgetUseCase = ChangeMonthlyBudgetUseCase()
    private val validateTransactionUseCase = ValidateTransactionUseCase()

    private val period = YearMonth.of(2026, 3)

    @Test
    fun testCalculateMonthlySummaryUseCase() {
        val budget = MonthlyBudget(period = period, baseBudgetEur = Money.eur(BigDecimal("2000.00")))
        val txs = listOf(
            Transaction(
                id = 1L,
                enteredAmount = Money.eur(BigDecimal("1000.00")),
                type = TransactionType.INCOME,
                baseValueEur = Money.eur(BigDecimal("1000.00")),
                exchangeRate = BigDecimal.ONE,
                description = "Salary",
                categoryId = 1L,
                timestamp = Instant.now(),
                period = period
            )
        )

        val summary = calculateMonthlySummaryUseCase(budget, txs)
        assertEquals(BigDecimal("2000.00"), summary.baseBudgetEur.amount)
        assertEquals(BigDecimal("1000.00"), summary.totalIncomeEur.amount)
        assertEquals(BigDecimal("3000.00"), summary.currentBudgetEur.amount)
    }

    @Test
    fun testCalculateSavingsBalanceUseCase() {
        val transfers = listOf(
            SavingsTransfer(1L, YearMonth.of(2026, 1), Money.eur(BigDecimal("500.00"))),
            SavingsTransfer(2L, YearMonth.of(2026, 2), Money.eur(BigDecimal("300.00")))
        )

        val balance = calculateSavingsBalanceUseCase(transfers)
        assertEquals(BigDecimal("800.00"), balance.amount)
    }

    @Test
    fun testChangeMonthlyBudgetUseCase() {
        val current = Money.eur(BigDecimal("1500.00"))
        val oldBase = Money.eur(BigDecimal("2000.00"))
        val newBase = Money.eur(BigDecimal("2500.00"))

        val newCurrent = changeMonthlyBudgetUseCase(current, oldBase, newBase)
        assertEquals(BigDecimal("2000.00"), newCurrent.amount)
    }

    @Test
    fun testValidateTransactionUseCasePassesForValidTransaction() {
        val category = Category(1L, "Salary", CategoryType.INCOME, "ic", "#0")
        val timestamp = Instant.parse("2026-03-15T10:00:00Z")
        val tx = Transaction(
            enteredAmount = Money.eur(BigDecimal("100.00")),
            type = TransactionType.INCOME,
            baseValueEur = Money.eur(BigDecimal("100.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Salary",
            categoryId = 1L,
            timestamp = timestamp,
            period = period
        )

        validateTransactionUseCase(tx, category, ZoneOffset.UTC)
    }
}
