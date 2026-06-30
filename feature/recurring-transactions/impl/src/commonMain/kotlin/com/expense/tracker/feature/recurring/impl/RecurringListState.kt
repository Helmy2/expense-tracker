package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.shared.core.domain.AppError

data class RecurringListState(
    val contentState: RecurringListContentState = RecurringListContentState.Loading,
    val deleteDialogTemplateId: String? = null,
)

sealed interface RecurringListContentState {
    data object Loading : RecurringListContentState
    data object Empty : RecurringListContentState
    data class Content(val templates: List<RecurringTemplateUi>) : RecurringListContentState
    data class Error(val error: AppError) : RecurringListContentState
}
