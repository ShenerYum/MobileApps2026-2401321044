package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidAmountException
import java.time.Instant
import java.time.YearMonth

data class MonthlyBudget(
    val id: Long = 0L,
    val period: YearMonth,
    val baseBudgetEur: Money,
    val isClosed: Boolean = false,
    val closedAt: Instant? = null
) {
    init {
        if (baseBudgetEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Base budget must be in EUR, got: ${baseBudgetEur.currency.code}")
        }
        if (baseBudgetEur.isNegative) {
            throw InvalidAmountException("Base budget cannot be negative: ${baseBudgetEur.amount}")
        }
    }
}
