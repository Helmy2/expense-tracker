package com.expense.tracker.shared.navigation

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

/**
 * Defines a top-level tab in bottom navigation.
 *
 * @param id Unique identifier for the tab (used as map key in [BottomNavNavigator]).
 * @param rootRoute The [NavKey] route displayed when the tab is first selected.
 * @param labelRes String resource for the tab label.
 */
data class TabDefinition(
    val id: String,
    val rootRoute: NavKey,
    val labelRes: StringResource,
)
