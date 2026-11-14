package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.calculator.BudgetCalculator
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject

/**
 * Recalculates the monthly savings transfer for an existing closed month.
 */
class RecalculateClosedMonthUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsTransferRepository: SavingsTransferRepository
) {
    suspend operator fun invoke(period: YearMonth) {
        val monthlyBudget = monthlyBudgetRepository.getMonthlyBudget(period) ?: return
        val transactions = transactionRepository.getTransactionsByMonth(period)

        val summary = BudgetCalculator.createMonthlySummary(monthlyBudget, transactions)

        val existingTransfer = savingsTransferRepository.getSavingsTransfer(period)
        val transferToSave = SavingsTransfer(
            id = existingTransfer?.id ?: 0L,
            period = period,
            savedAmountEur = summary.savedAmountEur
        )
        savingsTransferRepository.saveSavingsTransfer(transferToSave)

        if (!monthlyBudget.isClosed) {
            monthlyBudgetRepository.updateMonthlyBudget(
                monthlyBudget.copy(isClosed = true, closedAt = monthlyBudget.closedAt ?: Instant.now())
            )
        }
    }
}
