package com.expense.tracker.feature.recurring.impl

data class RecurringTemplateUi(
    val id: String,
    val formattedAmount: String,
    val category: String,
    val frequencyLabel: String,
    val nextDueDateFormatted: String,
    val isPaused: Boolean,
    val isIncome: Boolean,
)

data class UpcomingRecurringUi(
    val templateId: String,
    val formattedAmount: String,
    val category: String,
    val frequencyLabel: String,
    val nextDueDateFormatted: String,
    val isIncome: Boolean,
)
