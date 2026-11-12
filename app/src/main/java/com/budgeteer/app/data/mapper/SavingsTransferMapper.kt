package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.SavingsTransferEntity
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer
import java.time.YearMonth

fun SavingsTransferEntity.toDomain(): SavingsTransfer {
    return SavingsTransfer(
        id = id,
        period = YearMonth.of(year, month),
        savedAmountEur = Money.eurFromMinor(savedAmountEurMinor)
    )
}

fun SavingsTransfer.toEntity(createdAtEpochMs: Long = System.currentTimeMillis()): SavingsTransferEntity {
    return SavingsTransferEntity(
        id = id,
        year = period.year,
        month = period.monthValue,
        savedAmountEurMinor = savedAmountEur.toMinorUnits(),
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = System.currentTimeMillis()
    )
}
