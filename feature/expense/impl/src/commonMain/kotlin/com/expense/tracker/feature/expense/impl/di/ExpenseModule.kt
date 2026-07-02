package com.expense.tracker.feature.expense.impl.di

import com.expense.tracker.feature.expense.api.navigation.ExpenseRoute
import com.expense.tracker.feature.expense.impl.ExpensePresentationMapper
import com.expense.tracker.feature.expense.impl.ExpenseScreen
import com.expense.tracker.feature.expense.impl.ExpenseViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val expenseUiModule = module {
    viewModel<ExpenseViewModel>()
    factory { ExpensePresentationMapper(get()) }

    navigation<ExpenseRoute> {
        ExpenseScreen()
    }
}
