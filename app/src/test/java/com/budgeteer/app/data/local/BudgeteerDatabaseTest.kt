package com.budgeteer.app.data.local

import com.budgeteer.app.data.local.database.DatabaseConverters
import com.budgeteer.app.data.local.entity.CategoryType
import com.budgeteer.app.data.local.entity.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/**
 * Local JVM unit test for Database Type Converters and Enum mapping.
 * Full in-memory Room database tests are located in Android instrumentation tests.
 */
class BudgeteerDatabaseUnitTest {

    private val converters = DatabaseConverters()

    @Test
    fun testTypeConvertersRoundTrip() {
        assertEquals("INCOME", converters.fromCategoryType(CategoryType.INCOME))
        assertEquals(CategoryType.INCOME, converters.toCategoryType("INCOME"))
        assertEquals("EXPENSE", converters.fromTransactionType(TransactionType.EXPENSE))
        assertEquals(TransactionType.EXPENSE, converters.toTransactionType("EXPENSE"))

        val now = Instant.now()
        val epochMs = converters.fromInstant(now)
        val convertedInstant = converters.toInstant(epochMs)
        assertEquals(now.toEpochMilli(), convertedInstant?.toEpochMilli())
        assertNull(converters.fromInstant(null))
        assertNull(converters.toInstant(null))
    }
}
