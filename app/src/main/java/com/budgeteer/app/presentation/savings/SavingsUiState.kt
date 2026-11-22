package com.budgeteer.app.presentation.savings

import com.budgeteer.app.presentation.common.state.MoneyUiModel
import com.budgeteer.app.presentation.common.state.UiText
import java.time.YearMonth

data class SavingsUiState(
    val isLoading: Boolean = true,
    val totalSavingsBalanceUiModel: MoneyUiModel? = null,
    val transfersHistory: List<SavingsTransferItemUiModel> = emptyList(),
    val errorMessage: UiText? = null
)

data class SavingsTransferItemUiModel(
    val id: Long,
    val period: YearMonth,
    val formattedPeriod: String,
    val savedAmountUiModel: MoneyUiModel
)
