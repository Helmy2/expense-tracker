package com.expense.tracker.shared.navigation

import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavigation() {
    val navigator = koinInject<BottomNavNavigator>()

    NavDisplay(
        backStack = navigator.currentBackStack,
        onBack = { navigator.goBack() },
        entryProvider = koinEntryProvider<NavKey>()
    )
}
