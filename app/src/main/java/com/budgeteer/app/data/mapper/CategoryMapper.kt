package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.CategoryEntity
import com.budgeteer.app.data.local.entity.CategoryType as EntityCategoryType
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType as DomainCategoryType

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = type.toDomain(),
        iconIdentifier = iconIdentifier,
        colorHex = colorHex,
        isActive = isActive
    )
}

fun Category.toEntity(createdAtEpochMs: Long = System.currentTimeMillis()): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.toEntity(),
        iconIdentifier = iconIdentifier,
        colorHex = colorHex,
        isActive = isActive,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = System.currentTimeMillis()
    )
}

fun EntityCategoryType.toDomain(): DomainCategoryType {
    return when (this) {
        EntityCategoryType.INCOME -> DomainCategoryType.INCOME
        EntityCategoryType.EXPENSE -> DomainCategoryType.EXPENSE
    }
}

fun DomainCategoryType.toEntity(): EntityCategoryType {
    return when (this) {
        DomainCategoryType.INCOME -> EntityCategoryType.INCOME
        DomainCategoryType.EXPENSE -> EntityCategoryType.EXPENSE
    }
}
