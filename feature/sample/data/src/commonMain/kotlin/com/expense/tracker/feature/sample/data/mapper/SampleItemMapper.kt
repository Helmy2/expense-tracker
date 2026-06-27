package com.expense.tracker.feature.sample.data.mapper

import com.expense.tracker.feature.sample.domain.model.SampleItem
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.shared.core.data.local.SampleItemEntity

fun SampleItemEntity.toDomain(): SampleItem = SampleItem(
    id = id,
    title = title,
    description = description,
    category = category.toSampleCategory(),
    occurredAtMillis = occurredAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun SampleItem.toEntity(position: Int): SampleItemEntity = SampleItemEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    occurredAtMillis = occurredAtMillis,
    updatedAtMillis = updatedAtMillis,
    position = position,
)

private fun String.toSampleCategory(): SampleCategory = SampleCategory.entries.firstOrNull { category ->
    category.name == this
} ?: SampleCategory.Contract
