package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.calculator.BudgetCalculator
import com.budgeteer.app.domain.validation.BudgetValidator
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import javax.inject.Inject

/**
 * Calculates the new current budget after changing a monthly base budget using formula:
 * newCurrentBudget = currentBudget + (newBaseBudget - oldBaseBudget)
 */
class ChangeMonthlyBudgetUseCase @Inject constructor() {

    operator fun invoke(
        currentBudgetEur: Money,
        oldBaseBudgetEur: Money,
        newBaseBudgetEur: Money
    ): Money {
        BudgetValidator.validate(MonthlyBudget(period = java.time.YearMonth.now(), baseBudgetEur = newBaseBudgetEur))
        return BudgetCalculator.calculateNewCurrentBudget(currentBudgetEur, oldBaseBudgetEur, newBaseBudgetEur)
    }
}
