package com.budgeteer.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents the base budget period for a specific calendar year and month.
 * Enforces a unique index on (year, month) to ensure at most one monthly budget per period.
 */
@Entity(
    tableName = "monthly_budgets",
    indices = [
        Index(value = ["year", "month"], unique = true)
    ]
)
data class MonthlyBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val year: Int,
    val month: Int,
    val baseBudgetEurMinor: Long, // Base budget baseline stored in EUR minor units
    val isClosed: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val closedAtEpochMs: Long? = null
)
