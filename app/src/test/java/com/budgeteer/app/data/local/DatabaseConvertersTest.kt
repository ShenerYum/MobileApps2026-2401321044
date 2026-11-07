package com.budgeteer.app.data.local

import com.budgeteer.app.data.local.database.DatabaseConverters
import com.budgeteer.app.data.local.entity.CategoryType
import com.budgeteer.app.data.local.entity.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DatabaseConvertersTest {

    private val converters = DatabaseConverters()

    @Test
    fun testCategoryTypeConverterRoundTrip() {
        assertEquals("INCOME", converters.fromCategoryType(CategoryType.INCOME))
        assertEquals(CategoryType.INCOME, converters.toCategoryType("INCOME"))
        assertEquals("EXPENSE", converters.fromCategoryType(CategoryType.EXPENSE))
        assertEquals(CategoryType.EXPENSE, converters.toCategoryType("EXPENSE"))
    }

    @Test
    fun testTransactionTypeConverterRoundTrip() {
        assertEquals("INCOME", converters.fromTransactionType(TransactionType.INCOME))
        assertEquals(TransactionType.INCOME, converters.toTransactionType("INCOME"))
        assertEquals("EXPENSE", converters.fromTransactionType(TransactionType.EXPENSE))
        assertEquals(TransactionType.EXPENSE, converters.toTransactionType("EXPENSE"))
    }

    @Test
    fun testInstantConverterRoundTrip() {
        val now = Instant.now()
        val epochMs = converters.fromInstant(now)
        val convertedInstant = converters.toInstant(epochMs)
        assertEquals(now.toEpochMilli(), convertedInstant?.toEpochMilli())
        assertNull(converters.fromInstant(null))
        assertNull(converters.toInstant(null))
    }
}
