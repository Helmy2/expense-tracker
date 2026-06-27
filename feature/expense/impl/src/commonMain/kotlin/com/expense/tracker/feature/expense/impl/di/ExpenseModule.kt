package com.expense.tracker.feature.expense.impl.di

import com.expense.tracker.feature.expense.api.navigation.ExpenseRoute
import com.expense.tracker.feature.expense.impl.ExpenseScreen
import com.expense.tracker.feature.expense.impl.ExpenseViewModel
import com.expense.tracker.shared.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val expenseUiModule = module {
    viewModel<ExpenseViewModel>()

    navigation<ExpenseRoute> {
        val navigator = koinInject<Navigator>()
        ExpenseScreen(
            onNavigateBack = { navigator.goBack() },
        )
    }
}
