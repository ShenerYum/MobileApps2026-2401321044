package com.budgeteer.app.domain.model

import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class CurrencyRate(
    val id: Long = 0L,
    val baseCurrency: CurrencyCode,
    val quoteCurrency: CurrencyCode,
    val rate: BigDecimal,
    val rateDate: LocalDate,
    val provider: String,
    val fetchedAt: Instant
) {
    init {
        if (rate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be strictly positive, got: $rate")
        }
    }
}
