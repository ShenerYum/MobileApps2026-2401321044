package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.UserSettingsEntity
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.UserSettings

fun UserSettingsEntity.toDomain(): UserSettings {
    return UserSettings(
        displayCurrency = CurrencyCode(displayCurrency),
        defaultMonthlyBudgetEur = Money.eurFromMinor(defaultMonthlyBudgetEurMinor)
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        id = 1L,
        displayCurrency = displayCurrency.code,
        defaultMonthlyBudgetEurMinor = defaultMonthlyBudgetEur.toMinorUnits(),
        updatedAtEpochMs = System.currentTimeMillis()
    )
}
