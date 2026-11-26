package com.budgeteer.app.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriodType
import com.budgeteer.app.domain.model.analytics.CategoryBreakdown
import com.budgeteer.app.domain.model.analytics.FinancialOverview
import com.budgeteer.app.domain.model.analytics.IncomeExpenseTrend
import com.budgeteer.app.domain.model.analytics.MetricComparison
import com.budgeteer.app.domain.model.analytics.PeriodComparison
import com.budgeteer.app.domain.model.analytics.TrendGranularity
import com.budgeteer.app.domain.usecase.analytics.AnalyticsData
import com.budgeteer.app.presentation.common.charts.CategoryDonutChart
import com.budgeteer.app.presentation.common.charts.TrendBarChart
import com.budgeteer.app.presentation.common.components.EmptyState
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.components.MoneyText
import com.budgeteer.app.presentation.common.components.SummaryCard
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = injectViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Analytics...")
        return
    }

    AnalyticsContent(
        state = state,
        onPeriodTypeSelected = { viewModel.onPeriodTypeSelected(it) }
    )
}

@Composable
fun AnalyticsContent(
    state: AnalyticsUiState,
    onPeriodTypeSelected: (AnalyticsPeriodType) -> Unit = {}
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Financial Analytics",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Period Selector Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AnalyticsPeriodType.entries) { type ->
                    FilterChip(
                        selected = state.selectedPeriodType == type,
                        onClick = { onPeriodTypeSelected(type) },
                        label = { Text(type.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val data = state.analyticsData
            if (data == null || data.overview.transactionCount == 0) {
                EmptyState(message = "No financial activity in this period.")
                return@Column
            }

            // Overview Cards
            val overview = data.overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Income",
                    moneyUiModel = MoneyFormatter.toUiModel(overview.totalIncomeEur, showExplicitPlusSign = true),
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    highlightSignedColor = true
                )
                SummaryCard(
                    title = "Expenses",
                    moneyUiModel = MoneyFormatter.toUiModel(overview.totalExpensesEur),
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net Cash Flow Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Net Cash Flow",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MoneyText(
                        moneyUiModel = MoneyFormatter.toUiModel(overview.netCashFlowEur, showExplicitPlusSign = true),
                        style = MaterialTheme.typography.headlineLarge,
                        highlightSignedColor = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Income / Expense Trends
            Text(
                text = "Spending & Income Trends",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TrendBarChart(trend = data.trends)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spending Distribution by Category
            Text(
                text = "Expense Category Distribution",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CategoryDonutChart(breakdown = data.categoryBreakdown)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Insights
            if (data.categoryInsights.isNotEmpty()) {
                Text(
                    text = "Category Insights",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.categoryInsights.forEach { insight ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = insight.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = insight.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    BudgeteerTheme {
        AnalyticsContent(
            state = AnalyticsUiState(
                isLoading = false,
                analyticsData = AnalyticsData(
                    period = AnalyticsPeriod.create(AnalyticsPeriodType.THIS_MONTH),
                    overview = FinancialOverview(
                        totalIncomeEur = Money.eur(BigDecimal("2500.00")),
                        totalExpensesEur = Money.eur(BigDecimal("1200.00")),
                        netCashFlowEur = Money.eur(BigDecimal("1300.00")),
                        transactionCount = 12,
                        averageExpenseEur = Money.eur(BigDecimal("100.00"))
                    ),
                    comparison = PeriodComparison(
                        currentPeriod = AnalyticsPeriod.create(AnalyticsPeriodType.THIS_MONTH),
                        previousPeriod = AnalyticsPeriod.create(AnalyticsPeriodType.PREVIOUS_MONTH),
                        incomeComparison = MetricComparison.calculate(Money.eur(BigDecimal("2500.00")), Money.eur(BigDecimal("2000.00"))),
                        expenseComparison = MetricComparison.calculate(Money.eur(BigDecimal("1200.00")), Money.eur(BigDecimal("1000.00"))),
                        netCashFlowComparison = MetricComparison.calculate(Money.eur(BigDecimal("1300.00")), Money.eur(BigDecimal("1000.00")))
                    ),
                    trends = IncomeExpenseTrend(
                        granularity = TrendGranularity.DAY,
                        buckets = emptyList(),
                        totalIncomeEur = Money.eur(BigDecimal("2500.00")),
                        totalExpenseEur = Money.eur(BigDecimal("1200.00"))
                    ),
                    categoryBreakdown = CategoryBreakdown(
                        totalExpensesEur = Money.eur(BigDecimal("1200.00")),
                        items = emptyList()
                    ),
                    categoryInsights = emptyList()
                )
            )
        )
    }
}
