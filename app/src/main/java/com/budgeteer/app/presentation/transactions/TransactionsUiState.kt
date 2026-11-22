package com.budgeteer.app.presentation.transactions

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.presentation.common.state.UiText
import java.time.Instant
import java.time.YearMonth

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedTypeFilter: TransactionType? = null,
    val selectedCategoryFilterId: Long? = null,
    val transactions: List<TransactionItemUiModel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isFormSheetOpen: Boolean = false,
    val editingTransactionId: Long? = null,
    val deletingTransactionId: Long? = null,
    val errorMessage: UiText? = null
)

data class TransactionItemUiModel(
    val id: Long,
    val description: String,
    val categoryName: String,
    val categoryColorHex: String,
    val amountUiModel: MoneyUiModel,
    val type: TransactionType,
    val timestamp: Instant,
    val rawTransaction: Transaction
)
