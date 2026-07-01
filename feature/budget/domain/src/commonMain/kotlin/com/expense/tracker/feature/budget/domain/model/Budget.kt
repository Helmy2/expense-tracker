package com.expense.tracker.feature.budget.domain.model

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory

data class Budget(
    val id: String,
    val category: ExpenseCategory,
    val monthlyLimit: Double,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
