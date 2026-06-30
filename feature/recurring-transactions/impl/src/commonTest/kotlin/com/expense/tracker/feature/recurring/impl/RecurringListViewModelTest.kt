package com.expense.tracker.feature.recurring.impl

import app.cash.turbine.test
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringListViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val mapper = RecurringPresentationMapper(FakeTimeProvider())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsInLoadingState() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = emptyList())
        val viewModel = RecurringListViewModel(repository, mapper)

        assertIs<RecurringListContentState.Loading>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithEmptyListTransitionsToEmpty() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = emptyList())
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.Load)
        advanceUntilIdle()

        assertIs<RecurringListContentState.Empty>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithTemplatesTransitionsToContent() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = sampleTemplates())
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<RecurringListContentState.Content>(viewModel.state.value.contentState)
        assertEquals(2, contentState.templates.size)
    }

    @Test
    fun loadErrorExposesErrorState() = runTest(mainDispatcher) {
        val repository = mock<RecurringTemplateRepository> {
            everySuspend { loadTemplates() } returns Result.Failure(AppError.Message("Database error"))
        }
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<RecurringListContentState.Error>(viewModel.state.value.contentState)
        assertEquals(AppError.Message("Database error"), contentState.error)
    }

    @Test
    fun togglePauseCallsRepositoryAndReloads() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = sampleTemplates())
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.Load)
        advanceUntilIdle()

        assertEquals(0, repository.togglePauseCount)

        viewModel.onAction(RecurringListAction.TogglePause("rt-1"))
        advanceUntilIdle()

        assertEquals(1, repository.togglePauseCount)
    }

    @Test
    fun showDeleteDialogSetsDialogState() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = sampleTemplates())
        val viewModel = RecurringListViewModel(repository, mapper)

        assertNull(viewModel.state.value.deleteDialogTemplateId)

        viewModel.onAction(RecurringListAction.ShowDeleteDialog("rt-1"))
        advanceUntilIdle()

        assertEquals("rt-1", viewModel.state.value.deleteDialogTemplateId)
    }

    @Test
    fun confirmDeleteAfterShowDeletesAndClearsDialog() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = sampleTemplates())
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.ShowDeleteDialog("rt-1"))
        advanceUntilIdle()
        assertEquals("rt-1", viewModel.state.value.deleteDialogTemplateId)

        viewModel.eventFlow.test {
            viewModel.onAction(RecurringListAction.ConfirmDelete)
            advanceUntilIdle()

            assertNull(viewModel.state.value.deleteDialogTemplateId)
            assertEquals(1, repository.deleteCount)
            assertEquals(RecurringListEvent.TemplateDeleted, awaitItem())
        }
    }

    @Test
    fun dismissDeleteDialogClearsDialogState() = runTest(mainDispatcher) {
        val repository = fakeRepository(templates = sampleTemplates())
        val viewModel = RecurringListViewModel(repository, mapper)

        viewModel.onAction(RecurringListAction.ShowDeleteDialog("rt-1"))
        advanceUntilIdle()
        assertEquals("rt-1", viewModel.state.value.deleteDialogTemplateId)

        viewModel.onAction(RecurringListAction.DismissDeleteDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.deleteDialogTemplateId)
    }
}

private fun sampleTemplates(): List<RecurringTemplate> = listOf(
    RecurringTemplate(
        id = "rt-1",
        amount = 1500.0,
        type = TransactionType.INCOME,
        category = TransactionCategory.SALARY,
        note = "Monthly salary",
        frequency = RecurringFrequency.MONTHLY,
        startDateMillis = 1700000000000L,
        endDateMillis = null,
        isPaused = false,
        lastGeneratedDateMillis = null,
        createdAtMillis = 1690000000000L,
        updatedAtMillis = 1690000000000L,
    ),
    RecurringTemplate(
        id = "rt-2",
        amount = 45.0,
        type = TransactionType.EXPENSE,
        category = TransactionCategory.ENTERTAINMENT,
        note = "Netflix",
        frequency = RecurringFrequency.MONTHLY,
        startDateMillis = 1700000000000L,
        endDateMillis = null,
        isPaused = true,
        lastGeneratedDateMillis = null,
        createdAtMillis = 1690000000000L,
        updatedAtMillis = 1690000000000L,
    ),
)

private fun fakeRepository(templates: List<RecurringTemplate>): FakeRecurringRepository =
    FakeRecurringRepository(templates)

private class FakeRecurringRepository(
    initialTemplates: List<RecurringTemplate>,
) : RecurringTemplateRepository {
    private val templates = initialTemplates.toMutableList()
    var togglePauseCount = 0
    var deleteCount = 0

    override suspend fun loadTemplates(): Result<List<RecurringTemplate>> =
        Result.Success(templates.toList())

    override suspend fun loadTemplateById(id: String): Result<RecurringTemplate?> =
        Result.Success(templates.find { it.id == id })

    override suspend fun createTemplate(
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> {
        val template = RecurringTemplate(
            id = "new-${templates.size + 1}",
            amount = amount,
            type = type,
            category = category,
            note = note,
            frequency = frequency,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 5000L,
            updatedAtMillis = 5000L,
        )
        templates.add(template)
        return Result.Success(template)
    }

    override suspend fun updateTemplate(
        id: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> {
        val index = templates.indexOfFirst { it.id == id }
        if (index >= 0) {
            val updated = templates[index].copy(
                amount = amount,
                type = type,
                category = category,
                note = note,
                frequency = frequency,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                updatedAtMillis = 5000L,
            )
            templates[index] = updated
            return Result.Success(updated)
        }
        return Result.Failure(AppError.Message("Not found"))
    }

    override suspend fun deleteTemplate(id: String): Result<Unit> {
        deleteCount++
        templates.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun togglePause(id: String): Result<RecurringTemplate> {
        togglePauseCount++
        val index = templates.indexOfFirst { it.id == id }
        if (index >= 0) {
            val toggled = templates[index].copy(isPaused = !templates[index].isPaused)
            templates[index] = toggled
            return Result.Success(toggled)
        }
        return Result.Failure(AppError.Message("Not found"))
    }

    override suspend fun processDueRecurring(): Result<Int> =
        Result.Success(0)

    override suspend fun loadUpcoming(count: Int): Result<List<com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring>> =
        Result.Success(emptyList())
}
