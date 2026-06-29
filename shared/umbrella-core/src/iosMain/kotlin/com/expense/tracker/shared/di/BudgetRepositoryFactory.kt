package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.data.repository.RoomBudgetRepository
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.shared.core.data.database.IosAppDatabaseFactory
import com.expense.tracker.shared.core.data.database.createAppDatabase
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosBudgetRepository(): BudgetRepository {
    val database = createAppDatabase(IosAppDatabaseFactory())
    return RoomBudgetRepository(
        budgetDao = database.budgetDao(),
        transactionDao = database.transactionDao(),
        timeProvider = SystemTimeProvider,
    )
}
