package com.budgeteer.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriodType
import com.budgeteer.app.domain.usecase.analytics.GetAnalyticsDashboardUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val getAnalyticsDashboardUseCase: GetAnalyticsDashboardUseCase,
    private val clock: Clock,
    private val zoneId: ZoneId
) : ViewModel() {

    private val _selectedPeriodType = MutableStateFlow(AnalyticsPeriodType.THIS_MONTH)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        observeAnalytics()
    }

    private fun observeAnalytics() {
        viewModelScope.launch {
            _selectedPeriodType.flatMapLatest { periodType ->
                val today = LocalDate.now(clock)
                val period = AnalyticsPeriod.create(periodType, referenceDate = today, zoneId = zoneId)
                getAnalyticsDashboardUseCase(period)
            }.collect { data ->
                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    selectedPeriodType = data.period.type,
                    currentPeriod = data.period,
                    analyticsData = data
                )
            }
        }
    }

    fun onPeriodTypeSelected(periodType: AnalyticsPeriodType) {
        _selectedPeriodType.value = periodType
    }
}
