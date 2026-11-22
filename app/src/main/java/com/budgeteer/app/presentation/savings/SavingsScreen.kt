package com.budgeteer.app.presentation.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.budgeteer.app.presentation.common.components.EmptyState
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.components.MoneyText
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.YearMonth

@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel = injectViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Savings...")
        return
    }

    SavingsContent(state = state)
}

@Composable
fun SavingsContent(
    state: SavingsUiState
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Total Savings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Accumulated Savings Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.totalSavingsBalanceUiModel?.formattedText ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Monthly Savings Transfer History",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.transfersHistory.isEmpty()) {
                EmptyState(
                    message = "No monthly savings transfers recorded yet.",
                    icon = Icons.Default.Savings
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.transfersHistory,
                        key = { it.id }
                    ) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.formattedPeriod,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                MoneyText(
                                    moneyUiModel = item.savedAmountUiModel,
                                    style = MaterialTheme.typography.titleMedium,
                                    highlightSignedColor = true
                                )
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
fun SavingsScreenPreview() {
    BudgeteerTheme {
        SavingsContent(
            state = SavingsUiState(
                isLoading = false,
                totalSavingsBalanceUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("1200.00"))),
                transfersHistory = listOf(
                    SavingsTransferItemUiModel(
                        id = 1L,
                        period = YearMonth.of(2026, 2),
                        formattedPeriod = "February 2026",
                        savedAmountUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("600.00")), showExplicitPlusSign = true)
                    ),
                    SavingsTransferItemUiModel(
                        id = 2L,
                        period = YearMonth.of(2026, 1),
                        formattedPeriod = "January 2026",
                        savedAmountUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("600.00")), showExplicitPlusSign = true)
                    )
                )
            )
        )
    }
}
