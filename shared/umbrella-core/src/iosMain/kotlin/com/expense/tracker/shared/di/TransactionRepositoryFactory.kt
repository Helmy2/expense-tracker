package com.expense.tracker.shared.di

import com.expense.tracker.feature.expense.data.local.IosTransactionDatabaseFactory
import com.expense.tracker.feature.expense.data.local.createTransactionDatabase
import com.expense.tracker.feature.expense.data.repository.RoomTransactionRepository
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosTransactionRepository(): TransactionRepository {
    val database = createTransactionDatabase(IosTransactionDatabaseFactory())
    return RoomTransactionRepository(
        dao = database.transactionDao(),
        timeProvider = SystemTimeProvider,
    )
}
