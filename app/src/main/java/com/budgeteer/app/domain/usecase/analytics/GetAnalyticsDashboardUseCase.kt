package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.CategoryBreakdown
import com.budgeteer.app.domain.model.analytics.CategoryInsight
import com.budgeteer.app.domain.model.analytics.FinancialOverview
import com.budgeteer.app.domain.model.analytics.IncomeExpenseTrend
import com.budgeteer.app.domain.model.analytics.PeriodComparison
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class AnalyticsData(
    val period: AnalyticsPeriod,
    val overview: FinancialOverview,
    val comparison: PeriodComparison,
    val trends: IncomeExpenseTrend,
    val categoryBreakdown: CategoryBreakdown,
    val categoryInsights: List<CategoryInsight>
)

class GetAnalyticsDashboardUseCase(
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase,
    private val getPeriodComparisonUseCase: GetPeriodComparisonUseCase,
    private val getIncomeExpenseTrendsUseCase: GetIncomeExpenseTrendsUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getCategoryInsightsUseCase: GetCategoryInsightsUseCase
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<AnalyticsData> {
        val overviewFlow = getFinancialOverviewUseCase(period)
        val comparisonFlow = getPeriodComparisonUseCase(period)
        val trendsFlow = getIncomeExpenseTrendsUseCase(period)
        val breakdownFlow = getCategoryBreakdownUseCase(period)
        val insightsFlow = getCategoryInsightsUseCase(period)

        return combine(
            overviewFlow,
            comparisonFlow,
            trendsFlow,
            breakdownFlow,
            insightsFlow
        ) { overview, comparison, trends, breakdown, insights ->
            AnalyticsData(
                period = period,
                overview = overview,
                comparison = comparison,
                trends = trends,
                categoryBreakdown = breakdown,
                categoryInsights = insights
            )
        }
    }
}
