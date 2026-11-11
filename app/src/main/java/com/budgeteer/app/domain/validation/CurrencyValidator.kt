package com.budgeteer.app.domain.validation

import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import com.budgeteer.app.domain.model.CurrencyRate
import java.math.BigDecimal

object CurrencyValidator {

    fun validateRate(rate: CurrencyRate) {
        if (rate.baseCurrency == rate.quoteCurrency && rate.rate.compareTo(BigDecimal.ONE) != 0) {
            throw InvalidExchangeRateException("Same-currency rate must be 1.0, got ${rate.rate}")
        }
        if (rate.rate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be strictly positive, got ${rate.rate}")
        }
    }
}
