package com.budgeteer.app.domain.model.analytics

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

enum class AnalyticsPeriodType(val label: String) {
    THIS_MONTH("This Month"),
    PREVIOUS_MONTH("Previous Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    PREVIOUS_YEAR("Previous Year")
}

data class AnalyticsPeriod(
    val type: AnalyticsPeriodType,
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val zoneId: ZoneId
) {
    val startInstant: Instant
        get() = startDate.atStartOfDay(zoneId).toInstant()

    val endInstantExclusive: Instant
        get() = endDateInclusive.plusDays(1).atStartOfDay(zoneId).toInstant()

    val comparisonPeriod: AnalyticsPeriod
        get() {
            return when (type) {
                AnalyticsPeriodType.THIS_MONTH, AnalyticsPeriodType.PREVIOUS_MONTH -> {
                    val prevMonth = YearMonth.from(startDate).minusMonths(1)
                    AnalyticsPeriod(
                        type = type,
                        startDate = prevMonth.atDay(1),
                        endDateInclusive = prevMonth.atEndOfMonth(),
                        zoneId = zoneId
                    )
                }
                AnalyticsPeriodType.LAST_3_MONTHS -> {
                    val prevStart = startDate.minusMonths(3)
                    val prevEnd = endDateInclusive.minusMonths(3)
                    AnalyticsPeriod(type, prevStart, prevEnd, zoneId)
                }
                AnalyticsPeriodType.LAST_6_MONTHS -> {
                    val prevStart = startDate.minusMonths(6)
                    val prevEnd = endDateInclusive.minusMonths(6)
                    AnalyticsPeriod(type, prevStart, prevEnd, zoneId)
                }
                AnalyticsPeriodType.THIS_YEAR, AnalyticsPeriodType.PREVIOUS_YEAR -> {
                    val prevStart = startDate.minusYears(1)
                    val prevEnd = endDateInclusive.minusYears(1)
                    AnalyticsPeriod(type, prevStart, prevEnd, zoneId)
                }
            }
        }

    companion object {
        fun create(
            type: AnalyticsPeriodType,
            referenceDate: LocalDate = LocalDate.now(),
            zoneId: ZoneId = ZoneId.systemDefault()
        ): AnalyticsPeriod {
            return when (type) {
                AnalyticsPeriodType.THIS_MONTH -> {
                    val ym = YearMonth.from(referenceDate)
                    AnalyticsPeriod(type, ym.atDay(1), ym.atEndOfMonth(), zoneId)
                }
                AnalyticsPeriodType.PREVIOUS_MONTH -> {
                    val ym = YearMonth.from(referenceDate).minusMonths(1)
                    AnalyticsPeriod(type, ym.atDay(1), ym.atEndOfMonth(), zoneId)
                }
                AnalyticsPeriodType.LAST_3_MONTHS -> {
                    val currentYm = YearMonth.from(referenceDate)
                    val startYm = currentYm.minusMonths(2)
                    AnalyticsPeriod(type, startYm.atDay(1), currentYm.atEndOfMonth(), zoneId)
                }
                AnalyticsPeriodType.LAST_6_MONTHS -> {
                    val currentYm = YearMonth.from(referenceDate)
                    val startYm = currentYm.minusMonths(5)
                    AnalyticsPeriod(type, startYm.atDay(1), currentYm.atEndOfMonth(), zoneId)
                }
                AnalyticsPeriodType.THIS_YEAR -> {
                    val year = referenceDate.year
                    AnalyticsPeriod(type, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), zoneId)
                }
                AnalyticsPeriodType.PREVIOUS_YEAR -> {
                    val year = referenceDate.year - 1
                    AnalyticsPeriod(type, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), zoneId)
                }
            }
        }
    }
}
