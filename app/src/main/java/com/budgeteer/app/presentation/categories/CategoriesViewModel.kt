package com.budgeteer.app.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow(CategoryType.EXPENSE)
    private val _isAddCategoryDialogOpen = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            _selectedType.collect { type ->
                categoryRepository.observeActiveCategoriesByType(type).collect { categories ->
                    _uiState.value = CategoriesUiState(
                        isLoading = false,
                        selectedType = type,
                        categories = categories,
                        isAddCategoryDialogOpen = _isAddCategoryDialogOpen.value
                    )
                }
            }
        }
    }

    fun onTypeSelected(type: CategoryType) {
        _selectedType.value = type
    }

    fun onOpenAddCategoryDialog() {
        _isAddCategoryDialogOpen.value = true
        _uiState.value = _uiState.value.copy(isAddCategoryDialogOpen = true)
    }

    fun onDismissAddCategoryDialog() {
        _isAddCategoryDialogOpen.value = false
        _uiState.value = _uiState.value.copy(isAddCategoryDialogOpen = false)
    }

    fun onAddCategory(name: String, iconIdentifier: String, colorHex: String) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                type = _selectedType.value,
                iconIdentifier = iconIdentifier,
                colorHex = colorHex,
                isActive = true
            )
            categoryRepository.insertCategory(category)
            onDismissAddCategoryDialog()
        }
    }

    fun onArchiveCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(id)
        }
    }
}
