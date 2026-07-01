package com.expense.tracker.feature.recurring.domain.repository

import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.shared.core.domain.Result

interface RecurringTemplateRepository {
    suspend fun loadTemplates(): Result<List<RecurringTemplate>>

    suspend fun loadTemplateById(id: String): Result<RecurringTemplate?>

    suspend fun createTemplate(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate>

    suspend fun updateTemplate(
        id: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate>

    suspend fun deleteTemplate(id: String): Result<Unit>

    suspend fun togglePause(id: String): Result<RecurringTemplate>

    suspend fun processDueRecurring(): Result<Int>

    suspend fun loadUpcoming(count: Int = 5): Result<List<UpcomingRecurring>>
}
