package com.budgeteer.app.domain.model.analytics

import com.budgeteer.app.domain.model.Money

data class FinancialOverview(
    val totalIncomeEur: Money,
    val totalExpensesEur: Money,
    val netCashFlowEur: Money,
    val transactionCount: Int,
    val averageExpenseEur: Money
)
