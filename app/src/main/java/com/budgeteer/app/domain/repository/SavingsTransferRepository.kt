package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface SavingsTransferRepository {
    fun observeSavingsTransfer(period: YearMonth): Flow<SavingsTransfer?>
    suspend fun getSavingsTransfer(period: YearMonth): SavingsTransfer?
    fun observeAllSavingsTransfers(): Flow<List<SavingsTransfer>>
    fun observeTotalSavingsBalanceEur(): Flow<Money>
    suspend fun getTotalSavingsBalanceEur(): Money
    suspend fun saveSavingsTransfer(transfer: SavingsTransfer): Long
}
