package com.budgeteer.app.presentation.settings

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.presentation.common.state.UiText

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userSettings: UserSettings = UserSettings(),
    val defaultBudgetUiModel: MoneyUiModel? = null,
    val availableCurrencies: List<CurrencyCode> = listOf(CurrencyCode.EUR, CurrencyCode.USD, CurrencyCode.BGN, CurrencyCode.JPY, CurrencyCode.BHD),
    val cachedRates: List<CurrencyRate> = emptyList(),
    val errorMessage: UiText? = null
)
