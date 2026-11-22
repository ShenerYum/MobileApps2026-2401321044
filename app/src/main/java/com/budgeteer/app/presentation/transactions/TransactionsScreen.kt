package com.budgeteer.app.presentation.transactions

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.presentation.common.components.ConfirmDeleteDialog
import com.budgeteer.app.presentation.common.components.EmptyState
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.components.MonthSelector
import com.budgeteer.app.presentation.common.components.TransactionListItem
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = injectViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Transactions...")
        return
    }

    if (state.isFormSheetOpen) {
        TransactionFormSheet(
            editingTransactionId = state.editingTransactionId,
            onDismiss = { viewModel.onCloseFormSheet() }
        )
    }

    if (state.deletingTransactionId != null) {
        ConfirmDeleteDialog(
            title = "Delete Transaction",
            message = "Are you sure you want to delete this transaction? Closed month totals will be recalculated automatically.",
            onConfirm = { viewModel.onConfirmDeleteTransaction() },
            onDismiss = { viewModel.onDismissDeleteDialog() }
        )
    }

    TransactionsContent(
        state = state,
        onPreviousMonthClicked = { viewModel.onPreviousMonthClicked() },
        onNextMonthClicked = { viewModel.onNextMonthClicked() },
        onTypeFilterSelected = { viewModel.onTypeFilterSelected(it) },
        onOpenAddTransactionSheet = { viewModel.onOpenAddTransactionSheet() },
        onOpenEditTransactionSheet = { viewModel.onOpenEditTransactionSheet(it) }
    )
}

@Composable
fun TransactionsContent(
    state: TransactionsUiState,
    onPreviousMonthClicked: () -> Unit = {},
    onNextMonthClicked: () -> Unit = {},
    onTypeFilterSelected: (TransactionType?) -> Unit = {},
    onOpenAddTransactionSheet: () -> Unit = {},
    onOpenEditTransactionSheet: (Long) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAddTransactionSheet) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            MonthSelector(
                selectedMonth = state.selectedMonth,
                onPreviousMonthClicked = onPreviousMonthClicked,
                onNextMonthClicked = onNextMonthClicked
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips (All, Income, Expense)
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = state.selectedTypeFilter == null,
                    onClick = { onTypeFilterSelected(null) },
                    label = { Text("All") }
                )
                Spacer(modifier = Modifier.padding(4.dp))
                FilterChip(
                    selected = state.selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { onTypeFilterSelected(TransactionType.EXPENSE) },
                    label = { Text("Expenses") }
                )
                Spacer(modifier = Modifier.padding(4.dp))
                FilterChip(
                    selected = state.selectedTypeFilter == TransactionType.INCOME,
                    onClick = { onTypeFilterSelected(TransactionType.INCOME) },
                    label = { Text("Income") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.transactions.isEmpty()) {
                EmptyState(
                    message = "No transactions found for this month.",
                    actionLabel = "Add Transaction",
                    onActionClicked = onOpenAddTransactionSheet
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { tx ->
                        TransactionListItem(
                            description = tx.description,
                            categoryName = tx.categoryName,
                            categoryColorHex = tx.categoryColorHex,
                            amountUiModel = tx.amountUiModel,
                            type = tx.type,
                            timestamp = tx.timestamp,
                            onClick = { onOpenEditTransactionSheet(tx.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionsScreenPreview() {
    BudgeteerTheme {
        TransactionsContent(
            state = TransactionsUiState(
                isLoading = false,
                selectedMonth = YearMonth.of(2026, 3),
                transactions = listOf(
                    TransactionItemUiModel(
                        id = 1L,
                        description = "Monthly Salary",
                        categoryName = "Salary",
                        categoryColorHex = "#00FF00",
                        amountUiModel = MoneyFormatter.toUiModel(Money.eur(BigDecimal("2500.00")), showExplicitPlusSign = true),
                        type = TransactionType.INCOME,
                        timestamp = Instant.now(),
                        rawTransaction = com.budgeteer.app.domain.model.Transaction(
                            id = 1L,
                            enteredAmount = Money.eur(BigDecimal("2500.00")),
                            type = TransactionType.INCOME,
                            baseValueEur = Money.eur(BigDecimal("2500.00")),
                            exchangeRate = BigDecimal.ONE,
                            description = "Monthly Salary",
                            categoryId = 1L,
                            timestamp = Instant.now(),
                            period = YearMonth.of(2026, 3)
                        )
                    )
                )
            )
        )
    }
}
