package com.budgeteer.app.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.presentation.common.formatting.DateFormatter
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.Instant

@Composable
fun TransactionListItem(
    description: String,
    categoryName: String,
    categoryColorHex: String,
    amountUiModel: MoneyUiModel,
    type: TransactionType,
    timestamp: Instant,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (type == TransactionType.INCOME) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
            val iconBgColor = if (type == TransactionType.INCOME) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val iconTint = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type.name,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$categoryName • ${DateFormatter.formatInstant(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MoneyText(
                moneyUiModel = amountUiModel,
                style = MaterialTheme.typography.titleMedium,
                highlightSignedColor = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListItemPreview() {
    BudgeteerTheme {
        TransactionListItem(
            description = "Supermarket Groceries",
            categoryName = "Food",
            categoryColorHex = "#FF0000",
            amountUiModel = MoneyUiModel(
                rawMoney = com.budgeteer.app.domain.model.Money.eur(BigDecimal("45.50")),
                formattedText = "-€45.50",
                isNegative = true,
                isPositive = false,
                isZero = false
            ),
            type = TransactionType.EXPENSE,
            timestamp = Instant.now()
        )
    }
}
