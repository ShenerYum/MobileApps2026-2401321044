package com.budgeteer.app.domain.calculator

import com.budgeteer.app.domain.exception.CurrencyMismatchException
import com.budgeteer.app.domain.exception.PeriodMismatchException
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.MonthlyBudget
import com.budgeteer.app.domain.model.MonthlySummary
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.model.TransactionType
import java.time.YearMonth

/**
 * Deterministic, side-effect-free calculator for monthly budget calculations.
 */
object BudgetCalculator {

    fun calculateTotalIncome(transactions: List<Transaction>, period: YearMonth): Money {
        val incomeTxs = filterAndValidateTxs(transactions, period).filter { it.type == TransactionType.INCOME }
        return incomeTxs.fold(Money.zero(CurrencyCode.EUR)) { acc, tx -> acc + tx.baseValueEur }
    }

    fun calculateTotalExpenses(transactions: List<Transaction>, period: YearMonth): Money {
        val expenseTxs = filterAndValidateTxs(transactions, period).filter { it.type == TransactionType.EXPENSE }
        return expenseTxs.fold(Money.zero(CurrencyCode.EUR)) { acc, tx -> acc + tx.baseValueEur }
    }

    fun calculateCurrentBudget(
        baseBudgetEur: Money,
        totalIncomeEur: Money,
        totalExpensesEur: Money
    ): Money {
        require(baseBudgetEur.currency == CurrencyCode.EUR)
        require(totalIncomeEur.currency == CurrencyCode.EUR)
        require(totalExpensesEur.currency == CurrencyCode.EUR)
        return baseBudgetEur + totalIncomeEur - totalExpensesEur
    }

    fun calculateSavedAmount(currentBudgetEur: Money, baseBudgetEur: Money): Money {
        require(currentBudgetEur.currency == CurrencyCode.EUR)
        require(baseBudgetEur.currency == CurrencyCode.EUR)
        return currentBudgetEur - baseBudgetEur
    }

    /**
     * Confirmed budget change formula:
     * newCurrentBudget = currentBudget + (newBaseBudget - oldBaseBudget)
     */
    fun calculateNewCurrentBudget(
        currentBudgetEur: Money,
        oldBaseBudgetEur: Money,
        newBaseBudgetEur: Money
    ): Money {
        require(currentBudgetEur.currency == CurrencyCode.EUR)
        require(oldBaseBudgetEur.currency == CurrencyCode.EUR)
        require(newBaseBudgetEur.currency == CurrencyCode.EUR)
        val delta = newBaseBudgetEur - oldBaseBudgetEur
        return currentBudgetEur + delta
    }

    fun createMonthlySummary(
        monthlyBudget: MonthlyBudget,
        transactions: List<Transaction>
    ): MonthlySummary {
        val period = monthlyBudget.period
        val baseBudget = monthlyBudget.baseBudgetEur
        val totalIncome = calculateTotalIncome(transactions, period)
        val totalExpenses = calculateTotalExpenses(transactions, period)
        val currentBudget = calculateCurrentBudget(baseBudget, totalIncome, totalExpenses)
        val savedAmount = calculateSavedAmount(currentBudget, baseBudget)

        return MonthlySummary(
            period = period,
            baseBudgetEur = baseBudget,
            totalIncomeEur = totalIncome,
            totalExpensesEur = totalExpenses,
            currentBudgetEur = currentBudget,
            savedAmountEur = savedAmount
        )
    }

    private fun filterAndValidateTxs(transactions: List<Transaction>, period: YearMonth): List<Transaction> {
        transactions.forEach { tx ->
            if (tx.period != period) {
                throw PeriodMismatchException("Transaction period ${tx.period} does not match summary period $period")
            }
            if (tx.baseValueEur.currency != CurrencyCode.EUR) {
                throw CurrencyMismatchException("Transaction base value must be in EUR, got ${tx.baseValueEur.currency.code}")
            }
        }
        return transactions
    }
}
