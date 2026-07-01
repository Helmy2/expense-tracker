package com.expense.tracker.feature.budget.impl.di

import com.expense.tracker.feature.budget.api.navigation.BudgetDetailRoute
import com.expense.tracker.feature.budget.api.navigation.BudgetFormRoute
import com.expense.tracker.feature.budget.api.navigation.BudgetRoute
import com.expense.tracker.feature.budget.impl.BudgetDetailScreen
import com.expense.tracker.feature.budget.impl.BudgetDetailViewModel
import com.expense.tracker.feature.budget.impl.BudgetFormScreen
import com.expense.tracker.feature.budget.impl.BudgetPresentationMapper
import com.expense.tracker.feature.budget.impl.BudgetScreen
import com.expense.tracker.feature.budget.impl.BudgetViewModel
import com.expense.tracker.shared.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val budgetUiModule = module {
    viewModel<BudgetViewModel>()
    viewModel<BudgetDetailViewModel>()
    factory { BudgetPresentationMapper(get()) }

    navigation<BudgetRoute> {
        val navigator = koinInject<Navigator>()
        BudgetScreen(
            onNavigateBack = { navigator.goBack() },
            onNavigateToDetail = { budgetId -> navigator.goTo(BudgetDetailRoute(budgetId)) },
            onNavigateToCreate = { navigator.goTo(BudgetFormRoute()) },
            onNavigateToEdit = { budgetId -> navigator.goTo(BudgetFormRoute(budgetId)) },
        )
    }

    navigation<BudgetFormRoute> { route ->
        val navigator = koinInject<Navigator>()
        BudgetFormScreen(
            budgetId = route.budgetId,
            onNavigateBack = { navigator.goBack() },
        )
    }

    navigation<BudgetDetailRoute> { route ->
        val navigator = koinInject<Navigator>()
        BudgetDetailScreen(
            budgetId = route.budgetId,
            onNavigateBack = { navigator.goBack() },
            onNavigateToEdit = { budgetId -> navigator.goTo(BudgetFormRoute(budgetId)) },
        )
    }
}
