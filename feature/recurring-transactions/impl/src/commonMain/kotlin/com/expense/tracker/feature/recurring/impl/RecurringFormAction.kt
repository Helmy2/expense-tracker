package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency

sealed interface RecurringFormAction {
    data class SetTemplate(val templateId: String?) : RecurringFormAction
    data class AmountChanged(val value: String) : RecurringFormAction
    data class TypeSelected(val type: TransactionType) : RecurringFormAction
    data class CategorySelected(val category: TransactionCategory) : RecurringFormAction
    data object ToggleCategoryMenu : RecurringFormAction
    data object DismissCategoryMenu : RecurringFormAction
    data class NoteChanged(val value: String) : RecurringFormAction
    data class FrequencySelected(val frequency: RecurringFrequency) : RecurringFormAction
    data class StartDateSelected(val millis: Long) : RecurringFormAction
    data class EndDateSelected(val millis: Long) : RecurringFormAction
    data object ClearEndDate : RecurringFormAction
    data object Save : RecurringFormAction
}
