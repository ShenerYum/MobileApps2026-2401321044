package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface MonthlyBudgetRepository {
    fun observeMonthlyBudget(period: YearMonth): Flow<MonthlyBudget?>
    suspend fun getMonthlyBudget(period: YearMonth): MonthlyBudget?
    suspend fun getMostRecentMonthlyBudget(): MonthlyBudget?
    suspend fun getEarliestMonthlyBudget(): MonthlyBudget?
    suspend fun saveMonthlyBudget(monthlyBudget: MonthlyBudget): Long
    suspend fun updateMonthlyBudget(monthlyBudget: MonthlyBudget)
}
