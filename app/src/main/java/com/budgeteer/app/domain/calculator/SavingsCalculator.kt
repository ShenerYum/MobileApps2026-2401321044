package com.budgeteer.app.domain.calculator

import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer

/**
 * Deterministic calculator for savings balance calculations.
 */
object SavingsCalculator {

    fun calculateTotalSavingsBalance(transfers: List<SavingsTransfer>): Money {
        return transfers.fold(Money.zero(CurrencyCode.EUR)) { acc, transfer ->
            require(transfer.savedAmountEur.currency == CurrencyCode.EUR) {
                "Savings transfer must be in EUR, got: ${transfer.savedAmountEur.currency.code}"
            }
            acc + transfer.savedAmountEur
        }
    }
}
