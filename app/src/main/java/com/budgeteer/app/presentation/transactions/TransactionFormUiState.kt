package com.budgeteer.app.presentation.transactions

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.TransactionType

data class TransactionFormUiState(
    val isEditing: Boolean = false,
    val transactionId: Long = 0L,
    val amountInput: String = "",
    val currencyCode: CurrencyCode = CurrencyCode.EUR,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long? = null,
    val descriptionInput: String = "",
    val availableCategories: List<Category> = emptyList(),
    val isSubmitting: Boolean = false,
    val amountError: String? = null,
    val categoryError: String? = null,
    val generalError: String? = null
)
