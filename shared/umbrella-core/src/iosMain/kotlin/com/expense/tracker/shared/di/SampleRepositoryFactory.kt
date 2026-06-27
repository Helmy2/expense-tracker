package com.expense.tracker.shared.di

import com.expense.tracker.feature.sample.data.repository.RoomSampleRepository
import com.expense.tracker.feature.sample.domain.repository.SampleRepository
import com.expense.tracker.shared.core.data.local.IosSampleDatabaseFactory
import com.expense.tracker.shared.core.data.local.createSampleDatabase
import com.expense.tracker.shared.core.domain.SystemTimeProvider

fun iosSampleRepository(): SampleRepository {
    val database = createSampleDatabase(IosSampleDatabaseFactory())
    return RoomSampleRepository(
        dao = database.sampleItemDao(),
        timeProvider = SystemTimeProvider,
    )
}
