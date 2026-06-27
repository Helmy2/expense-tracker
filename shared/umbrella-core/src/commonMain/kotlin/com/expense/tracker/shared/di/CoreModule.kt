package com.expense.tracker.shared.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.expense.tracker.feature.expense.data.local.TransactionDao
import com.expense.tracker.feature.expense.data.local.TransactionDatabase
import com.expense.tracker.feature.expense.data.local.TransactionDatabaseFactory
import com.expense.tracker.feature.expense.data.local.createTransactionDatabase
import com.expense.tracker.feature.expense.data.repository.RoomTransactionRepository
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.data.datastore.createDataStore
import com.expense.tracker.shared.core.data.network.HttpClientFactory
import com.expense.tracker.shared.core.data.session.DataStoreSessionStorage
import com.expense.tracker.shared.core.domain.SystemTimeProvider
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.session.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun expenseDataModule(
    transactionDatabaseFactory: TransactionDatabaseFactory,
    appContext: Any? = null,
) = module {
    single<TimeProvider> { SystemTimeProvider }
    single<TransactionDatabase> { createTransactionDatabase(transactionDatabaseFactory) }
    single<TransactionDao> { get<TransactionDatabase>().transactionDao() }
    singleOf(::RoomTransactionRepository).bind<TransactionRepository>()

    single<DataStore<Preferences>> { createDataStore(appContext) }
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
    single {
        HttpClientFactory(
            sessionStorage = get<SessionStorage>(),
            baseUrl = null,
        )
    }
}