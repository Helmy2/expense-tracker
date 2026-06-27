package com.expense.tracker.feature.expense.impl

sealed interface ExpenseEvent {
    data object TransactionSaved : ExpenseEvent
    data class Error(val message: String) : ExpenseEvent
}
