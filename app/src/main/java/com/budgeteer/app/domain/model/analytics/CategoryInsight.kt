package com.budgeteer.app.domain.model.analytics

import com.budgeteer.app.domain.model.Money
import java.math.BigDecimal

sealed class CategoryInsight {
    abstract val title: String
    abstract val description: String

    data class TopSpendingCategory(
        val categoryName: String,
        val amountSpentEur: Money,
        val percentageOfTotal: BigDecimal
    ) : CategoryInsight() {
        override val title: String = "Top Spending Category"
        override val description: String = "$categoryName is your highest expense, accounting for $percentageOfTotal% (${amountSpentEur.amount} EUR) of overall spending."
    }

    data class LargestExpenseIncrease(
        val categoryName: String,
        val increaseAmountEur: Money,
        val percentageIncrease: BigDecimal?
    ) : CategoryInsight() {
        override val title: String = "Largest Expense Increase"
        override val description: String = "Spending in $categoryName increased by ${increaseAmountEur.amount} EUR compared to the previous period."
    }

    data class LargestExpenseDecrease(
        val categoryName: String,
        val decreaseAmountEur: Money
    ) : CategoryInsight() {
        override val title: String = "Largest Expense Reduction"
        override val description: String = "Spending in $categoryName decreased by ${decreaseAmountEur.amount} EUR compared to the previous period."
    }

    data class NewCategorySpending(
        val categoryName: String,
        val amountSpentEur: Money
    ) : CategoryInsight() {
        override val title: String = "New Spending Category"
        override val description: String = "New activity observed in $categoryName (${amountSpentEur.amount} EUR) for this period."
    }
}
