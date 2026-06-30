package com.expense.tracker.shared.core.presentation

import kotlin.math.abs
import kotlin.math.round

fun formatAmount(amount: Double): String {
    val absolute = abs(amount)
    val whole = absolute.toLong()
    val cents = round((absolute - whole) * 100).toInt()
    return "$${whole}.${cents.toString().padStart(2, '0')}"
}
