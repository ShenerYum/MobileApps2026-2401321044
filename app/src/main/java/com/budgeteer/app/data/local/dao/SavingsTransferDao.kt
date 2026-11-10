package com.budgeteer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgeteer.app.data.local.entity.SavingsTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavingsTransfer(transfer: SavingsTransferEntity): Long

    @Update
    suspend fun updateSavingsTransfer(transfer: SavingsTransferEntity): Int

    @Query("SELECT * FROM savings_transfers WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getSavingsTransfer(year: Int, month: Int): SavingsTransferEntity?

    @Query("SELECT * FROM savings_transfers WHERE year = :year AND month = :month LIMIT 1")
    fun observeSavingsTransfer(year: Int, month: Int): Flow<SavingsTransferEntity?>

    @Query("SELECT * FROM savings_transfers ORDER BY year ASC, month ASC")
    fun observeAllSavingsTransfers(): Flow<List<SavingsTransferEntity>>

    @Query("SELECT COALESCE(SUM(savedAmountEurMinor), 0) FROM savings_transfers")
    fun observeTotalSavingsBalanceEurMinor(): Flow<Long>

    @Query("SELECT COALESCE(SUM(savedAmountEurMinor), 0) FROM savings_transfers")
    suspend fun getTotalSavingsBalanceEurMinor(): Long
}
