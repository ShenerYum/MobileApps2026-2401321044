package com.budgeteer.app.presentation.dashboard

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.domain.usecase.ChangeMonthlyBudgetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeMonthlyBudgetRepository : MonthlyBudgetRepository {
        var budget: MonthlyBudget? = null
        override fun observeMonthlyBudget(period: YearMonth): Flow<MonthlyBudget?> = flowOf(budget)
        override suspend fun getMonthlyBudget(period: YearMonth): MonthlyBudget? = budget
        override suspend fun getMostRecentMonthlyBudget(): MonthlyBudget? = budget
        override suspend fun getEarliestMonthlyBudget(): MonthlyBudget? = budget
        override suspend fun saveMonthlyBudget(monthlyBudget: MonthlyBudget): Long {
            budget = monthlyBudget
            return 1L
        }
        override suspend fun updateMonthlyBudget(monthlyBudget: MonthlyBudget) { budget = monthlyBudget }
    }

    private class FakeTransactionRepository : TransactionRepository {
        var txs = mutableListOf<Transaction>()
        override fun observeAllTransactions(): Flow<List<Transaction>> = flowOf(txs)
        override fun observeTransactionsByMonth(period: YearMonth): Flow<List<Transaction>> = flowOf(txs)
        override suspend fun getTransactionsByMonth(period: YearMonth): List<Transaction> = txs
        override suspend fun getTransactionById(id: Long): Transaction? = txs.find { it.id == id }
        override suspend fun getEarliestTransaction(): Transaction? = txs.minByOrNull { it.timestamp }
        override suspend fun insertTransaction(transaction: Transaction): Long {
            txs.add(transaction)
            return 1L
        }
        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: Long) {}
    }

    private class FakeSavingsTransferRepository : SavingsTransferRepository {
        override fun observeSavingsTransfer(period: YearMonth): Flow<SavingsTransfer?> = flowOf(null)
        override suspend fun getSavingsTransfer(period: YearMonth): SavingsTransfer? = null
        override fun observeAllSavingsTransfers(): Flow<List<SavingsTransfer>> = flowOf(emptyList())
        override fun observeTotalSavingsBalanceEur(): Flow<Money> = flowOf(Money.eur(BigDecimal("500.00")))
        override suspend fun getTotalSavingsBalanceEur(): Money = Money.eur(BigDecimal("500.00"))
        override suspend fun saveSavingsTransfer(transfer: SavingsTransfer): Long = 1L
    }

    private class FakeCategoryRepository : CategoryRepository {
        var categories = listOf(Category(1L, "Salary", CategoryType.INCOME, "ic", "#0"))
        override fun observeActiveCategoriesByType(type: CategoryType): Flow<List<Category>> = flowOf(categories)
        override fun observeAllActiveCategories(): Flow<List<Category>> = flowOf(categories)
        override suspend fun getCategoryById(id: Long): Category? = categories.find { it.id == id }
        override suspend fun insertCategory(category: Category): Long = 1L
        override suspend fun updateCategory(category: Category) {}
        override suspend fun archiveCategory(id: Long) {}
    }

    private class FakeUserSettingsRepository : UserSettingsRepository {
        var settings = UserSettings()
        override fun observeUserSettings(): Flow<UserSettings> = flowOf(settings)
        override suspend fun getUserSettings(): UserSettings = settings
        override suspend fun saveUserSettings(settings: UserSettings) { this.settings = settings }
    }

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val budgetRepo = FakeMonthlyBudgetRepository()
        val txRepo = FakeTransactionRepository()
        val savingsRepo = FakeSavingsTransferRepository()
        val catRepo = FakeCategoryRepository()
        val settingsRepo = FakeUserSettingsRepository()
        val changeBudget = ChangeMonthlyBudgetUseCase()

        viewModel = DashboardViewModel(
            budgetRepo, txRepo, savingsRepo, catRepo, settingsRepo, changeBudget
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDashboardInitialDataState() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.baseBudgetUiModel)
        assertNotNull(state.currentBudgetUiModel)
        assertEquals("€2,000.00", state.baseBudgetUiModel?.formattedText)
        assertEquals("€500.00", state.savingsBalanceUiModel?.formattedText)
    }
}
