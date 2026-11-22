package com.budgeteer.app.presentation.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal

@Composable
fun SummaryCard(
    title: String,
    moneyUiModel: MoneyUiModel?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    cardColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    highlightSignedColor: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (moneyUiModel != null) {
                MoneyText(
                    moneyUiModel = moneyUiModel,
                    style = MaterialTheme.typography.headlineSmall,
                    highlightSignedColor = highlightSignedColor
                )
            } else {
                Text(text = "—", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryCardPreview() {
    BudgeteerTheme {
        SummaryCard(
            title = "Income",
            moneyUiModel = MoneyUiModel(
                rawMoney = com.budgeteer.app.domain.model.Money.eur(BigDecimal("1500.00")),
                formattedText = "€1,500.00",
                isNegative = false,
                isPositive = true,
                isZero = false
            ),
            icon = Icons.Default.ArrowUpward,
            highlightSignedColor = true
        )
    }
}
