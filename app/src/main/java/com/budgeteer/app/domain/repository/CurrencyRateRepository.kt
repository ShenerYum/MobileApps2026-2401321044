package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import kotlinx.coroutines.flow.Flow

interface CurrencyRateRepository {
    suspend fun getLatestRate(baseCurrency: CurrencyCode, quoteCurrency: CurrencyCode): CurrencyRate?
    fun observeLatestRate(baseCurrency: CurrencyCode, quoteCurrency: CurrencyCode): Flow<CurrencyRate?>
    fun observeAllRates(): Flow<List<CurrencyRate>>
    suspend fun saveRates(rates: List<CurrencyRate>)
}
