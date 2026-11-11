package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import java.time.YearMonth

data class SavingsTransfer(
    val id: Long = 0L,
    val period: YearMonth,
    val savedAmountEur: Money
) {
    init {
        if (savedAmountEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Savings transfer must be in EUR, got: ${savedAmountEur.currency.code}")
        }
    }
}
