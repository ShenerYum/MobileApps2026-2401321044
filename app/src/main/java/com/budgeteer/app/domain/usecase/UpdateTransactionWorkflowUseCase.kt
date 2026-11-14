package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import java.time.ZoneId
import javax.inject.Inject

class UpdateTransactionWorkflowUseCase @Inject constructor(
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val recalculateClosedMonthUseCase: RecalculateClosedMonthUseCase,
    private val transactionRunner: FinancialTransactionRunner,
    private val zoneId: ZoneId
) {
    suspend operator fun invoke(updatedTransaction: Transaction, category: Category) {
        validateTransactionUseCase(updatedTransaction, category, zoneId)

        transactionRunner.runInTransaction {
            val oldTransaction = transactionRepository.getTransactionById(updatedTransaction.id)
                ?: throw IllegalArgumentException("Transaction with ID ${updatedTransaction.id} not found")

            transactionRepository.updateTransaction(updatedTransaction)

            val affectedPeriods = setOf(oldTransaction.period, updatedTransaction.period)
            affectedPeriods.forEach { period ->
                val budget = monthlyBudgetRepository.getMonthlyBudget(period)
                if ((budget != null) && budget.isClosed) {
                    recalculateClosedMonthUseCase(period)
                }
            }
        }
    }
}
