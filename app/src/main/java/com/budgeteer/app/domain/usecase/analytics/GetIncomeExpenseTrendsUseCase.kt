package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriodType
import com.budgeteer.app.domain.model.analytics.IncomeExpenseTrend
import com.budgeteer.app.domain.model.analytics.TrendBucket
import com.budgeteer.app.domain.model.analytics.TrendGranularity
import com.budgeteer.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GetIncomeExpenseTrendsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<IncomeExpenseTrend> {
        val granularity = when (period.type) {
            AnalyticsPeriodType.THIS_MONTH, AnalyticsPeriodType.PREVIOUS_MONTH -> TrendGranularity.DAY
            AnalyticsPeriodType.LAST_3_MONTHS, AnalyticsPeriodType.LAST_6_MONTHS -> TrendGranularity.WEEK
            AnalyticsPeriodType.THIS_YEAR, AnalyticsPeriodType.PREVIOUS_YEAR -> TrendGranularity.MONTH
        }

        return transactionRepository.observeAllTransactions().map { allTxs ->
            val periodTxs = allTxs.filter { tx ->
                val txInstant = tx.timestamp
                !txInstant.isBefore(period.startInstant) && txInstant.isBefore(period.endInstantExclusive)
            }

            val buckets = mutableListOf<TrendBucket>()
            var totalIncome = Money.zero(CurrencyCode.EUR)
            var totalExpense = Money.zero(CurrencyCode.EUR)

            when (granularity) {
                TrendGranularity.DAY -> {
                    var currDate = period.startDate
                    val dayFormatter = DateTimeFormatter.ofPattern("MMM d")
                    while (!currDate.isAfter(period.endDateInclusive)) {
                        val dayStart = currDate.atStartOfDay(period.zoneId).toInstant()
                        val dayEnd = currDate.plusDays(1).atStartOfDay(period.zoneId).toInstant()

                        val dayTxs = periodTxs.filter { tx ->
                            !tx.timestamp.isBefore(dayStart) && tx.timestamp.isBefore(dayEnd)
                        }

                        var dayIncome = Money.zero(CurrencyCode.EUR)
                        var dayExpense = Money.zero(CurrencyCode.EUR)

                        dayTxs.forEach { tx ->
                            if (tx.type == TransactionType.INCOME) dayIncome += tx.baseValueEur
                            else dayExpense += tx.baseValueEur
                        }

                        totalIncome += dayIncome
                        totalExpense += dayExpense

                        buckets.add(
                            TrendBucket(
                                bucketLabel = currDate.format(dayFormatter),
                                startDate = currDate,
                                incomeEur = dayIncome,
                                expenseEur = dayExpense,
                                netCashFlowEur = dayIncome - dayExpense
                            )
                        )
                        currDate = currDate.plusDays(1)
                    }
                }
                TrendGranularity.WEEK -> {
                    var currDate = period.startDate
                    val weekFormatter = DateTimeFormatter.ofPattern("MMM d")
                    while (!currDate.isAfter(period.endDateInclusive)) {
                        val weekEnd = currDate.plusDays(6).coerceAtMost(period.endDateInclusive)
                        val startInst = currDate.atStartOfDay(period.zoneId).toInstant()
                        val endInst = weekEnd.plusDays(1).atStartOfDay(period.zoneId).toInstant()

                        val weekTxs = periodTxs.filter { tx ->
                            !tx.timestamp.isBefore(startInst) && tx.timestamp.isBefore(endInst)
                        }

                        var weekIncome = Money.zero(CurrencyCode.EUR)
                        var weekExpense = Money.zero(CurrencyCode.EUR)

                        weekTxs.forEach { tx ->
                            if (tx.type == TransactionType.INCOME) weekIncome += tx.baseValueEur
                            else weekExpense += tx.baseValueEur
                        }

                        totalIncome += weekIncome
                        totalExpense += weekExpense

                        buckets.add(
                            TrendBucket(
                                bucketLabel = currDate.format(weekFormatter),
                                startDate = currDate,
                                incomeEur = weekIncome,
                                expenseEur = weekExpense,
                                netCashFlowEur = weekIncome - weekExpense
                            )
                        )
                        currDate = currDate.plusDays(7)
                    }
                }
                TrendGranularity.MONTH -> {
                    var currDate = period.startDate.withDayOfMonth(1)
                    val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
                    while (!currDate.isAfter(period.endDateInclusive)) {
                        val nextMonth = currDate.plusMonths(1).withDayOfMonth(1)
                        val startInst = currDate.atStartOfDay(period.zoneId).toInstant()
                        val endInst = nextMonth.atStartOfDay(period.zoneId).toInstant()

                        val monthTxs = periodTxs.filter { tx ->
                            !tx.timestamp.isBefore(startInst) && tx.timestamp.isBefore(endInst)
                        }

                        var mIncome = Money.zero(CurrencyCode.EUR)
                        var mExpense = Money.zero(CurrencyCode.EUR)

                        monthTxs.forEach { tx ->
                            if (tx.type == TransactionType.INCOME) mIncome += tx.baseValueEur
                            else mExpense += tx.baseValueEur
                        }

                        totalIncome += mIncome
                        totalExpense += mExpense

                        buckets.add(
                            TrendBucket(
                                bucketLabel = currDate.format(monthFormatter),
                                startDate = currDate,
                                incomeEur = mIncome,
                                expenseEur = mExpense,
                                netCashFlowEur = mIncome - mExpense
                            )
                        )
                        currDate = nextMonth
                    }
                }
            }

            IncomeExpenseTrend(
                granularity = granularity,
                buckets = buckets,
                totalIncomeEur = totalIncome,
                totalExpenseEur = totalExpense
            )
        }
    }

    private fun LocalDate.coerceAtMost(other: LocalDate): LocalDate {
        return if (this.isAfter(other)) other else this
    }
}
