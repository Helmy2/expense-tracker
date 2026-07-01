package com.expense.tracker.feature.budget.data.mapper

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.shared.core.data.entity.BudgetEntity

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    category = ExpenseCategory.valueOf(category),
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
