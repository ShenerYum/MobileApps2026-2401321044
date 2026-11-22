package com.budgeteer.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.usecase.DeleteTransactionWorkflowUseCase
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val deleteTransactionWorkflowUseCase: DeleteTransactionWorkflowUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    private val _selectedCategoryFilterId = MutableStateFlow<Long?>(null)
    private val _isFormSheetOpen = MutableStateFlow(false)
    private val _editingTransactionId = MutableStateFlow<Long?>(null)
    private val _deletingTransactionId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                combine(_selectedMonth, _selectedTypeFilter, _selectedCategoryFilterId) { m, t, c -> Triple(m, t, c) },
                combine(_isFormSheetOpen, _editingTransactionId, _deletingTransactionId) { f, e, d -> Triple(f, e, d) },
                categoryRepository.observeAllActiveCategories()
            ) { (month, typeFilter, categoryFilterId), (isFormOpen, editId, deleteId), categories ->
                val txs = transactionRepository.getTransactionsByMonth(month)
                val categoryMap = categories.associateBy { it.id }

                val filteredTxs = txs.filter { tx ->
                    val typeMatch = typeFilter == null || tx.type == typeFilter
                    val catMatch = categoryFilterId == null || tx.categoryId == categoryFilterId
                    typeMatch && catMatch
                }

                val itemUiModels = filteredTxs.map { tx ->
                    val cat = categoryMap[tx.categoryId]
                    TransactionItemUiModel(
                        id = tx.id,
                        description = tx.description,
                        categoryName = cat?.name ?: "Uncategorized",
                        categoryColorHex = cat?.colorHex ?: "#888888",
                        amountUiModel = MoneyFormatter.toUiModel(tx.signedBaseValueEur, showExplicitPlusSign = true),
                        type = tx.type,
                        timestamp = tx.timestamp,
                        rawTransaction = tx
                    )
                }

                TransactionsUiState(
                    isLoading = false,
                    selectedMonth = month,
                    selectedTypeFilter = typeFilter,
                    selectedCategoryFilterId = categoryFilterId,
                    transactions = itemUiModels,
                    categories = categories,
                    isFormSheetOpen = isFormOpen,
                    editingTransactionId = editId,
                    deletingTransactionId = deleteId
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onPreviousMonthClicked() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun onNextMonthClicked() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun onTypeFilterSelected(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun onCategoryFilterSelected(categoryId: Long?) {
        _selectedCategoryFilterId.value = categoryId
    }

    fun onOpenAddTransactionSheet() {
        _editingTransactionId.value = null
        _isFormSheetOpen.value = true
    }

    fun onOpenEditTransactionSheet(transactionId: Long) {
        _editingTransactionId.value = transactionId
        _isFormSheetOpen.value = true
    }

    fun onCloseFormSheet() {
        _isFormSheetOpen.value = false
        _editingTransactionId.value = null
    }

    fun onRequestDeleteTransaction(transactionId: Long) {
        _deletingTransactionId.value = transactionId
    }

    fun onDismissDeleteDialog() {
        _deletingTransactionId.value = null
    }

    fun onConfirmDeleteTransaction() {
        val idToDelete = _deletingTransactionId.value ?: return
        viewModelScope.launch {
            deleteTransactionWorkflowUseCase(idToDelete)
            _deletingTransactionId.value = null
        }
    }
}
