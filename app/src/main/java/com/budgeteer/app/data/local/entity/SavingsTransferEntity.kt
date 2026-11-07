package com.budgeteer.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents the monthly savings transfer calculated upon month closing.
 * Enforces one transfer per period via unique index on (year, month).
 * [savedAmountEurMinor] can be positive, zero, or negative (deficit).
 */
@Entity(
    tableName = "savings_transfers",
    indices = [
        Index(value = ["year", "month"], unique = true)
    ]
)
data class SavingsTransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val year: Int,
    val month: Int,
    val savedAmountEurMinor: Long, // Calculated surplus/deficit in EUR minor units
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
