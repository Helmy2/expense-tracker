package com.expense.tracker.feature.recurring.domain.model

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType

data class UpcomingRecurring(
    val templateId: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val note: String,
    val frequency: RecurringFrequency,
    val nextDueDateMillis: Long,
)
