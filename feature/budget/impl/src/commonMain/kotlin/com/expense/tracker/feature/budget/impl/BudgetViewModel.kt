package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.presentation.MviViewModel

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val timeProvider: TimeProvider,
    val mapper: BudgetPresentationMapper,
) : MviViewModel<BudgetState, BudgetAction, BudgetEvent>(
    initialState = BudgetState(),
) {
    override suspend fun handleAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.Load -> load()
            is BudgetAction.SetBudget -> setBudget(action.budgetId)
            is BudgetAction.CategorySelected -> updateState {
                it.copy(selectedCategory = action.category, categoryMenuExpanded = false)
            }
            is BudgetAction.ToggleCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = !it.categoryMenuExpanded)
            }
            is BudgetAction.DismissCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = false)
            }
            is BudgetAction.LimitChanged -> updateState {
                it.copy(limitText = action.value)
            }
            is BudgetAction.SaveBudget -> saveBudget()
            is BudgetAction.DeleteBudget -> updateState {
                it.copy(deleteTargetId = action.id)
            }
            is BudgetAction.ConfirmDelete -> deleteBudget()
            is BudgetAction.CancelDelete -> updateState {
                it.copy(deleteTargetId = null)
            }
        }
    }

    private suspend fun setBudget(budgetId: String?) {
        if (budgetId == null) {
            updateState {
                it.copy(
                    formMode = BudgetFormMode.Create,
                    editingBudgetId = null,
                    selectedCategory = ExpenseCategory.OTHER_EXPENSE,
                    limitText = "",
                )
            }
        } else {
            updateState { it.copy(contentState = BudgetContentState.Loading) }
            when (val result = budgetRepository.loadBudgetById(budgetId)) {
                is Result.Success -> {
                    val budget = result.value
                    if (budget != null) {
                        updateState {
                            it.copy(
                                formMode = BudgetFormMode.Edit,
                                editingBudgetId = budget.id,
                                selectedCategory = budget.category,
                                limitText = budget.monthlyLimit.toString(),
                            )
                        }
                    } else {
                        updateState {
                            it.copy(contentState = BudgetContentState.Error(com.expense.tracker.shared.core.domain.AppError.Unknown))
                        }
                    }
                }
                is Result.Failure -> updateState {
                    it.copy(contentState = BudgetContentState.Error(result.error))
                }
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(contentState = BudgetContentState.Loading) }

        when (val result = budgetRepository.loadBudgetsWithSpending()) {
            is Result.Success -> {
                val budgetsWithSpending = result.value
                if (budgetsWithSpending.isEmpty()) {
                    updateState { it.copy(contentState = BudgetContentState.Empty) }
                } else {
                    updateState {
                        it.copy(contentState = BudgetContentState.Content(
                            budgetsWithSpending.map(mapper::toBudgetWithSpendingUi)
                        ))
                    }
                }
            }
            is Result.Failure -> updateState {
                it.copy(contentState = BudgetContentState.Error(result.error))
            }
        }
    }

    private suspend fun saveBudget() {
        val currentState = state.value
        val limit = currentState.limitText.toDoubleOrNull()

        if (limit == null || limit <= 0) {
            return
        }

        when (currentState.formMode) {
            BudgetFormMode.Create -> {
                when (val result = budgetRepository.createBudget(
                    category = currentState.selectedCategory,
                    monthlyLimit = limit,
                )) {
                    is Result.Success -> {
                        sendEvent(BudgetEvent.BudgetSaved)
                    }
                    is Result.Failure -> {
                        sendEvent(BudgetEvent.Error(result.error.asMessageText()))
                    }
                }
            }
            BudgetFormMode.Edit -> {
                val budgetId = currentState.editingBudgetId ?: return
                when (val result = budgetRepository.updateBudget(
                    id = budgetId,
                    monthlyLimit = limit,
                )) {
                    is Result.Success -> {
                        sendEvent(BudgetEvent.BudgetSaved)
                    }
                    is Result.Failure -> {
                        sendEvent(BudgetEvent.Error(result.error.asMessageText()))
                    }
                }
            }
        }
    }

    private suspend fun deleteBudget() {
        val budgetId = state.value.deleteTargetId ?: return

        when (val result = budgetRepository.deleteBudget(budgetId)) {
            is Result.Success -> {
                updateState { it.copy(deleteTargetId = null) }
                load()
                sendEvent(BudgetEvent.BudgetDeleted)
            }
            is Result.Failure -> {
                updateState { it.copy(deleteTargetId = null) }
                sendEvent(BudgetEvent.Error(result.error.asMessageText()))
            }
        }
    }
}


