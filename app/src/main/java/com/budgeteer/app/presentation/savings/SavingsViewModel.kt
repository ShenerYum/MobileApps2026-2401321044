package com.budgeteer.app.presentation.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.presentation.common.formatting.DateFormatter
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

class SavingsViewModel(
    private val savingsTransferRepository: SavingsTransferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        observeSavingsData()
    }

    private fun observeSavingsData() {
        viewModelScope.launch {
            combine(
                savingsTransferRepository.observeTotalSavingsBalanceEur(),
                savingsTransferRepository.observeAllSavingsTransfers()
            ) { totalBalance, transfers ->
                val transferItems = transfers.sortedByDescending { it.period }.map { transfer ->
                    SavingsTransferItemUiModel(
                        id = transfer.id,
                        period = transfer.period,
                        formattedPeriod = DateFormatter.formatYearMonth(transfer.period),
                        savedAmountUiModel = MoneyFormatter.toUiModel(transfer.savedAmountEur, showExplicitPlusSign = true)
                    )
                }

                SavingsUiState(
                    isLoading = false,
                    totalSavingsBalanceUiModel = MoneyFormatter.toUiModel(totalBalance),
                    transfersHistory = transferItems
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
