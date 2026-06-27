package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.computeDashboard
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.presentation.MviViewModel

class ExpenseViewModel(
    private val repository: TransactionRepository,
    private val timeProvider: TimeProvider,
) : MviViewModel<ExpenseState, ExpenseAction, ExpenseEvent>(
    initialState = ExpenseState(),
) {
    override suspend fun handleAction(action: ExpenseAction) {
        when (action) {
            is ExpenseAction.Load -> load()
            is ExpenseAction.AmountChanged -> updateState { it.copy(amountText = action.value) }
            is ExpenseAction.TypeSelected -> updateState { it.copy(selectedType = action.type) }
            is ExpenseAction.CategorySelected -> updateState {
                it.copy(selectedCategory = action.category, categoryMenuExpanded = false)
            }
            is ExpenseAction.NoteChanged -> updateState { it.copy(noteText = action.value) }
            is ExpenseAction.ToggleCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = !it.categoryMenuExpanded)
            }
            is ExpenseAction.DismissCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = false)
            }
            is ExpenseAction.SaveTransaction -> saveTransaction()
            is ExpenseAction.DeleteTransaction -> deleteTransaction(action.id)
            is ExpenseAction.ConfirmDelete -> { /* unused - confirmation is local in UI */ }
            is ExpenseAction.CancelDelete -> { /* unused - confirmation is local in UI */ }
            is ExpenseAction.ToggleFormSheet -> updateState {
                it.copy(showBottomSheet = !it.showBottomSheet)
            }
            is ExpenseAction.DismissFormSheet -> updateState {
                it.copy(showBottomSheet = false)
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(contentState = ExpenseContentState.Loading) }

        when (val result = repository.loadTransactions()) {
            is Result.Success -> {
                val transactions = result.value
                updateState { current ->
                    current.copy(
                        contentState = if (transactions.isEmpty()) {
                            ExpenseContentState.Empty
                        } else {
                            ExpenseContentState.Content(transactions)
                        },
                        dashboard = computeDashboard(transactions),
                    )
                }
            }

            is Result.Failure -> updateState { current ->
                current.copy(contentState = ExpenseContentState.Error(result.error))
            }
        }
    }

    private suspend fun saveTransaction() {
        val currentState = state.value
        val amount = currentState.amountText.toDoubleOrNull()
        val isAmountValid = amount != null && amount > 0

        if (!isAmountValid) {
            return
        }

        when (val result = repository.addTransaction(
            amount = amount,
            type = currentState.selectedType,
            category = currentState.selectedCategory,
            note = currentState.noteText.trim(),
        )) {
            is Result.Success -> {
                updateState { current ->
                    current.copy(
                        amountText = "",
                        noteText = "",
                        selectedType = com.expense.tracker.feature.expense.domain.model.TransactionType.EXPENSE,
                        selectedCategory = com.expense.tracker.feature.expense.domain.model.TransactionCategory.OTHER,
                        showBottomSheet = false,
                    )
                }
                load()
                sendEvent(ExpenseEvent.TransactionSaved)
            }

            is Result.Failure -> {
                sendEvent(ExpenseEvent.Error(result.error.messageText()))
            }
        }
    }

    private suspend fun deleteTransaction(id: String) {
        when (val result = repository.deleteTransaction(id)) {
            is Result.Success -> {
                load()
            }

            is Result.Failure -> {
                sendEvent(ExpenseEvent.Error(result.error.messageText()))
            }
        }
    }
}

private fun AppError.messageText(): String = when (this) {
    AppError.Unknown -> "Something went wrong"
    is AppError.Message -> value
}
