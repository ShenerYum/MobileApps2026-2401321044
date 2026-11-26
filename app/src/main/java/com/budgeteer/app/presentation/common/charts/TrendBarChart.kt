package com.budgeteer.app.presentation.common.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.analytics.IncomeExpenseTrend
import com.budgeteer.app.domain.model.analytics.TrendBucket
import com.budgeteer.app.domain.model.analytics.TrendGranularity
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun TrendBarChart(
    trend: IncomeExpenseTrend,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF2E7D32),
    expenseColor: Color = Color(0xFFC62828)
) {
    if (trend.buckets.isEmpty()) {
        Text(
            text = "No trend data available for this period.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val maxAmount = trend.buckets.flatMap { listOf(it.incomeEur.amount, it.expenseEur.amount) }
        .maxOrNull()?.coerceAtLeast(BigDecimal("1.00")) ?: BigDecimal("1.00")

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.padding(end = 6.dp)) {
                drawCircle(color = incomeColor, radius = 6.dp.toPx())
            }
            Text(
                text = "Income (${trend.totalIncomeEur.amount} EUR)",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.padding(horizontal = 12.dp))
            Canvas(modifier = Modifier.padding(end = 6.dp)) {
                drawCircle(color = expenseColor, radius = 6.dp.toPx())
            }
            Text(
                text = "Expense (${trend.totalExpenseEur.amount} EUR)",
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val bucketCount = trend.buckets.size
            val groupWidth = width / bucketCount
            val barWidth = (groupWidth * 0.35f).coerceAtMost(24.dp.toPx())

            // Baseline
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, height - 20f),
                end = Offset(width, height - 20f),
                strokeWidth = 2f
            )

            trend.buckets.forEachIndexed { index, bucket ->
                val groupLeft = index * groupWidth
                val usableHeight = height - 40f

                val incomeHeight = ((bucket.incomeEur.amount.toFloat() / maxAmount.toFloat()) * usableHeight).coerceAtLeast(0f)
                val expenseHeight = ((bucket.expenseEur.amount.toFloat() / maxAmount.toFloat()) * usableHeight).coerceAtLeast(0f)

                // Income Bar
                if (incomeHeight > 0f) {
                    drawRect(
                        color = incomeColor,
                        topLeft = Offset(groupLeft + (groupWidth * 0.15f), height - 20f - incomeHeight),
                        size = Size(barWidth, incomeHeight)
                    )
                }

                // Expense Bar
                if (expenseHeight > 0f) {
                    drawRect(
                        color = expenseColor,
                        topLeft = Offset(groupLeft + (groupWidth * 0.15f) + barWidth + 4f, height - 20f - expenseHeight),
                        size = Size(barWidth, expenseHeight)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrendBarChartPreview() {
    BudgeteerTheme {
        TrendBarChart(
            trend = IncomeExpenseTrend(
                granularity = TrendGranularity.DAY,
                buckets = listOf(
                    TrendBucket("Nov 1", LocalDate.now(), Money.eur(BigDecimal("100.00")), Money.eur(BigDecimal("40.00")), Money.eur(BigDecimal("60.00"))),
                    TrendBucket("Nov 2", LocalDate.now(), Money.eur(BigDecimal("0.00")), Money.eur(BigDecimal("120.00")), Money.eur(BigDecimal("-120.00")))
                ),
                totalIncomeEur = Money.eur(BigDecimal("100.00")),
                totalExpenseEur = Money.eur(BigDecimal("160.00"))
            )
        )
    }
}
