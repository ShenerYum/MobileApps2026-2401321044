package com.budgeteer.app.domain.calculator

import com.budgeteer.app.domain.model.Money
import com.budgeteer.app.domain.model.SavingsTransfer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class SavingsCalculatorTest {

    @Test
    fun testEmptyTransferListGivesZero() {
        val total = SavingsCalculator.calculateTotalSavingsBalance(emptyList())
        assertEquals(BigDecimal("0"), total.amount)
    }

    @Test
    fun testPositiveZeroAndNegativeTransfers() {
        val transfers = listOf(
            SavingsTransfer(1L, YearMonth.of(2026, 1), Money.eur(BigDecimal("500.00"))),
            SavingsTransfer(2L, YearMonth.of(2026, 2), Money.eur(BigDecimal("0.00"))),
            SavingsTransfer(3L, YearMonth.of(2026, 3), Money.eur(BigDecimal("-200.00")))
        )

        val total = SavingsCalculator.calculateTotalSavingsBalance(transfers)
        assertEquals(BigDecimal("300.00"), total.amount)
    }
}
