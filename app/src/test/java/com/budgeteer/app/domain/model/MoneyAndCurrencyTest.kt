package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.calculator.CurrencyCalculator
import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidCurrencyCodeException
import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class MoneyAndCurrencyTest {

    @Test
    fun testCurrencyCodeValidationAndFractionDigits() {
        val eur = CurrencyCode("eur")
        assertEquals("EUR", eur.code)
        assertEquals(2, eur.defaultFractionDigits)

        val jpy = CurrencyCode("JPY")
        assertEquals(0, jpy.defaultFractionDigits)

        val bhd = CurrencyCode("BHD")
        assertEquals(3, bhd.defaultFractionDigits)
    }

    @Test(expected = InvalidCurrencyCodeException::class)
    fun testInvalidCurrencyCodeThrows() {
        CurrencyCode("INVALID")
    }

    @Test
    fun testMoneyMinorUnitConversions() {
        // 2-fraction currency (EUR)
        val eur10 = Money.eurFromMinor(1000L) // 10.00 EUR
        assertEquals(BigDecimal("10.00"), eur10.amount)
        assertEquals(1000L, eur10.toMinorUnits())

        // 0-fraction currency (JPY)
        val jpy1000 = Money.fromMinorUnits(1000L, CurrencyCode.JPY)
        assertEquals(BigDecimal("1000"), jpy1000.amount)
        assertEquals(1000L, jpy1000.toMinorUnits())

        // 3-fraction currency (BHD)
        val bhd1000 = Money.fromMinorUnits(1000L, CurrencyCode.BHD)
        assertEquals(BigDecimal("1.000"), bhd1000.amount)
        assertEquals(1000L, bhd1000.toMinorUnits())
    }

    @Test
    fun testMoneyArithmeticSameCurrency() {
        val m1 = Money.eur(BigDecimal("15.50"))
        val m2 = Money.eur(BigDecimal("4.50"))

        val sum = m1 + m2
        assertEquals(BigDecimal("20.00"), sum.amount)

        val diff = m1 - m2
        assertEquals(BigDecimal("11.00"), diff.amount)

        val neg = -m1
        assertEquals(BigDecimal("-15.50"), neg.amount)
        assertTrue(neg.isNegative)
    }

    @Test(expected = CurrencyMismatchException::class)
    fun testMoneyArithmeticMixedCurrenciesThrows() {
        val eur = Money.eur(BigDecimal("10.00"))
        val usd = Money(BigDecimal("10.00"), CurrencyCode.USD)
        eur + usd
    }

    @Test
    fun testCurrencyConversionSameCurrency() {
        val source = Money.eur(BigDecimal("100.00"))
        val converted = CurrencyCalculator.convert(source, CurrencyCode.EUR, BigDecimal("1.0"))
        assertEquals(source, converted)
    }

    @Test
    fun testCurrencyConversionToTargetCurrency() {
        val source = Money.eur(BigDecimal("100.00"))
        // Rate: 1 EUR = 1.08 USD
        val converted = CurrencyCalculator.convert(source, CurrencyCode.USD, BigDecimal("1.08"))
        assertEquals(CurrencyCode.USD, converted.currency)
        assertEquals(BigDecimal("108.00"), converted.amount)
    }

    @Test
    fun testCurrencyConversionDecimalRounding() {
        val source = Money.eur(BigDecimal("100.00"))
        // Rate: 1 EUR = 1.08555 USD -> rounds to 108.56
        val converted = CurrencyCalculator.convert(
            sourceMoney = source,
            targetCurrency = CurrencyCode.USD,
            rate = BigDecimal("1.08555"),
            roundingMode = RoundingMode.HALF_EVEN
        )
        assertEquals(BigDecimal("108.56"), converted.amount)
    }

    @Test
    fun testConvertToEur() {
        val usdMoney = Money(BigDecimal("108.00"), CurrencyCode.USD)
        // Rate: 1 EUR = 1.08 USD
        val eurMoney = CurrencyCalculator.convertToEur(usdMoney, BigDecimal("1.08"))
        assertEquals(CurrencyCode.EUR, eurMoney.currency)
        assertEquals(BigDecimal("100.00"), eurMoney.amount)
    }

    @Test(expected = InvalidExchangeRateException::class)
    fun testInvalidZeroRateThrows() {
        CurrencyCalculator.convert(Money.eur(BigDecimal("10.00")), CurrencyCode.USD, BigDecimal.ZERO)
    }

    @Test(expected = InvalidExchangeRateException::class)
    fun testInvalidNegativeRateThrows() {
        CurrencyCalculator.convert(Money.eur(BigDecimal("10.00")), CurrencyCode.USD, BigDecimal("-1.08"))
    }
}
