package com.expense.tracker.shared.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Multi-tab navigator that maintains independent per-tab back stacks.
 *
 * Each tab is identified by a [TabDefinition] and owns a [SnapshotStateList]<[NavKey]>.
 * [currentBackStack] always points to the active tab's stack and can be consumed
 * directly by Navigation 3 [NavDisplay].
 *
 * Registered as a Koin `single` — instantiated once with the full tab list.
 */
class BottomNavNavigator(tabs: List<TabDefinition>) {

    private val tabDefs: List<TabDefinition> = tabs.toList()
    private val stacks: MutableMap<String, SnapshotStateList<NavKey>> = mutableMapOf()

    /** Index of the currently selected tab. */
    val selectedTabIndex: MutableState<Int> = mutableStateOf(0)

    /**
     * The active tab's back stack. Changes identity when [selectTab] is called.
     * Compose will recompose readers whenever the reference switches or the
     * underlying list mutates.
     */
    private val _currentBackStack: MutableState<SnapshotStateList<NavKey>> =
        mutableStateOf(mutableStateListOf())

    /** Public read accessor for the active tab's back stack. */
    val currentBackStack: SnapshotStateList<NavKey>
        get() = _currentBackStack.value

    /** `true` when the active tab has navigated past its root route. */
    val isDetailVisible: Boolean
        get() = currentBackStack.size > 1

    init {
        require(tabDefs.isNotEmpty()) { "At least one tab must be provided" }
        tabDefs.forEach { tab ->
            stacks[tab.id] = mutableStateListOf(tab.rootRoute)
        }
        _currentBackStack.value = stacks[tabDefs.first().id]!!
    }

    /**
     * Switch the active tab. If the tab's stack is empty (should not happen
     * after init), it is seeded with the tab's root route.
     */
    fun selectTab(index: Int) {
        if (index !in tabDefs.indices) return
        selectedTabIndex.value = index
        _currentBackStack.value = stacks[tabDefs[index].id]!!
    }

    /** Push [destination] onto the active tab's back stack. */
    fun goTo(destination: NavKey) {
        currentBackStack.add(destination)
    }

    /** Pop the active tab's back stack. No-op when already at root. */
    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        }
    }
}
