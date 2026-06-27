package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.data.local.BudgetDatabaseFactory
import com.expense.tracker.feature.budget.impl.di.budgetUiModule
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
    budgetDatabaseFactory: BudgetDatabaseFactory? = null,
    appContext: Any? = null,
    appDeclaration: KoinAppDeclaration = {}
) {
    if (hasStartedKoin) return

    val coreModules = mutableListOf(
        expenseDataModule(transactionDatabaseFactory, appContext),
        expenseUiModule,
        budgetUiModule,
        navigatorModule,
    )

    if (budgetDatabaseFactory != null) {
        coreModules.add(budgetDataModule(budgetDatabaseFactory))
    }

    startKoin {
        appDeclaration()
        modules(coreModules)
    }
    hasStartedKoin = true
}
