package com.budgeteer.app.presentation.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.budgeteer.app.presentation.common.formatting.DateFormatter
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.time.YearMonth

@Composable
fun MonthSelector(
    selectedMonth: YearMonth,
    onPreviousMonthClicked: () -> Unit,
    onNextMonthClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonthClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = DateFormatter.formatYearMonth(selectedMonth),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onNextMonthClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthSelectorPreview() {
    BudgeteerTheme {
        MonthSelector(
            selectedMonth = YearMonth.of(2026, 3),
            onPreviousMonthClicked = {},
            onNextMonthClicked = {}
        )
    }
}
