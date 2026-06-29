package com.expense.tracker.shared.di

import com.expense.tracker.feature.expense.data.repository.RoomTransactionRepository
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.data.database.IosAppDatabaseFactory
import com.expense.tracker.shared.core.data.database.createAppDatabase
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosTransactionRepository(): TransactionRepository {
    val database = createAppDatabase(IosAppDatabaseFactory())
    return RoomTransactionRepository(
        dao = database.transactionDao(),
        timeProvider = SystemTimeProvider,
    )
}
