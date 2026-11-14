package com.budgeteer.app.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.usecase.ProcessPendingMonthlyPeriodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartupUiState {
    data object Loading : StartupUiState
    data class Success(val processedMonthsCount: Int) : StartupUiState
    data class Error(val message: String) : StartupUiState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val processPendingMonthlyPeriodsUseCase: ProcessPendingMonthlyPeriodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Loading)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    init {
        runStartupCatchUp()
    }

    fun runStartupCatchUp() {
        viewModelScope.launch {
            _uiState.value = StartupUiState.Loading
            try {
                val count = processPendingMonthlyPeriodsUseCase()
                _uiState.value = StartupUiState.Success(count)
            } catch (e: Exception) {
                _uiState.value = StartupUiState.Error(e.message ?: "Failed to process pending monthly periods")
            }
        }
    }
}
