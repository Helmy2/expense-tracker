package com.expense.tracker.feature.sample.domain.model

data class SampleItem(
    val id: String,
    val title: String,
    val description: String,
    val category: SampleCategory,
    val occurredAtMillis: Long,
    val updatedAtMillis: Long,
) {
    fun update(
        title: String,
        description: String,
        category: SampleCategory,
        updatedAtMillis: Long,
    ): SampleItem = copy(
        title = title.trim(),
        description = description.trim(),
        category = category,
        updatedAtMillis = updatedAtMillis,
    )
}

enum class SampleCategory {
    Contract,
    Architecture,
    Preview,
}
