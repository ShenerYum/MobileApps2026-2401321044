package com.budgeteer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgeteer.app.data.local.entity.TransactionEntity
import com.budgeteer.app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long): Int

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY transactionTimestampEpochMs DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE year = :year AND month = :month ORDER BY transactionTimestampEpochMs DESC")
    fun observeTransactionsByMonth(year: Int, month: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE year = :year AND month = :month ORDER BY transactionTimestampEpochMs DESC")
    suspend fun getTransactionsByMonth(year: Int, month: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE transactionTimestampEpochMs BETWEEN :startTimestampMs AND :endTimestampMs ORDER BY transactionTimestampEpochMs DESC")
    fun observeTransactionsByDateRange(startTimestampMs: Long, endTimestampMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY transactionTimestampEpochMs DESC")
    fun observeTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(baseValueEurMinor), 0) FROM transactions WHERE year = :year AND month = :month AND type = :type")
    suspend fun getSumBaseValueEurMinorByMonthAndType(year: Int, month: Int, type: TransactionType): Long
}
