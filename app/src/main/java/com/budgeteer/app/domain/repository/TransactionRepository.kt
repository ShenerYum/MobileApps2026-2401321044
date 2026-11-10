package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface TransactionRepository {
    fun observeAllTransactions(): Flow<List<Transaction>>
    fun observeTransactionsByMonth(period: YearMonth): Flow<List<Transaction>>
    suspend fun getTransactionsByMonth(period: YearMonth): List<Transaction>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
}
