package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.data.local.IosBudgetDatabaseFactory
import com.expense.tracker.feature.budget.data.local.createBudgetDatabase
import com.expense.tracker.feature.budget.data.repository.RoomBudgetRepository
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.data.local.IosTransactionDatabaseFactory
import com.expense.tracker.feature.expense.data.local.createTransactionDatabase
import com.expense.tracker.feature.expense.data.repository.RoomTransactionRepository
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosBudgetRepository(): BudgetRepository {
    val budgetDatabase = createBudgetDatabase(IosBudgetDatabaseFactory())
    val transactionDatabase = createTransactionDatabase(IosTransactionDatabaseFactory())
    val transactionRepository = RoomTransactionRepository(
        dao = transactionDatabase.transactionDao(),
        timeProvider = SystemTimeProvider,
    )
    return RoomBudgetRepository(
        budgetDao = budgetDatabase.budgetDao(),
        transactionRepository = transactionRepository,
        timeProvider = SystemTimeProvider,
    )
}
