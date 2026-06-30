package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.presentation.MviViewModel

class RecurringListViewModel(
    private val repository: RecurringTemplateRepository,
    private val mapper: RecurringPresentationMapper,
) : MviViewModel<RecurringListState, RecurringListAction, RecurringListEvent>(
    initialState = RecurringListState(),
) {
    override suspend fun handleAction(action: RecurringListAction) {
        when (action) {
            is RecurringListAction.Load -> load()
            is RecurringListAction.TogglePause -> togglePause(action.id)
            is RecurringListAction.ShowDeleteDialog -> updateState {
                it.copy(deleteDialogTemplateId = action.id)
            }
            is RecurringListAction.ConfirmDelete -> confirmDelete()
            is RecurringListAction.DismissDeleteDialog -> updateState {
                it.copy(deleteDialogTemplateId = null)
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(contentState = RecurringListContentState.Loading) }

        when (val result = repository.loadTemplates()) {
            is Result.Success -> {
                val templates = result.value
                updateState { current ->
                    current.copy(
                        contentState = if (templates.isEmpty()) {
                            RecurringListContentState.Empty
                        } else {
                            RecurringListContentState.Content(templates.map(mapper::toTemplateUi))
                        },
                    )
                }
            }

            is Result.Failure -> updateState { current ->
                current.copy(contentState = RecurringListContentState.Error(result.error))
            }
        }
    }

    private suspend fun togglePause(id: String) {
        when (val result = repository.togglePause(id)) {
            is Result.Success -> load()
            is Result.Failure -> sendEvent(RecurringListEvent.Error(result.error.asMessageText()))
        }
    }

    private suspend fun confirmDelete() {
        val templateId = state.value.deleteDialogTemplateId ?: return
        updateState { it.copy(deleteDialogTemplateId = null) }

        when (repository.deleteTemplate(templateId)) {
            is Result.Success -> {
                load()
                sendEvent(RecurringListEvent.TemplateDeleted)
            }

            is Result.Failure -> {
                sendEvent(RecurringListEvent.Error(AppError.Unknown.asMessageText()))
            }
        }
    }
}
