package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.InvalidCurrencyCodeException
import java.util.Currency
import java.util.Locale

/**
 * Value object representing an ISO 4217 currency code.
 */
data class CurrencyCode(val value: String) {
    val code: String = value.trim().uppercase(Locale.ROOT)

    init {
        if (code.length != 3 || !code.all { it.isLetter() }) {
            throw InvalidCurrencyCodeException("Currency code must consist of exactly 3 alphabetic characters: '$value'")
        }
    }

    val defaultFractionDigits: Int
        get() {
            return try {
                val currency = Currency.getInstance(code)
                val digits = currency.defaultFractionDigits
                if (digits >= 0) digits else 2
            } catch (e: IllegalArgumentException) {
                2 // Default fallback for custom 3-letter currency codes
            }
        }

    companion object {
        val EUR = CurrencyCode("EUR")
        val USD = CurrencyCode("USD")
        val BGN = CurrencyCode("BGN")
        val JPY = CurrencyCode("JPY")
        val BHD = CurrencyCode("BHD")
    }
}
