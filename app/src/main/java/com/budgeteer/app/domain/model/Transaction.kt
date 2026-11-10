package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidAmountException
import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth

data class Transaction(
    val id: Long = 0L,
    val enteredAmount: Money,
    val type: TransactionType,
    val baseValueEur: Money,
    val exchangeRate: BigDecimal,
    val exchangeRateDate: String? = null,
    val description: String,
    val categoryId: Long,
    val timestamp: Instant,
    val period: YearMonth
) {
    init {
        if (!enteredAmount.isPositive) {
            throw InvalidAmountException("Transaction entered amount must be positive, got: ${enteredAmount.amount}")
        }
        if (baseValueEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Base value must be in EUR, got: ${baseValueEur.currency.code}")
        }
        if (baseValueEur.isNegative) {
            throw InvalidAmountException("Base value in EUR cannot be negative")
        }
        if (exchangeRate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be strictly positive, got: $exchangeRate")
        }
    }

    val signedBaseValueEur: Money
        get() = when (type) {
            TransactionType.INCOME -> baseValueEur
            TransactionType.EXPENSE -> -baseValueEur
        }
}
