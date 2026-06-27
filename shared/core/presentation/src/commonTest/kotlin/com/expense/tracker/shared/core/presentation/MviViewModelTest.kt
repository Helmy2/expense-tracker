package com.expense.tracker.shared.core.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {
    @Test
    fun processesActionsSequentially() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val viewModel = RecordingViewModel()

        viewModel.onAction(1)
        advanceUntilIdle()

        assertEquals(listOf(1), viewModel.processedActions)
        assertEquals(1, viewModel.state.value)

        viewModel.onAction(2)
        advanceUntilIdle()

        assertEquals(listOf(1, 2), viewModel.processedActions)
        assertEquals(2, viewModel.state.value)
    }

    private class RecordingViewModel : MviViewModel<Int, Int, Nothing>(
        initialState = 0,
    ) {
        val processedActions = mutableListOf<Int>()

        override suspend fun handleAction(action: Int) {
            processedActions += action
            if (action == 1) {
                delay(50)
            }
            updateState { action }
        }
    }
}
