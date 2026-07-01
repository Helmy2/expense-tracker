package com.expense.tracker.feature.expense.domain.repository

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.Result

interface TransactionRepository {
    suspend fun loadTransactions(): Result<List<Transaction>>

    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
    ): Result<Transaction>

    suspend fun deleteTransaction(id: String): Result<Unit>
}
