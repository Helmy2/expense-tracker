package com.expense.tracker.shared.di

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.domain.Result

suspend fun RecurringTemplateRepository.loadTemplatesOrThrow(): List<RecurringTemplate> = safeOrThrow("RecurringTemplateRepository.loadTemplates") {
    when (val result = loadTemplates()) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.loadTemplateByIdOrThrow(id: String): RecurringTemplate? = safeOrThrow("RecurringTemplateRepository.loadTemplateById") {
    when (val result = loadTemplateById(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.createTemplateOrThrow(
    amount: Double,
    type: TransactionType,
    category: TransactionCategory,
    note: String,
    frequency: RecurringFrequency,
    startDateMillis: Long,
    endDateMillis: Long?,
): RecurringTemplate = safeOrThrow("RecurringTemplateRepository.createTemplate") {
    when (val result = createTemplate(amount, type, category, note, frequency, startDateMillis, endDateMillis)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.updateTemplateOrThrow(
    id: String,
    amount: Double,
    type: TransactionType,
    category: TransactionCategory,
    note: String,
    frequency: RecurringFrequency,
    startDateMillis: Long,
    endDateMillis: Long?,
): RecurringTemplate = safeOrThrow("RecurringTemplateRepository.updateTemplate") {
    when (val result = updateTemplate(id, amount, type, category, note, frequency, startDateMillis, endDateMillis)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.deleteTemplateOrThrow(id: String) = safeOrThrow("RecurringTemplateRepository.deleteTemplate") {
    when (val result = deleteTemplate(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.togglePauseOrThrow(id: String): RecurringTemplate = safeOrThrow("RecurringTemplateRepository.togglePause") {
    when (val result = togglePause(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.loadUpcomingOrThrow(count: Int = 5): List<UpcomingRecurring> = safeOrThrow("RecurringTemplateRepository.loadUpcoming") {
    when (val result = loadUpcoming(count)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun RecurringTemplateRepository.processDueRecurringOrThrow(): Int = safeOrThrow("RecurringTemplateRepository.processDueRecurring") {
    when (val result = processDueRecurring()) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}
