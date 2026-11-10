package com.budgeteer.app.domain.validation

import com.budgeteer.app.domain.exception.CategoryTypeMismatchException
import com.budgeteer.app.domain.exception.PeriodMismatchException
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class TransactionValidatorTest {

    private val period = YearMonth.of(2026, 3)
    private val timestamp = Instant.parse("2026-03-15T10:00:00Z")

    private val validIncomeCategory = Category(1L, "Salary", CategoryType.INCOME, "ic", "#0")
    private val validExpenseCategory = Category(2L, "Food", CategoryType.EXPENSE, "ic", "#0")

    @Test
    fun testValidIncomeTransactionPasses() {
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
        TransactionValidator.validate(tx, validIncomeCategory, ZoneOffset.UTC)
    }

    @Test
    fun testValidExpenseTransactionPasses() {
        val tx = Transaction(
            enteredAmount = Money.eur(BigDecimal("50.00")),
            type = TransactionType.EXPENSE,
            baseValueEur = Money.eur(BigDecimal("50.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Groceries",
            categoryId = 2L,
            timestamp = timestamp,
            period = period
        )
        TransactionValidator.validate(tx, validExpenseCategory, ZoneOffset.UTC)
    }

    @Test(expected = CategoryTypeMismatchException::class)
    fun testIncomeTransactionWithExpenseCategoryFails() {
        val tx = Transaction(
            enteredAmount = Money.eur(BigDecimal("100.00")),
            type = TransactionType.INCOME,
            baseValueEur = Money.eur(BigDecimal("100.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Salary",
            categoryId = 2L,
            timestamp = timestamp,
            period = period
        )
        TransactionValidator.validate(tx, validExpenseCategory, ZoneOffset.UTC)
    }

    @Test(expected = CategoryTypeMismatchException::class)
    fun testExpenseTransactionWithIncomeCategoryFails() {
        val tx = Transaction(
            enteredAmount = Money.eur(BigDecimal("50.00")),
            type = TransactionType.EXPENSE,
            baseValueEur = Money.eur(BigDecimal("50.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Groceries",
            categoryId = 1L,
            timestamp = timestamp,
            period = period
        )
        TransactionValidator.validate(tx, validIncomeCategory, ZoneOffset.UTC)
    }

    @Test(expected = PeriodMismatchException::class)
    fun testPeriodMismatchFails() {
        val wrongPeriod = YearMonth.of(2026, 4)
        val tx = Transaction(
            enteredAmount = Money.eur(BigDecimal("100.00")),
            type = TransactionType.INCOME,
            baseValueEur = Money.eur(BigDecimal("100.00")),
            exchangeRate = BigDecimal.ONE,
            description = "Salary",
            categoryId = 1L,
            timestamp = timestamp,
            period = wrongPeriod
        )
        TransactionValidator.validate(tx, validIncomeCategory, ZoneOffset.UTC)
    }
}
