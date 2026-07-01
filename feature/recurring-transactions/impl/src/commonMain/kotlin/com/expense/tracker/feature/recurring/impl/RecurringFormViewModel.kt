package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.feature.expense.domain.model.IncomeCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.presentation.MviViewModel

class RecurringFormViewModel(
    private val repository: RecurringTemplateRepository,
    private val timeProvider: TimeProvider,
) : MviViewModel<RecurringFormState, RecurringFormAction, RecurringFormEvent>(
    initialState = RecurringFormState(
        startDateMillis = timeProvider.nowMillis(),
    ),
) {
    override suspend fun handleAction(action: RecurringFormAction) {
        when (action) {
            is RecurringFormAction.SetTemplate -> setTemplate(action.templateId)
            is RecurringFormAction.AmountChanged -> updateState { it.copy(amountText = action.value) }
            is RecurringFormAction.TypeSelected -> updateState {
                val firstCategory = when (action.type) {
                    TransactionType.INCOME -> IncomeCategory.entries.first().name
                    TransactionType.EXPENSE -> ExpenseCategory.entries.first().name
                }
                it.copy(selectedType = action.type, selectedCategory = firstCategory)
            }
            is RecurringFormAction.CategorySelected -> updateState {
                it.copy(selectedCategory = action.category, categoryMenuExpanded = false)
            }
            is RecurringFormAction.ToggleCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = !it.categoryMenuExpanded)
            }
            is RecurringFormAction.DismissCategoryMenu -> updateState {
                it.copy(categoryMenuExpanded = false)
            }
            is RecurringFormAction.NoteChanged -> updateState { it.copy(noteText = action.value) }
            is RecurringFormAction.FrequencySelected -> updateState {
                it.copy(selectedFrequency = action.frequency)
            }
            is RecurringFormAction.StartDateSelected -> updateState {
                it.copy(startDateMillis = action.millis)
            }
            is RecurringFormAction.EndDateSelected -> updateState {
                it.copy(endDateMillis = action.millis)
            }
            is RecurringFormAction.ClearEndDate -> updateState {
                it.copy(endDateMillis = null)
            }
            is RecurringFormAction.Save -> save()
        }
    }

    private suspend fun setTemplate(templateId: String?) {
        updateState { it.copy(templateId = templateId) }
        if (templateId != null) {
            loadTemplate(templateId)
        }
    }

    private suspend fun loadTemplate(id: String) {
        updateState { it.copy(isLoading = true) }

        when (val result = repository.loadTemplateById(id)) {
            is Result.Success -> {
                val template = result.value
                if (template != null) {
                    updateState {
                        it.copy(
                            isLoading = false,
                            amountText = template.amount.toString(),
                            selectedType = template.type,
                            selectedCategory = template.category,
                            noteText = template.note,
                            selectedFrequency = template.frequency,
                            startDateMillis = template.startDateMillis,
                            endDateMillis = template.endDateMillis,
                        )
                    }
                } else {
                    updateState { it.copy(isLoading = false, error = "Template not found") }
                }
            }

            is Result.Failure -> updateState {
                it.copy(isLoading = false, error = result.error.asMessageText())
            }
        }
    }

    private suspend fun save() {
        val currentState = state.value
        val amount = currentState.amountText.toDoubleOrNull()
        val isAmountValid = amount != null && amount > 0
        val startDate = currentState.startDateMillis

        if (!isAmountValid || startDate == null) {
            if (!isAmountValid) {
                sendEvent(RecurringFormEvent.Error("Please enter a valid amount"))
            } else {
                sendEvent(RecurringFormEvent.Error("Please select a start date"))
            }
            return
        }

        updateState { it.copy(isSaving = true) }

        val result = if (currentState.templateId != null) {
            repository.updateTemplate(
                id = currentState.templateId,
                amount = amount,
                type = currentState.selectedType,
                category = currentState.selectedCategory,
                note = currentState.noteText.trim(),
                frequency = currentState.selectedFrequency,
                startDateMillis = startDate,
                endDateMillis = currentState.endDateMillis,
            )
        } else {
            repository.createTemplate(
                amount = amount,
                type = currentState.selectedType,
                category = currentState.selectedCategory,
                note = currentState.noteText.trim(),
                frequency = currentState.selectedFrequency,
                startDateMillis = startDate,
                endDateMillis = currentState.endDateMillis,
            )
        }

        when (result) {
            is Result.Success -> {
                updateState { it.copy(isSaving = false) }
                sendEvent(RecurringFormEvent.RecurringSaved)
            }

            is Result.Failure -> {
                updateState { it.copy(isSaving = false) }
                sendEvent(RecurringFormEvent.Error(result.error.asMessageText()))
            }
        }
    }
}
