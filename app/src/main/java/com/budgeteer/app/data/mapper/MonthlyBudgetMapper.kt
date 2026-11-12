package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.MonthlyBudgetEntity
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import java.time.Instant
import java.time.YearMonth

fun MonthlyBudgetEntity.toDomain(): MonthlyBudget {
    return MonthlyBudget(
        id = id,
        period = YearMonth.of(year, month),
        baseBudgetEur = Money.eurFromMinor(baseBudgetEurMinor),
        isClosed = isClosed,
        closedAt = closedAtEpochMs?.let { Instant.ofEpochMilli(it) }
    )
}

fun MonthlyBudget.toEntity(createdAtEpochMs: Long = System.currentTimeMillis()): MonthlyBudgetEntity {
    return MonthlyBudgetEntity(
        id = id,
        year = period.year,
        month = period.monthValue,
        baseBudgetEurMinor = baseBudgetEur.toMinorUnits(),
        isClosed = isClosed,
        createdAtEpochMs = createdAtEpochMs,
        closedAtEpochMs = closedAt?.toEpochMilli()
    )
}
