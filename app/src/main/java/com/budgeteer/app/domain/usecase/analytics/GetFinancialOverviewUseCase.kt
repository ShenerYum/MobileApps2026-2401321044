package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.FinancialOverview
import com.budgeteer.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

class GetFinancialOverviewUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<FinancialOverview> {
        return transactionRepository.observeAllTransactions().map { allTxs ->
            val periodTxs = allTxs.filter { tx ->
                val txInstant = tx.timestamp
                !txInstant.isBefore(period.startInstant) && txInstant.isBefore(period.endInstantExclusive)
            }

            var incomeSum = Money.zero(CurrencyCode.EUR)
            var expenseSum = Money.zero(CurrencyCode.EUR)
            var expenseCount = 0

            periodTxs.forEach { tx ->
                if (tx.type == TransactionType.INCOME) {
                    incomeSum += tx.baseValueEur
                } else {
                    expenseSum += tx.baseValueEur
                    expenseCount++
                }
            }

            val netCashFlow = incomeSum - expenseSum

            val avgExpense = if (expenseCount > 0) {
                Money.eur(
                    expenseSum.amount.divide(BigDecimal(expenseCount), 2, RoundingMode.HALF_UP)
                )
            } else {
                Money.zero(CurrencyCode.EUR)
            }

            FinancialOverview(
                totalIncomeEur = incomeSum,
                totalExpensesEur = expenseSum,
                netCashFlowEur = netCashFlow,
                transactionCount = periodTxs.size,
                averageExpenseEur = avgExpense
            )
        }
    }
}
