package com.expense.tracker.feature.recurring.impl.di

import com.expense.tracker.feature.recurring.api.navigation.RecurringFormRoute
import com.expense.tracker.feature.recurring.api.navigation.RecurringListRoute
import com.expense.tracker.feature.recurring.impl.RecurringFormScreen
import com.expense.tracker.feature.recurring.impl.RecurringFormViewModel
import com.expense.tracker.feature.recurring.impl.RecurringListScreen
import com.expense.tracker.feature.recurring.impl.RecurringListViewModel
import com.expense.tracker.feature.recurring.impl.RecurringPresentationMapper
import com.expense.tracker.shared.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val recurringUiModule = module {
    viewModel<RecurringListViewModel>()
    viewModel<RecurringFormViewModel>()
    factory { RecurringPresentationMapper(get()) }

    navigation<RecurringListRoute> {
        val navigator = koinInject<Navigator>()
        RecurringListScreen(
            onNavigateBack = { navigator.goBack() },
            onNavigateToCreate = { navigator.goTo(RecurringFormRoute()) },
            onNavigateToEdit = { templateId -> navigator.goTo(RecurringFormRoute(templateId)) },
        )
    }

    navigation<RecurringFormRoute> { route ->
        val navigator = koinInject<Navigator>()
        RecurringFormScreen(
            templateId = route.templateId,
            onNavigateBack = { navigator.goBack() },
        )
    }
}
