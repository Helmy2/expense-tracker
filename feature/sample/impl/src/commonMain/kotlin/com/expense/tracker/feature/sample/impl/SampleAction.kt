package com.expense.tracker.feature.sample.impl

import com.expense.tracker.feature.sample.domain.model.SampleCategory

sealed interface SampleAction {
    data class Load(val force: Boolean = false) : SampleAction
    data class SelectItem(val id: String, val navigate: Boolean = true) : SampleAction
    data object StartCreate : SampleAction
    data object StartEdit : SampleAction
    data object CancelEdit : SampleAction
    data class TitleChanged(val value: String) : SampleAction
    data class DescriptionChanged(val value: String) : SampleAction
    data class CategoryChanged(val value: SampleCategory) : SampleAction
    data object Save : SampleAction
}
