package com.expense.tracker.shared.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_templates")
data class RecurringTemplateEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String,
    val frequency: String,
    val startDateMillis: Long,
    val endDateMillis: Long?,
    val isPaused: Boolean = false,
    val lastGeneratedDateMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
