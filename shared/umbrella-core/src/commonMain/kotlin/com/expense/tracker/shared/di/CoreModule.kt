package com.expense.tracker.shared.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.expense.tracker.feature.budget.data.repository.RoomBudgetRepository
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.data.repository.RoomTransactionRepository
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.data.dao.BudgetDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.database.AppDatabase
import com.expense.tracker.shared.core.data.database.AppDatabaseFactory
import com.expense.tracker.shared.core.data.database.createAppDatabase
import com.expense.tracker.shared.core.data.datastore.createDataStore
import com.expense.tracker.shared.core.data.network.HttpClientFactory
import com.expense.tracker.shared.core.data.session.DataStoreSessionStorage
import com.expense.tracker.shared.core.domain.SystemTimeProvider
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.session.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appDataModule(
    databaseFactory: AppDatabaseFactory,
    appContext: Any? = null,
) = module {
    single<TimeProvider> { SystemTimeProvider }
    single<AppDatabase> { createAppDatabase(databaseFactory) }
    single<TransactionDao> { get<AppDatabase>().transactionDao() }
    single<BudgetDao> { get<AppDatabase>().budgetDao() }
    singleOf(::RoomTransactionRepository).bind<TransactionRepository>()
    singleOf(::RoomBudgetRepository).bind<BudgetRepository>()

    single<DataStore<Preferences>> { createDataStore(appContext) }
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
    single {
        HttpClientFactory(
            sessionStorage = get<SessionStorage>(),
            baseUrl = null,
        )
    }
}
