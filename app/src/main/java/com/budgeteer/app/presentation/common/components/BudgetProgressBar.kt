package com.budgeteer.app.presentation.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal

@Composable
fun BudgetProgressBar(
    baseBudgetUiModel: MoneyUiModel?,
    currentBudgetUiModel: MoneyUiModel?,
    modifier: Modifier = Modifier
) {
    if (baseBudgetUiModel == null || currentBudgetUiModel == null) return

    val baseAmount = baseBudgetUiModel.rawMoney.amount.toFloat()
    val currentAmount = currentBudgetUiModel.rawMoney.amount.toFloat()

    val progress = if (baseAmount > 0f) {
        ((baseAmount - currentAmount) / baseAmount).coerceIn(0f, 1f)
    } else 0f

    val isOverspent = currentBudgetUiModel.isNegative

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Spent: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Base: ${baseBudgetUiModel.formattedText}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressBarPreview() {
    BudgeteerTheme {
        BudgetProgressBar(
            baseBudgetUiModel = MoneyUiModel(
                rawMoney = com.budgeteer.app.domain.model.Money.eur(BigDecimal("2000.00")),
                formattedText = "€2,000.00",
                isNegative = false,
                isPositive = true,
                isZero = false
            ),
            currentBudgetUiModel = MoneyUiModel(
                rawMoney = com.budgeteer.app.domain.model.Money.eur(BigDecimal("1200.00")),
                formattedText = "€1,200.00",
                isNegative = false,
                isPositive = true,
                isZero = false
            )
        )
    }
}
