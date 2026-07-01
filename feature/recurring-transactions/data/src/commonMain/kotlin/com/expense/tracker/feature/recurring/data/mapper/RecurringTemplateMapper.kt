package com.expense.tracker.feature.recurring.data.mapper

import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.shared.core.data.entity.RecurringTemplateEntity

fun RecurringTemplateEntity.toDomain(): RecurringTemplate = RecurringTemplate(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = category,
    note = note,
    frequency = RecurringFrequency.valueOf(frequency),
    startDateMillis = startDateMillis,
    endDateMillis = endDateMillis,
    isPaused = isPaused,
    lastGeneratedDateMillis = lastGeneratedDateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun RecurringTemplate.toEntity(): RecurringTemplateEntity = RecurringTemplateEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category,
    note = note,
    frequency = frequency.name,
    startDateMillis = startDateMillis,
    endDateMillis = endDateMillis,
    isPaused = isPaused,
    lastGeneratedDateMillis = lastGeneratedDateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
