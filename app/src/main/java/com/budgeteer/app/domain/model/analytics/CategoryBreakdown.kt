package com.budgeteer.app.domain.model.analytics

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

data class CategorySpendingItem(
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val categoryIconIdentifier: String,
    val totalAmountSpentEur: Money,
    val percentageOfTotalExpenses: BigDecimal,
    val transactionCount: Int
)

data class CategoryBreakdown(
    val totalExpensesEur: Money,
    val items: List<CategorySpendingItem>
) {
    companion object {
        fun create(
            itemsWithTotals: List<Pair<Long, Money>>,
            categoryMap: Map<Long, Category>,
            transactionCounts: Map<Long, Int>
        ): CategoryBreakdown {
            val totalExpense = itemsWithTotals.fold(Money.zero(CurrencyCode.EUR)) { acc, pair -> acc + pair.second }
            val totalAmount = totalExpense.amount

            val spendingItems = itemsWithTotals.map { (catId, moneySpent) ->
                val category = categoryMap[catId]
                val name = category?.name ?: "Uncategorized"
                val color = category?.colorHex ?: "#888888"
                val icon = category?.iconIdentifier ?: "ic_folder"
                val count = transactionCounts[catId] ?: 0

                val pct = if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    moneySpent.amount.multiply(BigDecimal("100"))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP)
                }

                CategorySpendingItem(
                    categoryId = catId,
                    categoryName = name,
                    categoryColorHex = color,
                    categoryIconIdentifier = icon,
                    totalAmountSpentEur = moneySpent,
                    percentageOfTotalExpenses = pct,
                    transactionCount = count
                )
            }.sortedByDescending { it.totalAmountSpentEur.amount }

            return CategoryBreakdown(
                totalExpensesEur = totalExpense,
                items = spendingItems
            )
        }
    }
}
