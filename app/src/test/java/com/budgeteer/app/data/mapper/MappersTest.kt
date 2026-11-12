package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.CategoryType as EntityCategoryType
import com.budgeteer.app.data.local.entity.TransactionType as EntityTransactionType
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType as DomainCategoryType
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType as DomainTransactionType
import com.budgeteer.app.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class MappersTest {

    @Test
    fun testCategoryMapperBidirectional() {
        val domainCat = Category(
            id = 5L,
            name = "Salary",
            type = DomainCategoryType.INCOME,
            iconIdentifier = "ic_money",
            colorHex = "#00FF00",
            isActive = true
        )

        val entity = domainCat.toEntity(createdAtEpochMs = 1000L)
        assertEquals(5L, entity.id)
        assertEquals("Salary", entity.name)
        assertEquals(EntityCategoryType.INCOME, entity.type)
        assertEquals(1000L, entity.createdAtEpochMs)

        val mappedBack = entity.toDomain()
        assertEquals(domainCat, mappedBack)
    }

    @Test
    fun testCurrencyRateMapperBidirectional() {
        val domainRate = CurrencyRate(
            id = 2L,
            baseCurrency = CurrencyCode.EUR,
            quoteCurrency = CurrencyCode.USD,
            rate = BigDecimal("1.0850"),
            rateDate = LocalDate.of(2026, 3, 31),
            provider = "ECB",
            fetchedAt = Instant.ofEpochMilli(1774915200000L)
        )

        val entity = domainRate.toEntity()
        assertEquals("EUR", entity.baseCurrency)
        assertEquals("USD", entity.quoteCurrency)
        assertEquals(1.0850, entity.rate, 0.00001)
        assertEquals("2026-03-31", entity.rateDate)

        val mappedBack = entity.toDomain()
        assertEquals(domainRate.baseCurrency, mappedBack.baseCurrency)
        assertEquals(domainRate.quoteCurrency, mappedBack.quoteCurrency)
        assertEquals(0, domainRate.rate.compareTo(mappedBack.rate))
        assertEquals(domainRate.rateDate, mappedBack.rateDate)
    }

    @Test
    fun testMonthlyBudgetMapperBidirectional() {
        val domainBudget = MonthlyBudget(
            id = 10L,
            period = YearMonth.of(2026, 3),
            baseBudgetEur = Money.eurFromMinor(200000L),
            isClosed = true,
            closedAt = Instant.ofEpochMilli(1774915200000L)
        )

        val entity = domainBudget.toEntity()
        assertEquals(2026, entity.year)
        assertEquals(3, entity.month)
        assertEquals(200000L, entity.baseBudgetEurMinor)
        assertTrue(entity.isClosed)

        val mappedBack = entity.toDomain()
        assertEquals(domainBudget, mappedBack)
    }

    @Test
    fun testSavingsTransferMapperBidirectional() {
        val domainTransfer = SavingsTransfer(
            id = 3L,
            period = YearMonth.of(2026, 3),
            savedAmountEur = Money.eurFromMinor(-15000L)
        )

        val entity = domainTransfer.toEntity()
        assertEquals(-15000L, entity.savedAmountEurMinor)

        val mappedBack = entity.toDomain()
        assertEquals(domainTransfer, mappedBack)
    }

    @Test
    fun testTransactionMapperBidirectional() {
        val domainTx = Transaction(
            id = 12L,
            enteredAmount = Money.fromMinorUnits(10800L, CurrencyCode.USD),
            type = DomainTransactionType.EXPENSE,
            baseValueEur = Money.eurFromMinor(10000L),
            exchangeRate = BigDecimal("1.08"),
            exchangeRateDate = "2026-03-31",
            description = "Groceries",
            categoryId = 2L,
            timestamp = Instant.ofEpochMilli(1774915200000L),
            period = YearMonth.of(2026, 3)
        )

        val entity = domainTx.toEntity()
        assertEquals(10800L, entity.enteredAmountMinor)
        assertEquals("USD", entity.originalCurrencyCode)
        assertEquals(EntityTransactionType.EXPENSE, entity.type)
        assertEquals(10000L, entity.baseValueEurMinor)
        assertEquals(1.08, entity.exchangeRate, 0.0001)

        val mappedBack = entity.toDomain()
        assertEquals(domainTx.enteredAmount, mappedBack.enteredAmount)
        assertEquals(domainTx.baseValueEur, mappedBack.baseValueEur)
        assertEquals(domainTx.period, mappedBack.period)
    }

    @Test
    fun testUserSettingsMapperBidirectional() {
        val settings = UserSettings(
            displayCurrency = CurrencyCode.USD,
            defaultMonthlyBudgetEur = Money.eurFromMinor(250000L)
        )

        val entity = settings.toEntity()
        assertEquals(1L, entity.id)
        assertEquals("USD", entity.displayCurrency)
        assertEquals(250000L, entity.defaultMonthlyBudgetEurMinor)

        val mappedBack = entity.toDomain()
        assertEquals(settings, mappedBack)
    }
}
