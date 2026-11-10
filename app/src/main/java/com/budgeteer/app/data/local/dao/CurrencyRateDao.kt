package com.budgeteer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgeteer.app.data.local.entity.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRate(rate: CurrencyRateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRates(rates: List<CurrencyRateEntity>): List<Long>

    @Query("SELECT * FROM currency_rates WHERE baseCurrency = :baseCurrency AND quoteCurrency = :quoteCurrency ORDER BY rateDate DESC, fetchedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestRate(baseCurrency: String, quoteCurrency: String): CurrencyRateEntity?

    @Query("SELECT * FROM currency_rates WHERE baseCurrency = :baseCurrency AND quoteCurrency = :quoteCurrency ORDER BY rateDate DESC, fetchedAtEpochMs DESC LIMIT 1")
    fun observeLatestRate(baseCurrency: String, quoteCurrency: String): Flow<CurrencyRateEntity?>

    @Query("SELECT * FROM currency_rates ORDER BY rateDate DESC, fetchedAtEpochMs DESC")
    fun observeAllRates(): Flow<List<CurrencyRateEntity>>

    @Query("DELETE FROM currency_rates WHERE fetchedAtEpochMs < :cutoffEpochMs")
    suspend fun deleteRatesOlderThan(cutoffEpochMs: Long): Int
}
