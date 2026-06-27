package com.expense.tracker.shared.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.expense.tracker.feature.sample.data.repository.RoomSampleRepository
import com.expense.tracker.feature.sample.domain.repository.SampleRepository
import com.expense.tracker.shared.core.data.datastore.createDataStore
import com.expense.tracker.shared.core.data.local.SampleDatabase
import com.expense.tracker.shared.core.data.local.SampleDatabaseFactory
import com.expense.tracker.shared.core.data.local.SampleItemDao
import com.expense.tracker.shared.core.data.local.createSampleDatabase
import com.expense.tracker.shared.core.data.network.HttpClientFactory
import com.expense.tracker.shared.core.data.session.DataStoreSessionStorage
import com.expense.tracker.shared.core.domain.SystemTimeProvider
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.session.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun sampleDataModule(
    sampleDatabaseFactory: SampleDatabaseFactory,
    appContext: Any? = null,
) = module {
    single<TimeProvider> { SystemTimeProvider }
    single { sampleDatabaseFactory }
    single<SampleDatabase> { createSampleDatabase(get()) }
    single<SampleItemDao> { get<SampleDatabase>().sampleItemDao() }
    singleOf(::RoomSampleRepository).bind<SampleRepository>()

    single<DataStore<Preferences>> { createDataStore(appContext) }
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
    single {
        HttpClientFactory(
            sessionStorage = get<SessionStorage>(),
            baseUrl = null,
        )
    }
}