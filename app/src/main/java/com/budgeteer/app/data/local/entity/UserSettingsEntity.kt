package com.budgeteer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists application-wide settings as a singleton row in Room.
 * The primary key [id] is fixed to 1L to prevent multiple settings records.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Long = 1L,
    val displayCurrency: String = "EUR",
    val defaultMonthlyBudgetEurMinor: Long = 200000L, // Default €2,000.00 stored in EUR minor units (cents)
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
