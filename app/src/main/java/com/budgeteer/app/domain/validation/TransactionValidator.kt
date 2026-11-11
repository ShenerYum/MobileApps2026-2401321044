package com.budgeteer.app.domain.validation

import com.budgeteer.app.domain.exception.CategoryTypeMismatchException
import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.InvalidAmountException
import com.budgeteer.app.domain.exception.InvalidExchangeRateException
import com.budgeteer.app.domain.exception.PeriodMismatchException
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import java.math.BigDecimal
import java.time.YearMonth
import java.time.ZoneId

object TransactionValidator {

    fun validate(
        transaction: Transaction,
        category: Category,
        zoneId: ZoneId = ZoneId.of("UTC")
    ) {
        if (!transaction.enteredAmount.isPositive) {
            throw InvalidAmountException("Entered transaction amount must be positive")
        }

        if (transaction.baseValueEur.currency != CurrencyCode.EUR) {
            throw CurrencyMismatchException("Base value must be in EUR, got ${transaction.baseValueEur.currency.code}")
        }

        if (transaction.baseValueEur.isNegative) {
            throw InvalidAmountException("Base value in EUR cannot be negative")
        }

        if (transaction.exchangeRate <= BigDecimal.ZERO) {
            throw InvalidExchangeRateException("Exchange rate must be positive, got ${transaction.exchangeRate}")
        }

        val expectedCategoryType = when (transaction.type) {
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.EXPENSE -> CategoryType.EXPENSE
        }

        if (category.type != expectedCategoryType) {
            throw CategoryTypeMismatchException(
                "Transaction type ${transaction.type} cannot be paired with category type ${category.type}"
            )
        }

        val derivedPeriod = YearMonth.from(transaction.timestamp.atZone(zoneId))
        if (derivedPeriod != transaction.period) {
            throw PeriodMismatchException(
                "Transaction timestamp period ($derivedPeriod) does not match assigned period (${transaction.period})"
            )
        }
    }
}
