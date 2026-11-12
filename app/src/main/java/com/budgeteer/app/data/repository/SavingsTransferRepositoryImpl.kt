package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.SavingsTransferDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsTransferRepositoryImpl @Inject constructor(
    private val savingsTransferDao: SavingsTransferDao
) : SavingsTransferRepository {

    override fun observeSavingsTransfer(period: YearMonth): Flow<SavingsTransfer?> {
        return savingsTransferDao.observeSavingsTransfer(period.year, period.monthValue).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getSavingsTransfer(period: YearMonth): SavingsTransfer? {
        return savingsTransferDao.getSavingsTransfer(period.year, period.monthValue)?.toDomain()
    }

    override fun observeAllSavingsTransfers(): Flow<List<SavingsTransfer>> {
        return savingsTransferDao.observeAllSavingsTransfers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTotalSavingsBalanceEur(): Flow<Money> {
        return savingsTransferDao.observeTotalSavingsBalanceEurMinor().map { minorUnits ->
            Money.eurFromMinor(minorUnits)
        }
    }

    override suspend fun getTotalSavingsBalanceEur(): Money {
        val minorUnits = savingsTransferDao.getTotalSavingsBalanceEurMinor()
        return Money.eurFromMinor(minorUnits)
    }

    override suspend fun saveSavingsTransfer(transfer: SavingsTransfer): Long {
        val existing = savingsTransferDao.getSavingsTransfer(transfer.period.year, transfer.period.monthValue)
        val createdAt = existing?.createdAtEpochMs ?: System.currentTimeMillis()
        return savingsTransferDao.upsertSavingsTransfer(transfer.toEntity(createdAtEpochMs = createdAt))
    }
}
