package com.expense.tracker.feature.budget.impl.di

import com.expense.tracker.feature.budget.api.navigation.BudgetDetailRoute
import com.expense.tracker.feature.budget.api.navigation.BudgetRoute
import com.expense.tracker.feature.budget.impl.BudgetDetailScreen
import com.expense.tracker.feature.budget.impl.BudgetDetailViewModel
import com.expense.tracker.feature.budget.impl.BudgetPresentationMapper
import com.expense.tracker.feature.budget.impl.BudgetScreen
import com.expense.tracker.feature.budget.impl.BudgetViewModel
import com.expense.tracker.shared.navigation.BottomNavNavigator
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
        val navigator = koinInject<BottomNavNavigator>()
        BudgetScreen(
            onNavigateToDetail = { budgetId -> navigator.goTo(BudgetDetailRoute(budgetId)) },
        )
    }

    navigation<BudgetDetailRoute> { route ->
        val navigator = koinInject<BottomNavNavigator>()
        BudgetDetailScreen(
            budgetId = route.budgetId,
            onNavigateBack = { navigator.goBack() },
        )
    }
}
