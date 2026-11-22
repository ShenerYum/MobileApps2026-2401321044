package com.budgeteer.app.presentation.common.formatting

import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.presentation.common.state.MoneyUiModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {

    fun format(
        money: Money,
        locale: Locale = Locale.getDefault(),
        showExplicitPlusSign: Boolean = false
    ): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
            try {
                currency = Currency.getInstance(money.currency.code)
            } catch (e: Exception) {
                // Fallback for custom 3-letter currency codes
            }
            minimumFractionDigits = money.currency.defaultFractionDigits
            maximumFractionDigits = money.currency.defaultFractionDigits
        }

        val formattedAmount = numberFormat.format(money.amount.abs())

        return when {
            money.isNegative -> "-$formattedAmount"
            money.isPositive && showExplicitPlusSign -> "+$formattedAmount"
            else -> formattedAmount
        }
    }

    fun toUiModel(
        money: Money,
        locale: Locale = Locale.getDefault(),
        showExplicitPlusSign: Boolean = false
    ): MoneyUiModel {
        return MoneyUiModel(
            rawMoney = money,
            formattedText = format(money, locale, showExplicitPlusSign),
            isNegative = money.isNegative,
            isPositive = money.isPositive,
            isZero = money.isZero
        )
    }
}
