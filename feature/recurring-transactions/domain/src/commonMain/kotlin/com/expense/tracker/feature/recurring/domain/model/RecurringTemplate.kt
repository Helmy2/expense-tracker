package com.expense.tracker.feature.recurring.domain.model

import com.expense.tracker.feature.expense.domain.model.TransactionType

data class RecurringTemplate(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String,
    val frequency: RecurringFrequency,
    val startDateMillis: Long,
    val endDateMillis: Long?,
    val isPaused: Boolean,
    val lastGeneratedDateMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
