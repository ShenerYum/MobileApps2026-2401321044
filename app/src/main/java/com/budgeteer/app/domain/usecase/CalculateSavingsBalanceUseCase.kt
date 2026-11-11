package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.calculator.SavingsCalculator
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer
import javax.inject.Inject

/**
 * Calculates total savings balance across monthly savings transfers.
 */
class CalculateSavingsBalanceUseCase @Inject constructor() {

    operator fun invoke(transfers: List<SavingsTransfer>): Money {
        return SavingsCalculator.calculateTotalSavingsBalance(transfers)
    }
}
