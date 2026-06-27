package com.expense.tracker.shared.di

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.Result

suspend fun TransactionRepository.loadTransactionsOrThrow(): List<Transaction> = when (val result = loadTransactions()) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun TransactionRepository.addTransactionOrThrow(
    amount: Double,
    type: TransactionType,
    category: TransactionCategory,
    note: String,
): Transaction = when (val result = addTransaction(amount, type, category, note)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun TransactionRepository.deleteTransactionOrThrow(id: String) = when (val result = deleteTransaction(id)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}
