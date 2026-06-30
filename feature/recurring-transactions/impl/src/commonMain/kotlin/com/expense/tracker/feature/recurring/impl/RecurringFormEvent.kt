package com.expense.tracker.feature.recurring.impl

sealed interface RecurringFormEvent {
    data object RecurringSaved : RecurringFormEvent
    data class Error(val message: String) : RecurringFormEvent
}
