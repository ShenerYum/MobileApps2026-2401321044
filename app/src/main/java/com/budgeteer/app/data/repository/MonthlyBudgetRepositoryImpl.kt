package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.MonthlyBudgetDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlyBudgetRepositoryImpl @Inject constructor(
    private val monthlyBudgetDao: MonthlyBudgetDao
) : MonthlyBudgetRepository {

    override fun observeMonthlyBudget(period: YearMonth): Flow<MonthlyBudget?> {
        return monthlyBudgetDao.observeMonthlyBudget(period.year, period.monthValue).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getMonthlyBudget(period: YearMonth): MonthlyBudget? {
        return monthlyBudgetDao.getMonthlyBudget(period.year, period.monthValue)?.toDomain()
    }

    override suspend fun getMostRecentMonthlyBudget(): MonthlyBudget? {
        return monthlyBudgetDao.getMostRecentMonthlyBudget()?.toDomain()
    }

    override suspend fun getEarliestMonthlyBudget(): MonthlyBudget? {
        return monthlyBudgetDao.getEarliestMonthlyBudget()?.toDomain()
    }

    override suspend fun saveMonthlyBudget(monthlyBudget: MonthlyBudget): Long {
        val insertedId = monthlyBudgetDao.insertMonthlyBudget(monthlyBudget.toEntity())
        return if (insertedId == -1L) {
            monthlyBudgetDao.getMonthlyBudget(monthlyBudget.period.year, monthlyBudget.period.monthValue)?.id ?: -1L
        } else {
            insertedId
        }
    }

    override suspend fun updateMonthlyBudget(monthlyBudget: MonthlyBudget) {
        val existing = monthlyBudgetDao.getMonthlyBudget(monthlyBudget.period.year, monthlyBudget.period.monthValue)
        val createdAt = existing?.createdAtEpochMs ?: System.currentTimeMillis()
        monthlyBudgetDao.updateMonthlyBudget(monthlyBudget.toEntity(createdAtEpochMs = createdAt))
    }
}
