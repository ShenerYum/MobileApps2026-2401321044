package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import java.time.ZoneId
import javax.inject.Inject

class CreateTransactionWorkflowUseCase @Inject constructor(
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val recalculateClosedMonthUseCase: RecalculateClosedMonthUseCase,
    private val transactionRunner: FinancialTransactionRunner,
    private val zoneId: ZoneId
) {
    suspend operator fun invoke(transaction: Transaction, category: Category): Long {
        validateTransactionUseCase(transaction, category, zoneId)

        return transactionRunner.runInTransaction {
            val txId = transactionRepository.insertTransaction(transaction)
            val budget = monthlyBudgetRepository.getMonthlyBudget(transaction.period)
            if (budget != null && budget.isClosed) {
                recalculateClosedMonthUseCase(transaction.period)
            }
            txId
        }
    }
}
