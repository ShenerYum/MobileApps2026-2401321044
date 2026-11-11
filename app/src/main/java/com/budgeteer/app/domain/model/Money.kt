package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Immutable value object representing a monetary value with an explicit currency.
 * Financial arithmetic uses exact [BigDecimal] calculations.
 */
data class Money(
    val amount: BigDecimal,
    val currency: CurrencyCode
) : Comparable<Money> {

    val isZero: Boolean get() = amount.compareTo(BigDecimal.ZERO) == 0
    val isPositive: Boolean get() = amount > BigDecimal.ZERO
    val isNegative: Boolean get() = amount < BigDecimal.ZERO

    operator fun plus(other: Money): Money {
        ensureSameCurrency(other)
        return Money(amount.add(other.amount), currency)
    }

    operator fun minus(other: Money): Money {
        ensureSameCurrency(other)
        return Money(amount.subtract(other.amount), currency)
    }

    operator fun unaryMinus(): Money = Money(amount.negate(), currency)

    override fun compareTo(other: Money): Int {
        ensureSameCurrency(other)
        return amount.compareTo(other.amount)
    }

    fun toMinorUnits(): Long {
        val scale = currency.defaultFractionDigits
        return amount.setScale(scale, RoundingMode.HALF_EVEN)
            .movePointRight(scale)
            .longValueExact()
    }

    private fun ensureSameCurrency(other: Money) {
        if (currency != other.currency) {
            throw CurrencyMismatchException(
                "Cannot perform arithmetic on different currencies: ${currency.code} and ${other.currency.code}"
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        return currency == other.currency && amount.compareTo(other.amount) == 0
    }

    override fun hashCode(): Int {
        var result = amount.stripTrailingZeros().hashCode()
        result = 31 * result + currency.hashCode()
        return result
    }

    companion object {
        fun zero(currency: CurrencyCode): Money = Money(BigDecimal.ZERO, currency)

        fun fromMinorUnits(minorUnits: Long, currency: CurrencyCode): Money {
            val scale = currency.defaultFractionDigits
            val bigDecimal = BigDecimal.valueOf(minorUnits).movePointLeft(scale)
            return Money(bigDecimal, currency)
        }

        fun eur(amount: BigDecimal): Money = Money(amount, CurrencyCode.EUR)
        fun eurFromMinor(minorUnits: Long): Money = fromMinorUnits(minorUnits, CurrencyCode.EUR)
    }
}
