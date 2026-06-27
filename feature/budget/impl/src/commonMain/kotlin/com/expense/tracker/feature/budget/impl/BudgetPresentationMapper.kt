package com.expense.tracker.feature.budget.impl

import com.expense.tracker.shared.core.domain.TimeProvider

class BudgetPresentationMapper(
    private val timeProvider: TimeProvider,
) {
    fun formatDate(millis: Long): String = timeProvider.formatDate(millis)

    fun formatAmount(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        val whole = abs.toLong()
        val frac = ((abs - whole) * 100).toInt()
        return "$${whole}.${frac.toString().padStart(2, '0')}"
    }

    fun formatAmountWithSign(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        val whole = abs.toLong()
        val frac = ((abs - whole) * 100).toInt()
        return "$${whole}.${frac.toString().padStart(2, '0')}"
    }
}
