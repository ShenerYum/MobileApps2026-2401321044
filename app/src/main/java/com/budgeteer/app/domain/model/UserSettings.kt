package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidAmountException

data class UserSettings(
    val displayCurrency: CurrencyCode = CurrencyCode.EUR,
    val defaultMonthlyBudgetEur: Money = Money.eurFromMinor(200000L)
) {
    init {
        if (defaultMonthlyBudgetEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Default budget must be in EUR, got: ${defaultMonthlyBudgetEur.currency.code}")
        }
        if (defaultMonthlyBudgetEur.isNegative) {
            throw InvalidAmountException("Default monthly budget cannot be negative: ${defaultMonthlyBudgetEur.amount}")
        }
    }
}
