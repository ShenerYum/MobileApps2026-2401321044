package com.budgeteer.app.presentation.categories

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.presentation.common.state.UiText

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val selectedType: CategoryType = CategoryType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val errorMessage: UiText? = null,
    val isAddCategoryDialogOpen: Boolean = false,
    val archivingCategoryId: Long? = null
)
