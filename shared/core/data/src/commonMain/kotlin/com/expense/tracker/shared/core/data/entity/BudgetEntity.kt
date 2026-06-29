package com.expense.tracker.shared.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val monthlyLimit: Double,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
