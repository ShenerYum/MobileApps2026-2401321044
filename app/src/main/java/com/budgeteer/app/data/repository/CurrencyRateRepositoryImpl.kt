package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.CurrencyRateDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import com.budgeteer.app.domain.repository.CurrencyRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRateRepositoryImpl @Inject constructor(
    private val currencyRateDao: CurrencyRateDao
) : CurrencyRateRepository {

    override suspend fun getLatestRate(baseCurrency: CurrencyCode, quoteCurrency: CurrencyCode): CurrencyRate? {
        return currencyRateDao.getLatestRate(baseCurrency.code, quoteCurrency.code)?.toDomain()
    }

    override fun observeLatestRate(baseCurrency: CurrencyCode, quoteCurrency: CurrencyCode): Flow<CurrencyRate?> {
        return currencyRateDao.observeLatestRate(baseCurrency.code, quoteCurrency.code).map { entity ->
            entity?.toDomain()
        }
    }

    override fun observeAllRates(): Flow<List<CurrencyRate>> {
        return currencyRateDao.observeAllRates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveRates(rates: List<CurrencyRate>) {
        currencyRateDao.upsertRates(rates.map { it.toEntity() })
    }
}
