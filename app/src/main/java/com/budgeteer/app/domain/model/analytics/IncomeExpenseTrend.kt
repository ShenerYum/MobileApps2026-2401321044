package com.budgeteer.app.domain.model.analytics

import com.budgeteer.app.domain.model.Money
import java.time.LocalDate

enum class TrendGranularity {
    DAY, WEEK, MONTH
}

data class TrendBucket(
    val bucketLabel: String,
    val startDate: LocalDate,
    val incomeEur: Money,
    val expenseEur: Money,
    val netCashFlowEur: Money
)

data class IncomeExpenseTrend(
    val granularity: TrendGranularity,
    val buckets: List<TrendBucket>,
    val totalIncomeEur: Money,
    val totalExpenseEur: Money
)
