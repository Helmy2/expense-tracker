package com.expense.tracker.feature.expense.data.mapper

import com.expense.tracker.feature.expense.data.local.TransactionEntity
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = TransactionCategory.valueOf(category),
    note = note,
    createdAtMillis = createdAtMillis,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    note = note,
    createdAtMillis = createdAtMillis,
)
