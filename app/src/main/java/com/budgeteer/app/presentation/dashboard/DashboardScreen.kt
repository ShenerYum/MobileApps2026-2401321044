package com.budgeteer.app.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.presentation.common.components.BudgetProgressBar
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.components.MonthSelector
import com.budgeteer.app.presentation.common.components.SummaryCard
import com.budgeteer.app.presentation.common.components.TransactionListItem
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = injectViewModel(),
    onNavigateToTransactions: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Dashboard...")
        return
    }

    if (state.isEditBudgetDialogOpen && state.monthlySummary != null) {
        EditBudgetDialog(
            initialBaseAmount = state.monthlySummary!!.baseBudgetEur.amount,
            onDismiss = { viewModel.onDismissEditBudgetDialog() },
            onConfirm = { newAmount -> viewModel.onSaveNewBaseBudget(newAmount) }
        )
    }

    DashboardContent(
        state = state,
        onPreviousMonthClicked = { viewModel.onPreviousMonthClicked() },
        onNextMonthClicked = { viewModel.onNextMonthClicked() },
        onOpenEditBudgetDialog = { viewModel.onOpenEditBudgetDialog() },
        onNavigateToTransactions = onNavigateToTransactions
    )
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    onPreviousMonthClicked: () -> Unit = {},
    onNextMonthClicked: () -> Unit = {},
    onOpenEditBudgetDialog: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToTransactions) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            MonthSelector(
                selectedMonth = state.selectedMonth,
                onPreviousMonthClicked = onPreviousMonthClicked,
                onNextMonthClicked = onNextMonthClicked
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Current Budget Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEditBudgetDialog() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Remaining Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.currentBudgetUiModel?.formattedText ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BudgetProgressBar(
                        baseBudgetUiModel = state.baseBudgetUiModel,
                        currentBudgetUiModel = state.currentBudgetUiModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Income",
                    moneyUiModel = state.totalIncomeUiModel,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    highlightSignedColor = true
                )
                SummaryCard(
                    title = "Expenses",
                    moneyUiModel = state.totalExpensesUiModel,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Savings / Total Savings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Monthly Saved",
                    moneyUiModel = state.savedAmountUiModel,
                    icon = Icons.Default.Savings,
                    modifier = Modifier.weight(1f),
                    highlightSignedColor = true
                )
                SummaryCard(
                    title = "Total Savings",
                    moneyUiModel = state.savingsBalanceUiModel,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToTransactions)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.recentTransactions.isEmpty()) {
                Text(
                    text = "No transactions entered for this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recentTransactions.forEach { tx ->
                        TransactionListItem(
                            description = tx.description,
                            categoryName = tx.categoryName,
                            categoryColorHex = tx.categoryColorHex,
                            amountUiModel = tx.amountUiModel,
                            type = tx.type,
                            timestamp = tx.timestamp,
                            onClick = onNavigateToTransactions
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    BudgeteerTheme {
        DashboardContent(
            state = DashboardUiState(
                isLoading = false,
                selectedMonth = YearMonth.of(2026, 3),
                baseBudgetUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("2000.00"))),
                currentBudgetUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("1450.00"))),
                totalIncomeUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("500.00")), showExplicitPlusSign = true),
                totalExpensesUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("1050.00"))),
                savedAmountUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("-550.00")), showExplicitPlusSign = true),
                savingsBalanceUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("1200.00"))),
                recentTransactions = listOf(
                    DashboardTransactionUiModel(
                        id = 1L,
                        description = "Supermarket",
                        categoryName = "Food",
                        categoryColorHex = "#FF0000",
                        amountUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("-45.50")), showExplicitPlusSign = true),
                        type = TransactionType.EXPENSE,
                        timestamp = Instant.now()
                    )
                )
            )
        )
    }
}
