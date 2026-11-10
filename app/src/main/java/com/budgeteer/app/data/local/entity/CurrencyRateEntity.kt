package com.budgeteer.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached exchange rate entry for a specific currency pair and date.
 * Unique index on (baseCurrency, quoteCurrency, rateDate).
 */
@Entity(
    tableName = "currency_rates",
    indices = [
        Index(value = ["baseCurrency", "quoteCurrency", "rateDate"], unique = true)
    ]
)
data class CurrencyRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val baseCurrency: String,
    val quoteCurrency: String,
    val rate: Double,
    val rateDate: String, // ISO date string (e.g. "2026-03-31")
    val provider: String,
    val fetchedAtEpochMs: Long = System.currentTimeMillis()
)
