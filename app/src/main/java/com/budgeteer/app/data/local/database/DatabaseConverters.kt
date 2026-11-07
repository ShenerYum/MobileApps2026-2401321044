package com.budgeteer.app.data.local.database

import androidx.room.TypeConverter
import com.budgeteer.app.data.local.entity.CategoryType
import com.budgeteer.app.data.local.entity.TransactionType
import java.time.Instant

/**
 * Room type converters for non-primitive fields such as Enums and Instant.
 */
class DatabaseConverters {
    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(epochMilli: Long?): Instant? = epochMilli?.let { Instant.ofEpochMilli(it) }
}
