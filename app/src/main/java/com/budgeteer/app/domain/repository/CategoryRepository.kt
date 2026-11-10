package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategoriesByType(type: CategoryType): Flow<List<Category>>
    fun observeAllActiveCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun archiveCategory(id: Long)
}
