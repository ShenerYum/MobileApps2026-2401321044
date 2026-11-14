package com.budgeteer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgeteer.app.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMonthlyBudget(monthlyBudget: MonthlyBudgetEntity): Long

    @Update
    suspend fun updateMonthlyBudget(monthlyBudget: MonthlyBudgetEntity): Int

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    fun observeMonthlyBudget(year: Int, month: Int): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getMonthlyBudget(year: Int, month: Int): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getMostRecentMonthlyBudget(): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets ORDER BY year ASC, month ASC LIMIT 1")
    suspend fun getEarliestMonthlyBudget(): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets WHERE isClosed = 0 ORDER BY year ASC, month ASC")
    suspend fun getUnclosedMonthlyBudgets(): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM monthly_budgets ORDER BY year ASC, month ASC")
    fun observeAllMonthlyBudgets(): Flow<List<MonthlyBudgetEntity>>
}
