package com.budgeteer.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.domain.usecase.CreateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.UpdateTransactionWorkflowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class TransactionFormViewModel(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val createTransactionWorkflowUseCase: CreateTransactionWorkflowUseCase,
    private val updateTransactionWorkflowUseCase: UpdateTransactionWorkflowUseCase,
    private val zoneId: ZoneId
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionFormUiState())
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    fun loadFormForTransaction(editingTransactionId: Long?) {
        viewModelScope.launch {
            val settings = userSettingsRepository.getUserSettings()

            if (editingTransactionId != null && editingTransactionId > 0L) {
                val existing = transactionRepository.getTransactionById(editingTransactionId)
                if (existing != null) {
                    _uiState.value = TransactionFormUiState(
                        isEditing = true,
                        transactionId = existing.id,
                        amountInput = existing.enteredAmount.amount.toPlainString(),
                        currencyCode = existing.enteredAmount.currency,
                        selectedType = existing.type,
                        selectedCategoryId = existing.categoryId,
                        descriptionInput = existing.description
                    )
                    loadCategoriesForType(existing.type)
                    return@launch
                }
            }

            _uiState.value = TransactionFormUiState(
                isEditing = false,
                currencyCode = settings.displayCurrency,
                selectedType = TransactionType.EXPENSE
            )
            loadCategoriesForType(TransactionType.EXPENSE)
        }
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            selectedCategoryId = null
        )
        loadCategoriesForType(type)
    }

    private fun loadCategoriesForType(type: TransactionType) {
        viewModelScope.launch {
            categoryRepository.observeActiveCategoriesByType(
                if (type == TransactionType.INCOME) com.budgeteer.app.domain.model.CategoryType.INCOME else com.budgeteer.app.domain.model.CategoryType.EXPENSE
            ).collect { categories ->
                _uiState.value = _uiState.value.copy(
                    availableCategories = categories,
                    selectedCategoryId = _uiState.value.selectedCategoryId ?: categories.firstOrNull()?.id
                )
            }
        }
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amountInput = amount, amountError = null)
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId, categoryError = null)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = description)
    }

    fun onSubmitForm(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amountParsed = state.amountInput.toBigDecimalOrNull()
        if (amountParsed == null || amountParsed <= BigDecimal.ZERO) {
            _uiState.value = state.copy(amountError = "Please enter a valid positive amount")
            return
        }

        val categoryId = state.selectedCategoryId
        if (categoryId == null) {
            _uiState.value = state.copy(categoryError = "Please select a category")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true)
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                    ?: throw IllegalArgumentException("Selected category not found")

                val money = Money(amountParsed, state.currencyCode)
                val now = Instant.now()
                val period = YearMonth.from(now.atZone(zoneId))

                val transaction = Transaction(
                    id = if (state.isEditing) state.transactionId else 0L,
                    enteredAmount = money,
                    type = state.selectedType,
                    baseValueEur = Money.eur(amountParsed),
                    exchangeRate = BigDecimal.ONE,
                    description = state.descriptionInput.ifBlank { category.name },
                    categoryId = categoryId,
                    timestamp = now,
                    period = period
                )

                if (state.isEditing) {
                    updateTransactionWorkflowUseCase(transaction, category)
                } else {
                    createTransactionWorkflowUseCase(transaction, category)
                }

                _uiState.value = state.copy(isSubmitting = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSubmitting = false,
                    generalError = e.message ?: "Failed to save transaction"
                )
            }
        }
    }
}
