package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.calculator.BudgetCalculator
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.MonthlySummary
import com.budgeteer.app.domain.model.Transaction
import javax.inject.Inject

/**
 * Calculates figures for a monthly summary period.
 */
class CalculateMonthlySummaryUseCase @Inject constructor() {

    operator fun invoke(
        monthlyBudget: MonthlyBudget,
        transactions: List<Transaction>
    ): MonthlySummary {
        return BudgetCalculator.createMonthlySummary(monthlyBudget, transactions)
    }
}
