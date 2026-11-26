package com.budgeteer.app.presentation.analytics

import com.budgeteer.app.domain.usecase.analytics.AnalyticsData
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriodType
import com.budgeteer.app.presentation.common.state.UiText

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedPeriodType: AnalyticsPeriodType = AnalyticsPeriodType.THIS_MONTH,
    val currentPeriod: AnalyticsPeriod = AnalyticsPeriod.create(AnalyticsPeriodType.THIS_MONTH),
    val analyticsData: AnalyticsData? = null,
    val errorMessage: UiText? = null
)
