package com.expense.tracker.feature.recurring.impl

sealed interface RecurringListAction {
    data object Load : RecurringListAction
    data class TogglePause(val id: String) : RecurringListAction
    data class ShowDeleteDialog(val id: String) : RecurringListAction
    data object ConfirmDelete : RecurringListAction
    data object DismissDeleteDialog : RecurringListAction
}
