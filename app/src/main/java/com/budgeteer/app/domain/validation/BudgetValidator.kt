package com.budgeteer.app.domain.validation

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidAmountException
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.MonthlyBudget

object BudgetValidator {

    fun validate(monthlyBudget: MonthlyBudget) {
        if (monthlyBudget.baseBudgetEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Base budget must be in EUR, got ${monthlyBudget.baseBudgetEur.currency.code}")
        }
        if (monthlyBudget.baseBudgetEur.isNegative) {
            throw InvalidAmountException("Base budget cannot be negative: ${monthlyBudget.baseBudgetEur}")
        }
    }
}
