package com.budgeteer.app.presentation.common.state

import com.budgeteer.app.domain.model.Money

data class MoneyUiModel(
    val rawMoney: Money,
    val formattedText: String,
    val isNegative: Boolean,
    val isPositive: Boolean,
    val isZero: Boolean
)
