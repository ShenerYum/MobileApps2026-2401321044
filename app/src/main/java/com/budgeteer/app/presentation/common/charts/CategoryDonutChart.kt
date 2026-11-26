package com.budgeteer.app.presentation.common.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.analytics.CategoryBreakdown
import com.budgeteer.app.domain.model.analytics.CategorySpendingItem
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal

@Composable
fun CategoryDonutChart(
    breakdown: CategoryBreakdown,
    modifier: Modifier = Modifier
) {
    if (breakdown.items.isEmpty()) {
        Text(
            text = "No category spending in this period.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val defaultColors = listOf(
        Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(140.dp)
        ) {
            val strokeWidth = 28.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = -90f
            breakdown.items.forEachIndexed { index, item ->
                val sweepAngle = (item.percentageOfTotalExpenses.toFloat() / 100f) * 360f
                val color = runCatching { Color(android.graphics.Color.parseColor(item.categoryColorHex)) }
                    .getOrDefault(defaultColors[index % defaultColors.size])

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            breakdown.items.take(5).forEachIndexed { index, item ->
                val color = runCatching { Color(android.graphics.Color.parseColor(item.categoryColorHex)) }
                    .getOrDefault(defaultColors[index % defaultColors.size])

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = color)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.categoryName} (${item.percentageOfTotalExpenses}%)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryDonutChartPreview() {
    BudgeteerTheme {
        CategoryDonutChart(
            breakdown = CategoryBreakdown(
                totalExpensesEur = Money.eur(BigDecimal("100.00")),
                items = listOf(
                    CategorySpendingItem(1L, "Food", "#E53935", "ic", Money.eur(BigDecimal("60.00")), BigDecimal("60.00"), 3),
                    CategorySpendingItem(2L, "Transport", "#1E88E5", "ic", Money.eur(BigDecimal("40.00")), BigDecimal("40.00"), 2)
                )
            )
        )
    }
}
