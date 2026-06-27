package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.shared.core.domain.TimeProvider

class ExpensePresentationMapper(
    private val timeProvider: TimeProvider,
) {
    fun formatDate(millis: Long): String = timeProvider.formatDate(millis)

    fun formatAmount(amount: Double, isIncome: Boolean): String {
        val prefix = if (isIncome) "+" else "-"
        val abs = kotlin.math.abs(amount)
        val whole = abs.toLong()
        val frac = ((abs - whole) * 100).toInt()
        return "$prefix $${whole}.${frac.toString().padStart(2, '0')}"
    }
}
