package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Sequential missed-month processor executed at application startup.
 * Processes all unclosed completed calendar months in chronological order up to [lastCompletedMonth].
 */
class ProcessPendingMonthlyPeriodsUseCase @Inject constructor(
    private val closeMonthlyPeriodUseCase: CloseMonthlyPeriodUseCase,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionRunner: FinancialTransactionRunner,
    private val clock: Clock,
    private val zoneId: ZoneId
) {
    suspend operator fun invoke(): Int {
        val now = clock.instant()
        val currentMonth = YearMonth.from(now.atZone(zoneId))
        val lastCompletedMonth = currentMonth.minusMonths(1)

        val earliestBudget = monthlyBudgetRepository.getEarliestMonthlyBudget()
        val earliestTransaction = transactionRepository.getEarliestTransaction()

        val anchorPeriod = when {
            earliestBudget != null -> earliestBudget.period
            earliestTransaction != null -> earliestTransaction.period
            else -> currentMonth
        }

        if (anchorPeriod > lastCompletedMonth) {
            return 0
        }

        var processedCount = 0
        var periodToProcess = anchorPeriod

        while (periodToProcess <= lastCompletedMonth) {
            val budget = monthlyBudgetRepository.getMonthlyBudget(periodToProcess)
            if ((budget == null) || !budget.isClosed) {
                val p = periodToProcess
                transactionRunner.runInTransaction {
                    closeMonthlyPeriodUseCase(p, now)
                }
                processedCount++
            }
            periodToProcess = periodToProcess.plusMonths(1)
        }

        return processedCount
    }
}
