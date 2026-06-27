package com.expense.tracker.shared.di

import com.expense.tracker.feature.sample.domain.model.SampleItem
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.feature.sample.domain.repository.SampleRepository
import com.expense.tracker.shared.core.domain.Result

suspend fun SampleRepository.loadItemsOrThrow(): List<SampleItem> = when (val result = loadItems()) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun SampleRepository.loadItemOrThrow(id: String): SampleItem? = when (val result = loadItem(id)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun SampleRepository.createItemOrThrow(
    title: String,
    description: String,
    category: SampleCategory,
): SampleItem = when (val result = createItem(title, description, category)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun SampleRepository.updateItemOrThrow(item: SampleItem): SampleItem = when (val result = updateItem(item)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}
