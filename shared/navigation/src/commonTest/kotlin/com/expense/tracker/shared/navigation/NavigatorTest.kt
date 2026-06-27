package com.expense.tracker.shared.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

private data object TestRoute : NavKey

class NavigatorTest {
    @Test
    fun initializesWithStartDestination() {
        val navigator = Navigator(startDestination = TestRoute)

        assertEquals(listOf(TestRoute), navigator.backStack.toList())
    }

    @Test
    fun navigatesForwardAndBack() {
        val navigator = Navigator(startDestination = TestRoute)

        navigator.goTo(TestRoute)
        assertEquals(listOf(TestRoute, TestRoute), navigator.backStack.toList())

        navigator.goBack()
        assertEquals(listOf(TestRoute), navigator.backStack.toList())
    }

    @Test
    fun doesNotPopStartDestination() {
        val navigator = Navigator(startDestination = TestRoute)

        navigator.goBack()

        assertEquals(listOf(TestRoute), navigator.backStack.toList())
    }
}
