package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.CategoryInsight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal

class GetCategoryInsightsUseCase(
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<List<CategoryInsight>> {
        val currentBreakdownFlow = getCategoryBreakdownUseCase(period)
        val prevBreakdownFlow = getCategoryBreakdownUseCase(period.comparisonPeriod)

        return combine(currentBreakdownFlow, prevBreakdownFlow) { currentBreakdown, prevBreakdown ->
            val insights = mutableListOf<CategoryInsight>()

            val topItem = currentBreakdown.items.maxByOrNull { it.totalAmountSpentEur.amount }
            if (topItem != null && topItem.totalAmountSpentEur.amount > BigDecimal.ZERO) {
                insights.add(
                    CategoryInsight.TopSpendingCategory(
                        categoryName = topItem.categoryName,
                        amountSpentEur = topItem.totalAmountSpentEur,
                        percentageOfTotal = topItem.percentageOfTotalExpenses
                    )
                )
            }

            val prevMap = prevBreakdown.items.associateBy { it.categoryId }

            var largestIncreaseItem: Pair<String, Money>? = null
            var largestIncreaseDiff = BigDecimal.ZERO

            var largestDecreaseItem: Pair<String, Money>? = null
            var largestDecreaseDiff = BigDecimal.ZERO

            currentBreakdown.items.forEach { currItem ->
                val prevItem = prevMap[currItem.categoryId]
                if (prevItem != null) {
                    val diff = currItem.totalAmountSpentEur - prevItem.totalAmountSpentEur
                    if (diff.amount > largestIncreaseDiff) {
                        largestIncreaseDiff = diff.amount
                        largestIncreaseItem = Pair(currItem.categoryName, diff)
                    } else if (diff.amount < largestDecreaseDiff) {
                        largestDecreaseDiff = diff.amount
                        largestDecreaseItem = Pair(currItem.categoryName, Money.eur(diff.amount.abs()))
                    }
                } else if (currItem.totalAmountSpentEur.amount > BigDecimal.ZERO) {
                    insights.add(
                        CategoryInsight.NewCategorySpending(
                            categoryName = currItem.categoryName,
                            amountSpentEur = currItem.totalAmountSpentEur
                        )
                    )
                }
            }

            if (largestIncreaseItem != null && largestIncreaseDiff > BigDecimal.ZERO) {
                insights.add(
                    CategoryInsight.LargestExpenseIncrease(
                        categoryName = largestIncreaseItem!!.first,
                        increaseAmountEur = largestIncreaseItem!!.second,
                        percentageIncrease = null
                    )
                )
            }

            if (largestDecreaseItem != null && largestDecreaseDiff < BigDecimal.ZERO.negate()) {
                insights.add(
                    CategoryInsight.LargestExpenseDecrease(
                        categoryName = largestDecreaseItem!!.first,
                        decreaseAmountEur = largestDecreaseItem!!.second
                    )
                )
            }

            insights
        }
    }
}
