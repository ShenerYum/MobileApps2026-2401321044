package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionWorkflowUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val recalculateClosedMonthUseCase: RecalculateClosedMonthUseCase,
    private val transactionRunner: FinancialTransactionRunner
) {
    suspend operator fun invoke(transactionId: Long) {
        transactionRunner.runInTransaction {
            val existing = transactionRepository.getTransactionById(transactionId) ?: return@runInTransaction
            transactionRepository.deleteTransaction(transactionId)

            val budget = monthlyBudgetRepository.getMonthlyBudget(existing.period)
            if (budget != null && budget.isClosed) {
                recalculateClosedMonthUseCase(existing.period)
            }
        }
    }
}
