package com.budgeteer.app.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val type: CategoryType,
    val iconIdentifier: String,
    val colorHex: String,
    val isActive: Boolean = true
)
