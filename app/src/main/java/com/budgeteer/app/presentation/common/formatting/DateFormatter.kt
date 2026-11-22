package com.budgeteer.app.presentation.common.formatting

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    fun formatYearMonth(
        yearMonth: YearMonth,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        return yearMonth.format(formatter)
    }

    fun formatDate(
        date: LocalDate,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)
        return date.format(formatter)
    }

    fun formatInstant(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val localDateTime = instant.atZone(zoneId)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm", locale)
        return localDateTime.format(formatter)
    }
}
