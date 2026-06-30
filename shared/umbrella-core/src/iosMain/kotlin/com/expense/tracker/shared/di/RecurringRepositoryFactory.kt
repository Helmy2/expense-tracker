package com.expense.tracker.shared.di

import com.expense.tracker.feature.recurring.data.repository.RoomRecurringTemplateRepository
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.data.database.IosAppDatabaseFactory
import com.expense.tracker.shared.core.data.database.createAppDatabase
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosRecurringRepository(): RecurringTemplateRepository {
    val database = createAppDatabase(IosAppDatabaseFactory())
    return RoomRecurringTemplateRepository(
        templateDao = database.recurringTemplateDao(),
        transactionDao = database.transactionDao(),
        timeProvider = SystemTimeProvider,
    )
}
