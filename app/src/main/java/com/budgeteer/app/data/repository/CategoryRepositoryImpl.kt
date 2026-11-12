package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.CategoryDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeActiveCategoriesByType(type: CategoryType): Flow<List<Category>> {
        return categoryDao.observeActiveCategoriesByType(type.toEntity()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeAllActiveCategories(): Flow<List<Category>> {
        return categoryDao.observeAllActiveCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        val existing = categoryDao.getCategoryById(category.id)
        val createdAt = existing?.createdAtEpochMs ?: System.currentTimeMillis()
        categoryDao.updateCategory(category.toEntity(createdAtEpochMs = createdAt))
    }

    override suspend fun archiveCategory(id: Long) {
        categoryDao.archiveCategory(id, System.currentTimeMillis())
    }
}
