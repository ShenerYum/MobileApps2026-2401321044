package com.budgeteer.app.domain.usecase.analytics

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.TransactionType
import com.budgeteer.app.domain.model.analytics.AnalyticsPeriod
import com.budgeteer.app.domain.model.analytics.CategoryBreakdown
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetCategoryBreakdownUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(period: AnalyticsPeriod): Flow<CategoryBreakdown> {
        return combine(
            transactionRepository.observeAllTransactions(),
            categoryRepository.observeAllActiveCategories()
        ) { allTxs, categories ->
            val periodTxs = allTxs.filter { tx ->
                val txInstant = tx.timestamp
                !txInstant.isBefore(period.startInstant) &&
                        txInstant.isBefore(period.endInstantExclusive) &&
                        tx.type == TransactionType.EXPENSE
            }

            val categoryMap = categories.associateBy { it.id }
            val totalsMap = mutableMapOf<Long, Money>()
            val countsMap = mutableMapOf<Long, Int>()

            periodTxs.forEach { tx ->
                val catId = tx.categoryId
                val currentSpent = totalsMap[catId] ?: Money.zero(CurrencyCode.EUR)
                totalsMap[catId] = currentSpent + tx.baseValueEur
                countsMap[catId] = (countsMap[catId] ?: 0) + 1
            }

            CategoryBreakdown.create(
                itemsWithTotals = totalsMap.toList(),
                categoryMap = categoryMap,
                transactionCounts = countsMap
            )
        }
    }
}
