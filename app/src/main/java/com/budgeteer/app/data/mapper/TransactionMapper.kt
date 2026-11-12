package com.budgeteer.app.data.mapper

import com.budgeteer.app.data.local.entity.TransactionEntity
import com.budgeteer.app.data.local.entity.TransactionType as EntityTransactionType
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType as DomainTransactionType
import java.time.Instant
import java.time.YearMonth

fun TransactionEntity.toDomain(): Transaction {
    val currencyCode = CurrencyCode(originalCurrencyCode)
    return Transaction(
        id = id,
        enteredAmount = Money.fromMinorUnits(enteredAmountMinor, currencyCode),
        type = type.toDomain(),
        baseValueEur = Money.eurFromMinor(baseValueEurMinor),
        exchangeRate = exchangeRate.toBigDecimal(),
        exchangeRateDate = exchangeRateDate,
        description = description,
        categoryId = categoryId,
        timestamp = Instant.ofEpochMilli(transactionTimestampEpochMs),
        period = YearMonth.of(year, month)
    )
}

fun Transaction.toEntity(createdAtEpochMs: Long = System.currentTimeMillis()): TransactionEntity {
    return TransactionEntity(
        id = id,
        enteredAmountMinor = enteredAmount.toMinorUnits(),
        originalCurrencyCode = enteredAmount.currency.code,
        type = type.toEntity(),
        baseValueEurMinor = baseValueEur.toMinorUnits(),
        exchangeRate = exchangeRate.toDouble(),
        exchangeRateDate = exchangeRateDate,
        description = description,
        categoryId = categoryId,
        year = period.year,
        month = period.monthValue,
        transactionTimestampEpochMs = timestamp.toEpochMilli(),
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = System.currentTimeMillis()
    )
}

fun EntityTransactionType.toDomain(): DomainTransactionType {
    return when (this) {
        EntityTransactionType.INCOME -> DomainTransactionType.INCOME
        EntityTransactionType.EXPENSE -> DomainTransactionType.EXPENSE
    }
}

fun DomainTransactionType.toEntity(): EntityTransactionType {
    return when (this) {
        DomainTransactionType.INCOME -> EntityTransactionType.INCOME
        DomainTransactionType.EXPENSE -> EntityTransactionType.EXPENSE
    }
}
