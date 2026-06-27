package com.expense.tracker.feature.budget.data.mapper

import com.expense.tracker.feature.budget.data.local.BudgetEntity
import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.expense.domain.model.TransactionCategory

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    category = TransactionCategory.valueOf(category),
    monthlyLimit = monthlyLimit,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    category = category.name,
    monthlyLimit = monthlyLimit,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
