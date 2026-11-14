package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.calculator.BudgetCalculator
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.SavingsTransfer
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject

/**
 * Closes a completed calendar month and creates its monthly savings transfer.
 */
class CloseMonthlyPeriodUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsTransferRepository: SavingsTransferRepository,
    private val userSettingsRepository: UserSettingsRepository
) {
    suspend operator fun invoke(period: YearMonth, now: Instant = Instant.now()) {
        val existingBudget = monthlyBudgetRepository.getMonthlyBudget(period)
        val baseBudgetEur = existingBudget?.baseBudgetEur ?: run {
            val mostRecent = monthlyBudgetRepository.getMostRecentMonthlyBudget()
            mostRecent?.baseBudgetEur ?: userSettingsRepository.getUserSettings().defaultMonthlyBudgetEur
        }

        val budgetToClose = MonthlyBudget(
            id = existingBudget?.id ?: 0L,
            period = period,
            baseBudgetEur = baseBudgetEur,
            isClosed = true,
            closedAt = existingBudget?.closedAt ?: now
        )

        monthlyBudgetRepository.saveMonthlyBudget(budgetToClose)

        val transactions = transactionRepository.getTransactionsByMonth(period)
        val summary = BudgetCalculator.createMonthlySummary(budgetToClose, transactions)

        val existingTransfer = savingsTransferRepository.getSavingsTransfer(period)
        val transferToSave = SavingsTransfer(
            id = existingTransfer?.id ?: 0L,
            period = period,
            savedAmountEur = summary.savedAmountEur
        )
        savingsTransferRepository.saveSavingsTransfer(transferToSave)
    }
}
