package com.budgeteer.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents financial transactions entered by the user.
 * [enteredAmountMinor] is stored as a positive integer value in minor units.
 * [baseValueEurMinor] is the captured, immutable EUR reference value in minor units.
 * Foreign key to [CategoryEntity] with RESTRICT to prevent deleting referenced categories.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["transactionTimestampEpochMs"]),
        Index(value = ["categoryId"]),
        Index(value = ["year", "month"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val enteredAmountMinor: Long, // Positive entered amount in minor units
    val originalCurrencyCode: String,
    val type: TransactionType,
    val baseValueEurMinor: Long, // Immutable captured EUR base value in minor units
    val exchangeRate: Double, // Exchange rate captured at creation/modification
    val exchangeRateDate: String? = null,
    val description: String,
    val categoryId: Long,
    val year: Int,
    val month: Int,
    val transactionTimestampEpochMs: Long,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
