package com.budgeteer.app.presentation.common.formatting

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

class FormattingTest {

    @Test
    fun testMoneyFormatterPositiveNegativeZero() {
        val positive = Money.eur(BigDecimal("123.45"))
        val negative = Money.eur(BigDecimal("-50.00"))
        val zero = Money.eur(BigDecimal("0.00"))

        val posFormatted = MoneyFormatter.format(positive, locale = Locale.US, showExplicitPlusSign = true)
        val negFormatted = MoneyFormatter.format(negative, locale = Locale.US)
        val zeroFormatted = MoneyFormatter.format(zero, locale = Locale.US)

        assertEquals("+€123.45", posFormatted)
        assertEquals("-€50.00", negFormatted)
        assertEquals("€0.00", zeroFormatted)
    }

    @Test
    fun testMoneyFormatterMinorUnitsUiModel() {
        val money = Money.fromMinorUnits(250000L, CurrencyCode.EUR)
        val uiModel = MoneyFormatter.toUiModel(money, locale = Locale.US)

        assertEquals("€2,500.00", uiModel.formattedText)
        assertEquals(false, uiModel.isNegative)
        assertEquals(true, uiModel.isPositive)
        assertEquals(false, uiModel.isZero)
    }

    @Test
    fun testDateFormatter() {
        val ym = YearMonth.of(2026, 3)
        val date = LocalDate.of(2026, 3, 31)
        val instant = Instant.parse("2026-03-15T12:00:00Z")

        val ymFormatted = DateFormatter.formatYearMonth(ym, Locale.US)
        val dateFormatted = DateFormatter.formatDate(date, Locale.US)
        val instantFormatted = DateFormatter.formatInstant(instant, ZoneOffset.UTC, Locale.US)

        assertEquals("March 2026", ymFormatted)
        assertEquals("Mar 31, 2026", dateFormatted)
        assertEquals("Mar 15, 2026 • 12:00", instantFormatted)
    }
}
