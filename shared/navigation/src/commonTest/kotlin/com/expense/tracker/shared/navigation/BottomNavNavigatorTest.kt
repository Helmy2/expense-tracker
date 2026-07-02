package com.expense.tracker.shared.navigation

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private data object RouteA : NavKey
private data object RouteB : NavKey
private data object RouteC : NavKey
private data object DetailA : NavKey
private data object DetailB : NavKey

@OptIn(InternalResourceApi::class)
private fun testStringResource(key: String) =
    StringResource("string:$key", key, setOf(ResourceItem(setOf(), "", 0, 0)))

class BottomNavNavigatorTest {

    private fun createNavigator() = BottomNavNavigator(
        tabs = listOf(
            TabDefinition(id = "a", rootRoute = RouteA, labelRes = testStringResource("tab_a")),
            TabDefinition(id = "b", rootRoute = RouteB, labelRes = testStringResource("tab_b")),
            TabDefinition(id = "c", rootRoute = RouteC, labelRes = testStringResource("tab_c")),
        )
    )

    @Test
    fun initializesWithFirstTabSelected() {
        val navigator = createNavigator()

        assertEquals(0, navigator.selectedTabIndex.value)
        assertEquals(listOf(RouteA), navigator.currentBackStack.toList())
    }

    @Test
    fun selectTabSwitchesActiveStack() {
        val navigator = createNavigator()

        navigator.selectTab(1)
        assertEquals(1, navigator.selectedTabIndex.value)
        assertEquals(listOf(RouteB), navigator.currentBackStack.toList())

        navigator.selectTab(2)
        assertEquals(2, navigator.selectedTabIndex.value)
        assertEquals(listOf(RouteC), navigator.currentBackStack.toList())
    }

    @Test
    fun selectTabIgnoredForInvalidIndex() {
        val navigator = createNavigator()

        navigator.selectTab(-1)
        assertEquals(0, navigator.selectedTabIndex.value)

        navigator.selectTab(5)
        assertEquals(0, navigator.selectedTabIndex.value)
    }

    @Test
    fun goToPushesOntoCurrentTabStack() {
        val navigator = createNavigator()

        navigator.goTo(DetailA)
        assertEquals(listOf(RouteA, DetailA), navigator.currentBackStack.toList())
        assertTrue(navigator.isDetailVisible)
    }

    @Test
    fun goBackPopsCurrentTabStack() {
        val navigator = createNavigator()

        navigator.goTo(DetailA)
        assertTrue(navigator.isDetailVisible)

        navigator.goBack()
        assertEquals(listOf(RouteA), navigator.currentBackStack.toList())
        assertFalse(navigator.isDetailVisible)
    }

    @Test
    fun goBackDoesNothingAtRoot() {
        val navigator = createNavigator()

        navigator.goBack()
        assertEquals(listOf(RouteA), navigator.currentBackStack.toList())
        assertFalse(navigator.isDetailVisible)
    }

    @Test
    fun perTabBackStacksAreIndependent() {
        val navigator = createNavigator()

        // Push onto tab A
        navigator.goTo(DetailA)
        assertEquals(2, navigator.currentBackStack.size)

        // Switch to tab B — should have only root
        navigator.selectTab(1)
        assertEquals(listOf(RouteB), navigator.currentBackStack.toList())
        assertFalse(navigator.isDetailVisible)

        // Switch back to tab A — detail should still be there
        navigator.selectTab(0)
        assertEquals(listOf(RouteA, DetailA), navigator.currentBackStack.toList())
        assertTrue(navigator.isDetailVisible)
    }

    @Test
    fun isDetailVisibleDerivedFromStackDepth() {
        val navigator = createNavigator()

        assertFalse(navigator.isDetailVisible) // root only

        navigator.goTo(DetailA)
        assertTrue(navigator.isDetailVisible) // root + detail

        navigator.goBack()
        assertFalse(navigator.isDetailVisible) // back to root
    }

    @Test
    fun tabSwitchResetsDetailVisibilityForNewTab() {
        val navigator = createNavigator()

        // Push detail on tab A
        navigator.goTo(DetailA)
        assertTrue(navigator.isDetailVisible)

        // Switch to tab B — should show root, no detail
        navigator.selectTab(1)
        assertFalse(navigator.isDetailVisible)
    }
}
