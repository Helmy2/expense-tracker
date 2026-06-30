package com.expense.tracker.shared.core.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class AmountFormatTest {

    @Test
    fun formatsZero() {
        assertEquals("$0.00", formatAmount(0.0))
    }

    @Test
    fun formatsWholeNumber() {
        assertEquals("$25.00", formatAmount(25.0))
    }

    @Test
    fun formatsPositiveWithCents() {
        assertEquals("$25.50", formatAmount(25.50))
    }

    @Test
    fun formatsNegativeAsAbsoluteWithSign() {
        // The formatter uses abs(), so -25.50 becomes "$25.50"
        assertEquals("$25.50", formatAmount(-25.50))
    }

    @Test
    fun formatsSingleDigitCents() {
        assertEquals("$25.05", formatAmount(25.05))
    }

    @Test
    fun formatsLargeNumber() {
        assertEquals("$1234.56", formatAmount(1234.56))
    }

    @Test
    fun formatsRoundingUp() {
        // 25.129 → round(12.9) → 13
        assertEquals("$25.13", formatAmount(25.129))
    }

    @Test
    fun formatsRoundingDown() {
        // 25.124 → round(12.4) → 12
        assertEquals("$25.12", formatAmount(25.124))
    }
}
