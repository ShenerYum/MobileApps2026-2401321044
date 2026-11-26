package com.budgeteer.app.domain.model.analytics

import com.budgeteer.app.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

data class MetricComparison(
    val currentPeriodAmountEur: Money,
    val previousPeriodAmountEur: Money,
    val absoluteDifferenceEur: Money,
    val percentageChange: BigDecimal?,
    val isIncrease: Boolean,
    val isDecrease: Boolean,
    val hasBaseline: Boolean
) {
    companion object {
        fun calculate(current: Money, previous: Money): MetricComparison {
            val diff = current - previous
            val prevAmount = previous.amount
            val currAmount = current.amount

            val (percentage, hasBaseline) = if (prevAmount.compareTo(BigDecimal.ZERO) == 0) {
                Pair(null, false)
            } else {
                val pct = currAmount.subtract(prevAmount)
                    .multiply(BigDecimal("100"))
                    .divide(prevAmount.abs(), 2, RoundingMode.HALF_UP)
                Pair(pct, true)
            }

            return MetricComparison(
                currentPeriodAmountEur = current,
                previousPeriodAmountEur = previous,
                absoluteDifferenceEur = diff,
                percentageChange = percentage,
                isIncrease = diff.amount > BigDecimal.ZERO,
                isDecrease = diff.amount < BigDecimal.ZERO,
                hasBaseline = hasBaseline
            )
        }
    }
}

data class PeriodComparison(
    val currentPeriod: AnalyticsPeriod,
    val previousPeriod: AnalyticsPeriod,
    val incomeComparison: MetricComparison,
    val expenseComparison: MetricComparison,
    val netCashFlowComparison: MetricComparison
)
