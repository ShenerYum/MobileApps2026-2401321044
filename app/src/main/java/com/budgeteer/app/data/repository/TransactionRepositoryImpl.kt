package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.TransactionDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun observeAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTransactionsByMonth(period: YearMonth): Flow<List<Transaction>> {
        return transactionDao.observeTransactionsByMonth(period.year, period.monthValue).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionsByMonth(period: YearMonth): List<Transaction> {
        return transactionDao.getTransactionsByMonth(period.year, period.monthValue).map { it.toDomain() }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val existing = transactionDao.getTransactionById(transaction.id)
        val createdAt = existing?.createdAtEpochMs ?: System.currentTimeMillis()
        transactionDao.updateTransaction(transaction.toEntity(createdAtEpochMs = createdAt))
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }
}
