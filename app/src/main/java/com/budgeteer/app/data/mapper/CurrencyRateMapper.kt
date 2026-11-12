package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.CurrencyRateEntity
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

fun CurrencyRateEntity.toDomain(): CurrencyRate {
    return CurrencyRate(
        id = id,
        baseCurrency = CurrencyCode(baseCurrency),
        quoteCurrency = CurrencyCode(quoteCurrency),
        rate = BigDecimal.valueOf(rate),
        rateDate = LocalDate.parse(rateDate),
        provider = provider,
        fetchedAt = Instant.ofEpochMilli(fetchedAtEpochMs)
    )
}

fun CurrencyRate.toEntity(): CurrencyRateEntity {
    return CurrencyRateEntity(
        id = id,
        baseCurrency = baseCurrency.code,
        quoteCurrency = quoteCurrency.code,
        rate = rate.toDouble(),
        rateDate = rateDate.toString(),
        provider = provider,
        fetchedAtEpochMs = fetchedAt.toEpochMilli()
    )
}
