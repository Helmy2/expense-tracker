package com.expense.tracker.feature.expense.data.repository

import com.expense.tracker.feature.expense.data.local.TransactionDao
import com.expense.tracker.feature.expense.data.mapper.toDomain
import com.expense.tracker.feature.expense.data.mapper.toEntity
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.runSuspendCatching

class RoomTransactionRepository(
    private val dao: TransactionDao,
    private val timeProvider: TimeProvider,
) : TransactionRepository {

    override suspend fun loadTransactions(): Result<List<Transaction>> = runSuspendCatching(
        block = {
            dao.getAll().map { it.toDomain() }
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
    ): Result<Transaction> = runSuspendCatching(
        block = {
            val transaction = Transaction(
                id = generateId(),
                amount = amount,
                type = type,
                category = category,
                note = note.trim(),
                createdAtMillis = timeProvider.nowMillis(),
            )
            dao.insert(transaction.toEntity())
            transaction
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun deleteTransaction(id: String): Result<Unit> = runSuspendCatching(
        block = {
            dao.deleteById(id)
        },
        onFailure = { AppError.Unknown },
    )

    private fun generateId(): String = "${timeProvider.nowMillis()}-${kotlin.random.Random.nextLong()}"
}
