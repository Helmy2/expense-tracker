package com.expense.tracker.shared.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

@Deprecated(
    message = "Use BottomNavNavigator for multi-tab navigation. " +
        "This single-stack navigator is retained for backward compatibility only.",
    replaceWith = ReplaceWith("BottomNavNavigator(tabs)"),
)
class Navigator(startDestination: NavKey) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(startDestination)

    fun goTo(destination: NavKey) {
        backStack.add(destination)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
