package com.budgeteer.app.data.repository

import androidx.room.withTransaction
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialTransactionRunnerImpl @Inject constructor(
    private val database: BudgeteerDatabase
) : FinancialTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withTransaction {
            block()
        }
    }
}
