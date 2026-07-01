package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.feature.expense.domain.model.IncomeCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.model.computeDashboard
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.presentation.MviViewModel
import kotlin.math.abs
import kotlin.math.round

class ExpenseViewModel(
    private val repository: TransactionRepository,
    private val recurringRepository: RecurringTemplateRepository,
    private val timeProvider: TimeProvider,
    val mapper: ExpensePresentationMapper,
) : MviViewModel<ExpenseState, ExpenseAction, ExpenseEvent>(
    initialState = ExpenseState(),
) {
    override suspend fun handleAction(action: ExpenseAction) {
        when (action) {
            is ExpenseAction.Load -> load()
            is ExpenseAction.AmountChanged -> updateState { it.copy(amountText = action.value) }
            is ExpenseAction.TypeSelected -> updateState {
                val firstCategory = when (action.type) {
                    TransactionType.INCOME -> IncomeCategory.entries.first().name
                    TransactionType.EXPENSE -> ExpenseCategory.entries.first().name
                }
                it.copy(selectedType = action.type, selectedCategory = firstCategory)
            }
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
            is ExpenseAction.DeleteTransaction -> updateState { it.copy(deleteTargetId = action.id) }
            is ExpenseAction.ConfirmDelete -> confirmDelete()
            is ExpenseAction.CancelDelete -> updateState { it.copy(deleteTargetId = null) }
            is ExpenseAction.ToggleFormSheet -> updateState {
                it.copy(showBottomSheet = !it.showBottomSheet)
            }
            is ExpenseAction.DismissFormSheet -> updateState {
                it.copy(showBottomSheet = false)
            }
            is ExpenseAction.NavigateToRecurringList -> {
                // handled at screen level via navigation callback
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(contentState = ExpenseContentState.Loading) }

        when (val result = repository.loadTransactions()) {
            is Result.Success -> {
                val transactions = result.value
                val dashboard = computeDashboard(transactions)

                val upcomingResult = recurringRepository.loadUpcoming(5)
                val upcomingItems = when (upcomingResult) {
                    is Result.Success -> upcomingResult.value
                    is Result.Failure -> emptyList()
                }

                updateState { current ->
                    current.copy(
                        contentState = if (transactions.isEmpty() && upcomingItems.isEmpty()) {
                            ExpenseContentState.Empty
                        } else {
                            ExpenseContentState.Content(transactions.map(mapper::toTransactionUi))
                        },
                        dashboard = mapper.toDashboardUi(dashboard),
                        upcomingRecurring = upcomingItems.map { it.toUpcomingUi() },
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
                        selectedType = TransactionType.EXPENSE,
                        selectedCategory = ExpenseCategory.OTHER_EXPENSE.name,
                        showBottomSheet = false,
                    )
                }
                load()
                sendEvent(ExpenseEvent.TransactionSaved)
            }

            is Result.Failure -> {
                sendEvent(ExpenseEvent.Error(result.error.asMessageText()))
            }
        }
    }

    private suspend fun confirmDelete() {
        val targetId = state.value.deleteTargetId ?: return

        when (val result = repository.deleteTransaction(targetId)) {
            is Result.Success -> {
                updateState { it.copy(deleteTargetId = null) }
                load()
            }
            is Result.Failure -> {
                updateState { it.copy(deleteTargetId = null) }
                sendEvent(ExpenseEvent.Error(result.error.asMessageText()))
            }
        }
    }

    private fun UpcomingRecurring.toUpcomingUi(): UpcomingRecurringUi = UpcomingRecurringUi(
        templateId = templateId,
        formattedAmount = formatSignedAmount(amount, type == TransactionType.INCOME),
        category = category,
        frequencyLabel = frequencyLabel(frequency),
        nextDueDateFormatted = timeProvider.formatDate(nextDueDateMillis),
        isIncome = type == TransactionType.INCOME,
    )

    private fun formatSignedAmount(amount: Double, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        val absolute = abs(amount)
        val whole = absolute.toLong()
        val cents = round((absolute - whole) * 100).toInt()
        return "$sign $${whole}.${cents.toString().padStart(2, '0')}"
    }

    private fun frequencyLabel(frequency: RecurringFrequency): String = when (frequency) {
        RecurringFrequency.DAILY -> "Daily"
        RecurringFrequency.WEEKLY -> "Weekly"
        RecurringFrequency.MONTHLY -> "Monthly"
        RecurringFrequency.YEARLY -> "Yearly"
    }
}
