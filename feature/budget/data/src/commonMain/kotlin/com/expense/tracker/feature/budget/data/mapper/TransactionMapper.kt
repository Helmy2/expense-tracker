package com.expense.tracker.feature.budget.data.mapper

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.data.entity.TransactionEntity

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = TransactionCategory.valueOf(category),
    note = note,
    createdAtMillis = createdAtMillis,
)
