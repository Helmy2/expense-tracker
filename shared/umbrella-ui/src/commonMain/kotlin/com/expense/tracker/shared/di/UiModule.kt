package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.impl.di.budgetUiModule
import com.expense.tracker.feature.expense.api.navigation.ExpenseRoute
import com.expense.tracker.feature.expense.impl.di.expenseUiModule
import com.expense.tracker.feature.recurring.impl.di.recurringUiModule
import com.expense.tracker.shared.core.data.database.AppDatabaseFactory
import com.expense.tracker.shared.navigation.Navigator
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


val navigatorModule = module {
    single { Navigator(startDestination = ExpenseRoute) }
}

private var hasStartedKoin = false

fun initKoin(
    databaseFactory: AppDatabaseFactory,
    appContext: Any? = null,
    appDeclaration: KoinAppDeclaration = {},
) {
    if (hasStartedKoin) return

    val coreModules = listOf(
        appDataModule(databaseFactory, appContext),
        expenseUiModule,
        budgetUiModule,
        recurringUiModule,
        navigatorModule,
    )

    startKoin {
        appDeclaration()
        modules(coreModules)
    }
    hasStartedKoin = true
}
