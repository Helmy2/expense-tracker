package com.expense.tracker.feature.recurring.impl

sealed interface RecurringListEvent {
    data object TemplateDeleted : RecurringListEvent
    data class Error(val message: String) : RecurringListEvent
}
