package com.budgeteer.app.presentation.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal

@Composable
fun MoneyText(
    moneyUiModel: MoneyUiModel,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    highlightSignedColor: Boolean = false,
    customColor: Color? = null
) {
    val color = when {
        customColor != null -> customColor
        highlightSignedColor && moneyUiModel.isPositive -> Color(0xFF2E7D32) // Positive green
        highlightSignedColor && moneyUiModel.isNegative -> Color(0xFFC62828) // Negative red
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = moneyUiModel.formattedText,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}

@Preview(showBackground = true)
@Composable
fun MoneyTextPreview() {
    BudgeteerTheme {
        MoneyText(
            moneyUiModel = MoneyUiModel(
                rawMoney = com.budgeteer.app.domain.model.Money.eur(BigDecimal("2000.00")),
                formattedText = "€2,000.00",
                isNegative = false,
                isPositive = true,
                isZero = false
            )
        )
    }
}
