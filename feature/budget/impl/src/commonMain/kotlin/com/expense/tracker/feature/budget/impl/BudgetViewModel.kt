package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.budget.domain.model.withSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.YearMonth
import com.expense.tracker.shared.core.presentation.MviViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider,
) : MviViewModel<BudgetState, BudgetAction, BudgetEvent>(
    initialState = BudgetState(),
) {
    override suspend fun handleAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.Load -> load()
            is BudgetAction.ToggleFormSheet -> updateState {
                it.copy(showFormSheet = !it.showFormSheet)
            }
            is BudgetAction.DismissFormSheet -> updateState {
                it.copy(
                    showFormSheet = false,
                    formMode = BudgetFormMode.Create,
                    editingBudgetId = null,
                    limitText = "",
                    selectedCategory = TransactionCategory.OTHER,
                )
            }
            is BudgetAction.StartCreate -> updateState {
                it.copy(
                    showFormSheet = true,
                    formMode = BudgetFormMode.Create,
                    editingBudgetId = null,
                    selectedCategory = TransactionCategory.OTHER,
                    limitText = "",
                    availableCategories = action.availableCategories,
                )
            }
            is BudgetAction.StartEdit -> updateState {
                it.copy(
                    showFormSheet = true,
                    formMode = BudgetFormMode.Edit,
                    editingBudgetId = action.budgetId,
                    selectedCategory = action.category,
                    limitText = action.currentLimit.toString(),
                    availableCategories = emptyList(),
                )
            }
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

    private suspend fun load() {
        updateState { it.copy(contentState = BudgetContentState.Loading) }

        when (val result = budgetRepository.loadBudgets()) {
            is Result.Success -> {
                val budgets = result.value
                if (budgets.isEmpty()) {
                    updateState { it.copy(contentState = BudgetContentState.Empty) }
                } else {
                    val budgetsWithSpending = calculateSpendingForBudgets(budgets)
                    updateState {
                        it.copy(contentState = BudgetContentState.Content(budgetsWithSpending))
                    }
                }
            }
            is Result.Failure -> updateState {
                it.copy(contentState = BudgetContentState.Error(result.error))
            }
        }
    }

    private suspend fun calculateSpendingForBudgets(
        budgets: List<Budget>,
    ): List<BudgetWithSpending> {
        val transactions = when (val result = transactionRepository.loadTransactions()) {
            is Result.Success -> result.value
            is Result.Failure -> emptyList()
        }

        val yearMonth = timeProvider.currentYearMonth()

        return budgets.map { budget ->
            val spent = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { it.category == budget.category }
                .filter {
                    val localDateTime = Instant.fromEpochMilliseconds(it.createdAtMillis)
                        .toLocalDateTime(timeProvider.timeZone())
                    val txYearMonth = YearMonth(
                        year = localDateTime.year,
                        month = localDateTime.monthNumber,
                    )
                    txYearMonth == yearMonth
                }
                .sumOf { it.amount }

            budget.withSpending(spent)
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
                        updateState {
                            it.copy(
                                showFormSheet = false,
                                formMode = BudgetFormMode.Create,
                                editingBudgetId = null,
                                limitText = "",
                                selectedCategory = TransactionCategory.OTHER,
                            )
                        }
                        load()
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
                        updateState {
                            it.copy(
                                showFormSheet = false,
                                formMode = BudgetFormMode.Create,
                                editingBudgetId = null,
                                limitText = "",
                                selectedCategory = TransactionCategory.OTHER,
                            )
                        }
                        load()
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

private fun AppError.asMessageText(): String = when (this) {
    AppError.Unknown -> "Something went wrong"
    is AppError.Message -> value
}
