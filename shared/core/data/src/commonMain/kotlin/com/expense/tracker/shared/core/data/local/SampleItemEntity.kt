package com.expense.tracker.shared.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample_items")
data class SampleItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val occurredAtMillis: Long,
    val updatedAtMillis: Long,
    val position: Int,
)