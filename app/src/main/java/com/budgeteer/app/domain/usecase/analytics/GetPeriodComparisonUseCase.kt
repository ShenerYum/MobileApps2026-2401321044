package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.MetricComparison
import com.budgeteer.app.domain.model.analytics.PeriodComparison
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPeriodComparisonUseCase(
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<PeriodComparison> {
        val currentFlow = getFinancialOverviewUseCase(period)
        val comparisonPeriod = period.comparisonPeriod
        val prevFlow = getFinancialOverviewUseCase(comparisonPeriod)

        return combine(currentFlow, prevFlow) { currentOverview, prevOverview ->
            val incomeComp = MetricComparison.calculate(currentOverview.totalIncomeEur, prevOverview.totalIncomeEur)
            val expenseComp = MetricComparison.calculate(currentOverview.totalExpensesEur, prevOverview.totalExpensesEur)
            val netCashFlowComp = MetricComparison.calculate(currentOverview.netCashFlowEur, prevOverview.netCashFlowEur)

            PeriodComparison(
                currentPeriod = period,
                previousPeriod = comparisonPeriod,
                incomeComparison = incomeComp,
                expenseComparison = expenseComp,
                netCashFlowComparison = netCashFlowComp
            )
        }
    }
}
