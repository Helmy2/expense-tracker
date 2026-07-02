package com.expense.tracker.shared.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.nav_budgets
import com.expense.tracker.shared.core.strings.nav_expenses
import com.expense.tracker.shared.core.strings.nav_recurring
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.components.NavigationBar
import com.expense.tracker.shared.designsystem.components.NavigationDestination
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AppShell() {
    val navigator = koinInject<BottomNavNavigator>()
    val state = BottomNavState(
        selectedTabIndex = navigator.selectedTabIndex.value,
        isDetailVisible = navigator.isDetailVisible
    )

    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Content area
                    Box(modifier = Modifier.weight(1f).safeDrawingPadding()) {
                        AppNavigation()
                    }
                    // Bottom bar with animation
                    AnimatedVisibility(
                        visible = !state.isDetailVisible,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        NavigationBar(
                            destinations = listOf(
                                NavigationDestination(
                                    label = stringResource(Res.string.nav_expenses),
                                    icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) },
                                    selectedIcon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) }
                                ),
                                NavigationDestination(
                                    label = stringResource(Res.string.nav_budgets),
                                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                                    selectedIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) }
                                ),
                                NavigationDestination(
                                    label = stringResource(Res.string.nav_recurring),
                                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                                    selectedIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                                )
                            ),
                            selectedDestination = state.selectedTabIndex,
                            onDestinationSelected = { index -> navigator.selectTab(index) }
                        )
                    }
                }
            }
        }
    }
}
