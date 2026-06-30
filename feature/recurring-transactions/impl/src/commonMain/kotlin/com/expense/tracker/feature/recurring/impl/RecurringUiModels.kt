package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory

data class RecurringTemplateUi(
    val id: String,
    val formattedAmount: String,
    val category: TransactionCategory,
    val frequencyLabel: String,
    val nextDueDateFormatted: String,
    val isPaused: Boolean,
    val isIncome: Boolean,
)

data class UpcomingRecurringUi(
    val templateId: String,
    val formattedAmount: String,
    val category: TransactionCategory,
    val frequencyLabel: String,
    val nextDueDateFormatted: String,
    val isIncome: Boolean,
)
