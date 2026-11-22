package com.budgeteer.app.presentation.dashboard

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.MonthlySummary
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.presentation.common.state.UiText
import java.time.Instant
import java.time.YearMonth

data class DashboardUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val displayCurrency: CurrencyCode = CurrencyCode.EUR,
    val monthlySummary: MonthlySummary? = null,
    val baseBudgetUiModel: MoneyUiModel? = null,
    val currentBudgetUiModel: MoneyUiModel? = null,
    val totalIncomeUiModel: MoneyUiModel? = null,
    val totalExpensesUiModel: MoneyUiModel? = null,
    val savedAmountUiModel: MoneyUiModel? = null,
    val savingsBalanceUiModel: MoneyUiModel? = null,
    val recentTransactions: List<DashboardTransactionUiModel> = emptyList(),
    val errorMessage: UiText? = null,
    val isEditBudgetDialogOpen: Boolean = false
)

data class DashboardTransactionUiModel(
    val id: Long,
    val description: String,
    val categoryName: String,
    val categoryColorHex: String,
    val amountUiModel: MoneyUiModel,
    val type: TransactionType,
    val timestamp: Instant
)
