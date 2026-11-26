package com.budgeteer.app.presentation.common.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.budgeteer.app.BudgeteerApplication
import com.budgeteer.app.domain.usecase.ChangeMonthlyBudgetUseCase
import com.budgeteer.app.domain.usecase.CloseMonthlyPeriodUseCase
import com.budgeteer.app.domain.usecase.CreateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.DeleteTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ProcessPendingMonthlyPeriodsUseCase
import com.budgeteer.app.domain.usecase.RecalculateClosedMonthUseCase
import com.budgeteer.app.domain.usecase.UpdateTransactionWorkflowUseCase
import com.budgeteer.app.domain.usecase.ValidateTransactionUseCase
import com.budgeteer.app.domain.usecase.analytics.GetAnalyticsDashboardUseCase
import com.budgeteer.app.domain.usecase.analytics.GetCategoryBreakdownUseCase
import com.budgeteer.app.domain.usecase.analytics.GetCategoryInsightsUseCase
import com.budgeteer.app.domain.usecase.analytics.GetFinancialOverviewUseCase
import com.budgeteer.app.domain.usecase.analytics.GetIncomeExpenseTrendsUseCase
import com.budgeteer.app.domain.usecase.analytics.GetPeriodComparisonUseCase
import com.budgeteer.app.presentation.analytics.AnalyticsViewModel
import com.budgeteer.app.presentation.categories.CategoriesViewModel
import com.budgeteer.app.presentation.dashboard.DashboardViewModel
import com.budgeteer.app.presentation.savings.SavingsViewModel
import com.budgeteer.app.presentation.settings.SettingsViewModel
import com.budgeteer.app.presentation.startup.StartupViewModel
import com.budgeteer.app.presentation.transactions.TransactionFormViewModel
import com.budgeteer.app.presentation.transactions.TransactionsViewModel

@Composable
inline fun <reified VM : ViewModel> injectViewModel(): VM {
    val context = LocalContext.current.applicationContext as BudgeteerApplication
    val container = context.container

    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val validateTx = ValidateTransactionUseCase()
            val changeBudget = ChangeMonthlyBudgetUseCase()
            val recalculateClosed = RecalculateClosedMonthUseCase(
                container.monthlyBudgetRepository,
                container.transactionRepository,
                container.savingsTransferRepository
            )
            val closeMonthlyPeriod = CloseMonthlyPeriodUseCase(
                container.monthlyBudgetRepository,
                container.transactionRepository,
                container.savingsTransferRepository,
                container.userSettingsRepository
            )

            return when {
                modelClass.isAssignableFrom(StartupViewModel::class.java) -> {
                    val processPending = ProcessPendingMonthlyPeriodsUseCase(
                        closeMonthlyPeriod,
                        container.monthlyBudgetRepository,
                        container.transactionRepository,
                        container.financialTransactionRunner,
                        container.clock,
                        container.zoneId
                    )
                    StartupViewModel(processPending) as T
                }
                modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                    DashboardViewModel(
                        container.monthlyBudgetRepository,
                        container.transactionRepository,
                        container.savingsTransferRepository,
                        container.categoryRepository,
                        container.userSettingsRepository,
                        changeBudget
                    ) as T
                }
                modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> {
                    val deleteTxWorkflow = DeleteTransactionWorkflowUseCase(
                        container.transactionRepository,
                        container.monthlyBudgetRepository,
                        recalculateClosed,
                        container.financialTransactionRunner
                    )
                    TransactionsViewModel(
                        container.transactionRepository,
                        container.categoryRepository,
                        deleteTxWorkflow
                    ) as T
                }
                modelClass.isAssignableFrom(AnalyticsViewModel::class.java) -> {
                    val overviewUseCase = GetFinancialOverviewUseCase(container.transactionRepository)
                    val comparisonUseCase = GetPeriodComparisonUseCase(overviewUseCase)
                    val trendsUseCase = GetIncomeExpenseTrendsUseCase(container.transactionRepository)
                    val breakdownUseCase = GetCategoryBreakdownUseCase(container.transactionRepository, container.categoryRepository)
                    val insightsUseCase = GetCategoryInsightsUseCase(breakdownUseCase)
                    val analyticsDashboardUseCase = GetAnalyticsDashboardUseCase(
                        overviewUseCase,
                        comparisonUseCase,
                        trendsUseCase,
                        breakdownUseCase,
                        insightsUseCase
                    )
                    AnalyticsViewModel(
                        analyticsDashboardUseCase,
                        container.clock,
                        container.zoneId
                    ) as T
                }
                modelClass.isAssignableFrom(TransactionFormViewModel::class.java) -> {
                    val createTxWorkflow = CreateTransactionWorkflowUseCase(
                        validateTx,
                        container.transactionRepository,
                        container.monthlyBudgetRepository,
                        recalculateClosed,
                        container.financialTransactionRunner,
                        container.zoneId
                    )
                    val updateTxWorkflow = UpdateTransactionWorkflowUseCase(
                        validateTx,
                        container.transactionRepository,
                        container.monthlyBudgetRepository,
                        recalculateClosed,
                        container.financialTransactionRunner,
                        container.zoneId
                    )
                    TransactionFormViewModel(
                        container.categoryRepository,
                        container.transactionRepository,
                        container.userSettingsRepository,
                        createTxWorkflow,
                        updateTxWorkflow,
                        container.zoneId
                    ) as T
                }
                modelClass.isAssignableFrom(SavingsViewModel::class.java) -> {
                    SavingsViewModel(container.savingsTransferRepository) as T
                }
                modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                    CategoriesViewModel(container.categoryRepository) as T
                }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel(
                        container.userSettingsRepository,
                        container.currencyRateRepository
                    ) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    return viewModel(factory = factory)
}
