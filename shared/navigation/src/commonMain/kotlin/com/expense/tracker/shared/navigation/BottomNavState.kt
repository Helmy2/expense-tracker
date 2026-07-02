package com.expense.tracker.shared.navigation

/**
 * Immutable snapshot of bottom navigation state, suitable for mapping to UI.
 */
data class BottomNavState(
    val selectedTabIndex: Int = 0,
    val isDetailVisible: Boolean = false,
)
