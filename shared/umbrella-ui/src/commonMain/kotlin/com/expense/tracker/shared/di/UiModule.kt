package com.expense.tracker.shared.di

import com.expense.tracker.feature.expense.api.navigation.ExpenseRoute
import com.expense.tracker.feature.expense.data.local.TransactionDatabaseFactory
import com.expense.tracker.feature.expense.impl.di.expenseUiModule
import com.expense.tracker.shared.navigation.Navigator
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


val navigatorModule = module {
    single { Navigator(startDestination = ExpenseRoute) }
}

private var hasStartedKoin = false

fun initKoin(
    transactionDatabaseFactory: TransactionDatabaseFactory,
    appContext: Any? = null,
    appDeclaration: KoinAppDeclaration = {}
) {
    if (hasStartedKoin) return

    startKoin {
        appDeclaration()
        modules(
            expenseDataModule(transactionDatabaseFactory, appContext),
            expenseUiModule,
            navigatorModule,
        )
    }
    hasStartedKoin = true
}
