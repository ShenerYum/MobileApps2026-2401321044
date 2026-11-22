package com.budgeteer.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgeteer.app.domain.calculator.BudgetCalculator
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.domain.usecase.ChangeMonthlyBudgetUseCase
import com.budgeteer.app.presentation.common.formatting.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsTransferRepository: SavingsTransferRepository,
    private val categoryRepository: CategoryRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val changeMonthlyBudgetUseCase: ChangeMonthlyBudgetUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _isEditBudgetDialogOpen = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            _selectedMonth.flatMapLatest { month ->
                combine(
                    monthlyBudgetRepository.observeMonthlyBudget(month),
                    transactionRepository.observeTransactionsByMonth(month),
                    combine(
                        userSettingsRepository.observeUserSettings(),
                        categoryRepository.observeAllActiveCategories(),
                        savingsTransferRepository.observeTotalSavingsBalanceEur(),
                        _isEditBudgetDialogOpen
                    ) { settings, categories, savingsBalance, isEditOpen ->
                        Tuple4(settings, categories, savingsBalance, isEditOpen)
                    }
                ) { budgetNullable, txs, (settings, categories, savingsBalance, isEditOpen) ->
                    val budget = budgetNullable ?: MonthlyBudget(
                        period = month,
                        baseBudgetEur = settings.defaultMonthlyBudgetEur
                    )
                    val summary = BudgetCalculator.createMonthlySummary(budget, txs)
                    val categoryMap = categories.associateBy { it.id }

                    val recentTxUiModels = txs.take(5).map { tx ->
                        val cat = categoryMap[tx.categoryId]
                        DashboardTransactionUiModel(
                            id = tx.id,
                            description = tx.description,
                            categoryName = cat?.name ?: "Uncategorized",
                            categoryColorHex = cat?.colorHex ?: "#888888",
                            amountUiModel = MoneyFormatter.toUiModel(tx.signedBaseValueEur, showExplicitPlusSign = true),
                            type = tx.type,
                            timestamp = tx.timestamp
                        )
                    }

                    DashboardUiState(
                        isLoading = false,
                        selectedMonth = month,
                        displayCurrency = settings.displayCurrency,
                        monthlySummary = summary,
                        baseBudgetUiModel = MoneyFormatter.toUiModel(summary.baseBudgetEur),
                        currentBudgetUiModel = MoneyFormatter.toUiModel(summary.currentBudgetEur),
                        totalIncomeUiModel = MoneyFormatter.toUiModel(summary.totalIncomeEur, showExplicitPlusSign = true),
                        totalExpensesUiModel = MoneyFormatter.toUiModel(summary.totalExpensesEur),
                        savedAmountUiModel = MoneyFormatter.toUiModel(summary.savedAmountEur, showExplicitPlusSign = true),
                        savingsBalanceUiModel = MoneyFormatter.toUiModel(savingsBalance),
                        recentTransactions = recentTxUiModels,
                        isEditBudgetDialogOpen = isEditOpen
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onPreviousMonthClicked() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun onNextMonthClicked() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun onOpenEditBudgetDialog() {
        _isEditBudgetDialogOpen.value = true
    }

    fun onDismissEditBudgetDialog() {
        _isEditBudgetDialogOpen.value = false
    }

    fun onSaveNewBaseBudget(newBaseAmount: BigDecimal) {
        viewModelScope.launch {
            val state = _uiState.value
            val summary = state.monthlySummary ?: return@launch
            val newBaseBudgetEur = Money.eur(newBaseAmount)

            changeMonthlyBudgetUseCase(
                currentBudgetEur = summary.currentBudgetEur,
                oldBaseBudgetEur = summary.baseBudgetEur,
                newBaseBudgetEur = newBaseBudgetEur
            )

            val currentBudget = monthlyBudgetRepository.getMonthlyBudget(state.selectedMonth)
            val updatedBudget = MonthlyBudget(
                id = currentBudget?.id ?: 0L,
                period = state.selectedMonth,
                baseBudgetEur = newBaseBudgetEur,
                isClosed = currentBudget?.isClosed ?: false,
                closedAt = currentBudget?.closedAt
            )

            monthlyBudgetRepository.saveMonthlyBudget(updatedBudget)
            userSettingsRepository.saveUserSettings(
                userSettingsRepository.getUserSettings().copy(defaultMonthlyBudgetEur = newBaseBudgetEur)
            )
            _isEditBudgetDialogOpen.value = false
        }
    }

    private data class Tuple4<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
