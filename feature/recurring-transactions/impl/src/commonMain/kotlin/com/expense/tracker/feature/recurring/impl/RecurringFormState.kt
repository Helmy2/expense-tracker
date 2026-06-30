package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency

data class RecurringFormState(
    val isLoading: Boolean = false,
    val templateId: String? = null,
    val amountText: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: TransactionCategory = TransactionCategory.OTHER,
    val noteText: String = "",
    val selectedFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val categoryMenuExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
