package com.budgeteer.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.domain.repository.CurrencyRateRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                userSettingsRepository.observeUserSettings(),
                currencyRateRepository.observeAllRates()
            ) { settings, rates ->
                SettingsUiState(
                    isLoading = false,
                    userSettings = settings,
                    defaultBudgetUiModel = MoneyFormatter.toUiModel(settings.defaultMonthlyBudgetEur),
                    cachedRates = rates
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onDisplayCurrencySelected(currencyCode: CurrencyCode) {
        viewModelScope.launch {
            val current = userSettingsRepository.getUserSettings()
            userSettingsRepository.saveUserSettings(current.copy(displayCurrency = currencyCode))
        }
    }

    fun onUpdateDefaultMonthlyBudget(newAmount: BigDecimal) {
        viewModelScope.launch {
            val current = userSettingsRepository.getUserSettings()
            val updated = current.copy(defaultMonthlyBudgetEur = Money.eur(newAmount))
            userSettingsRepository.saveUserSettings(updated)
        }
    }
}
