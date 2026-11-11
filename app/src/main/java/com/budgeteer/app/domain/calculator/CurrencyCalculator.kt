package com.budgeteer.app.domain.calculator

import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Deterministic calculator for currency conversions using exact BigDecimal arithmetic.
 */
object CurrencyCalculator {

    /**
     * Converts a Money amount in [sourceMoney] to [targetCurrency] using [rate].
     * Rate convention: 1 baseUnit (source) = [rate] quoteUnits (target).
     * [quote amount = base amount * rate]
     */
    fun convert(
        sourceMoney: Money,
        targetCurrency: CurrencyCode,
        rate: BigDecimal,
        roundingMode: RoundingMode = RoundingMode.HALF_EVEN
    ): Money {
        if (sourceMoney.currency == targetCurrency) {
            return sourceMoney
        }
        if (rate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be strictly positive, got $rate")
        }

        val targetScale = targetCurrency.defaultFractionDigits
        val convertedAmount = sourceMoney.amount.multiply(rate)
            .setScale(targetScale, roundingMode)

        return Money(convertedAmount, targetCurrency)
    }

    /**
     * Converts a transaction entered amount in original currency to EUR.
     * Rate convention: 1 EUR = [eurToOriginalRate] OriginalCurrency.
     * EUR amount = originalAmount / eurToOriginalRate
     */
    fun convertToEur(
        enteredMoney: Money,
        eurToOriginalRate: BigDecimal,
        roundingMode: RoundingMode = RoundingMode.HALF_EVEN
    ): Money {
        if (enteredMoney.currency == CurrencyCode.EUR) {
            return enteredMoney
        }
        if (eurToOriginalRate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be strictly positive, got $eurToOriginalRate")
        }

        val eurScale = CurrencyCode.EUR.defaultFractionDigits
        val eurAmount = enteredMoney.amount.divide(eurToOriginalRate, eurScale + 4, roundingMode)
            .setScale(eurScale, roundingMode)

        return Money(eurAmount, CurrencyCode.EUR)
    }
}
