package com.budgeteer.app.domain.model

import java.time.YearMonth

data class MonthlySummary(
    val period: YearMonth,
    val baseBudgetEur: Money,
    val totalIncomeEur: Money,
    val totalExpensesEur: Money,
    val currentBudgetEur: Money,
    val savedAmountEur: Money
)
