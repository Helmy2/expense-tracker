package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.api.navigation.BudgetRoute
import com.expense.tracker.feature.budget.impl.di.budgetUiModule
import com.expense.tracker.feature.expense.api.navigation.ExpenseRoute
import com.expense.tracker.feature.expense.impl.di.expenseUiModule
import com.expense.tracker.feature.recurring.api.navigation.RecurringListRoute
import com.expense.tracker.feature.recurring.impl.di.recurringUiModule
import com.expense.tracker.shared.core.data.database.AppDatabaseFactory
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.nav_budgets
import com.expense.tracker.shared.core.strings.nav_expenses
import com.expense.tracker.shared.core.strings.nav_recurring
import com.expense.tracker.shared.navigation.BottomNavNavigator
import com.expense.tracker.shared.navigation.TabDefinition
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


val navigatorModule = module {
    single {
        BottomNavNavigator(
            tabs = listOf(
                TabDefinition(id = "expenses", rootRoute = ExpenseRoute, labelRes = Res.string.nav_expenses),
                TabDefinition(id = "budgets", rootRoute = BudgetRoute, labelRes = Res.string.nav_budgets),
                TabDefinition(id = "recurring", rootRoute = RecurringListRoute, labelRes = Res.string.nav_recurring)
            )
        )
    }
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
