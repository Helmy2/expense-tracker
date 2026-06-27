package com.expense.tracker.feature.sample.impl

import app.cash.turbine.test
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.feature.sample.domain.model.SampleItem
import com.expense.tracker.feature.sample.domain.repository.SampleRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SampleViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsInLoadingStateWhileLoadIsRunning() = runTest(mainDispatcher) {
        val releaseLoad = CompletableDeferred<Unit>()
        val repository = mock<SampleRepository> {
            everySuspend { loadItems() } calls {
                releaseLoad.await()
                Result.Success(emptyList())
            }
        }
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        backgroundScope.launch { viewModel.load(force = true) }
        runCurrent()

        assertIs<SampleContentState.Loading>(viewModel.state.value.contentState)

        releaseLoad.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun loadsStarterCardsAsUiContent() = runTest(mainDispatcher) {
        val repository = fakeRepository(items = sampleItems())
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        viewModel.state.test {
            assertIs<SampleContentState.Loading>(awaitItem().contentState)

            viewModel.load(force = true)

            val contentState = assertIs<SampleContentState.Content>(awaitItem().contentState)
            assertEquals(3, contentState.items.size)
            assertEquals(SampleCategory.Contract, contentState.items.first().category)
            assertEquals("Jan 1, 1970", contentState.items.first().occurredLabel)
        }
    }

    @Test
    fun loadsEmptyStateWhenRepositoryHasNoItems() = runTest(mainDispatcher) {
        val repository = fakeRepository(items = emptyList())
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        viewModel.state.test {
            assertIs<SampleContentState.Loading>(awaitItem().contentState)

            viewModel.load(force = true)

            assertIs<SampleContentState.Empty>(awaitItem().contentState)
        }
    }

    @Test
    fun selectsItemAndEmitsDetailNavigation() = runTest(mainDispatcher) {
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = fakeRepository(items = sampleItems()), timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))
        viewModel.load(force = true)

        viewModel.eventFlow.test {
            viewModel.onAction(SampleAction.SelectItem("shared-architecture"))
            advanceUntilIdle()

            assertEquals(SampleEvent.NavigateToDetail("shared-architecture"), awaitItem())
            assertEquals(SampleTextUi.Raw("Shared architecture"), viewModel.state.value.detailState?.item?.title)
        }
    }

    @Test
    fun cancelEditReturnsToReadOnlyDetailAndClearsValidationErrors() = runTest(mainDispatcher) {
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = fakeRepository(items = sampleItems()), timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))
        viewModel.onAction(SampleAction.SelectItem("contract-first", navigate = false))
        advanceUntilIdle()

        viewModel.onAction(SampleAction.StartEdit)
        viewModel.onAction(SampleAction.TitleChanged(""))
        viewModel.onAction(SampleAction.DescriptionChanged(""))
        viewModel.onAction(SampleAction.Save)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.formState.titleError)
        assertTrue(viewModel.state.value.formState.descriptionError)

        viewModel.onAction(SampleAction.CancelEdit)
        advanceUntilIdle()

        val detailState = viewModel.state.value.detailState
        assertEquals(false, detailState?.isEditing)
        assertEquals(false, viewModel.state.value.formState.titleError)
        assertEquals(false, viewModel.state.value.formState.descriptionError)
        assertEquals(SampleTextUi.Raw("Contract-first features"), detailState?.item?.title)
    }

    @Test
    fun saveValidationMarksBlankFieldsWithoutCallingRepository() = runTest(mainDispatcher) {
        val repository = fakeRepository(items = sampleItems())
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        viewModel.onAction(SampleAction.StartCreate)
        viewModel.onAction(SampleAction.Save)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.formState.titleError)
        assertTrue(viewModel.state.value.formState.descriptionError)
        assertEquals(0, repository.createCount)
    }

    @Test
    fun createsItemAndEmitsSaveSuccess() = runTest(mainDispatcher) {
        val repository = fakeRepository(items = sampleItems())
        val timeProvider = FakeTimeProvider(current = 500L)
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        viewModel.eventFlow.test {
            viewModel.onAction(SampleAction.StartCreate)
            viewModel.onAction(SampleAction.TitleChanged("New example"))
            viewModel.onAction(SampleAction.DescriptionChanged("Shows create behavior"))
            viewModel.onAction(SampleAction.CategoryChanged(SampleCategory.Preview))
            viewModel.onAction(SampleAction.Save)
            advanceUntilIdle()

            assertEquals(SampleEvent.SaveSucceeded, awaitItem())
            assertEquals(SampleTextUi.Raw("New example"), viewModel.state.value.detailState?.item?.title)
            assertEquals(1, repository.createCount)
        }
    }

    @Test
    fun updatesSelectedItemAndEmitsSaveSuccess() = runTest(mainDispatcher) {
        val repository = fakeRepository(items = sampleItems())
        val timeProvider = FakeTimeProvider(current = 700L)
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))
        viewModel.onAction(SampleAction.SelectItem("contract-first"))
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(SampleAction.StartEdit)
            viewModel.onAction(SampleAction.TitleChanged("Updated contract"))
            viewModel.onAction(SampleAction.DescriptionChanged("Updated detail"))
            viewModel.onAction(SampleAction.Save)
            advanceUntilIdle()

            assertEquals(SampleEvent.SaveSucceeded, awaitItem())
            assertEquals(SampleTextUi.Raw("Updated contract"), viewModel.state.value.detailState?.item?.title)
            assertEquals(1, repository.updateCount)
        }
    }

    @Test
    fun exposesErrorState() = runTest(mainDispatcher) {
        val repository = mock<SampleRepository> {
            everySuspend { loadItems() } returns Result.Failure(AppError.Message("No data source"))
        }
        val timeProvider = FakeTimeProvider()
        val viewModel = SampleViewModel(repository = repository, timeProvider = timeProvider, mapper = SamplePresentationMapper(timeProvider))

        viewModel.state.test {
            assertIs<SampleContentState.Loading>(awaitItem().contentState)

            viewModel.load(force = true)

            val contentState = assertIs<SampleContentState.Error>(awaitItem().contentState)
            assertEquals(AppError.Message("No data source"), contentState.error)
        }
    }
}

private fun sampleItems(): List<SampleItem> = listOf(
    SampleItem("contract-first", "Contract-first features", "Generate compact contracts.", SampleCategory.Contract, 0L, 0L),
    SampleItem("shared-architecture", "Shared architecture", "Keep feature logic split.", SampleCategory.Architecture, 1_000L, 1_000L),
    SampleItem("preview-workflow", "Preview workflow", "Use mobile previews.", SampleCategory.Preview, 2_000L, 2_000L),
)

private fun fakeRepository(items: List<SampleItem>): FakeSampleRepository = FakeSampleRepository(items)

private class FakeSampleRepository(
    initialItems: List<SampleItem>,
) : SampleRepository {
    private val items = initialItems.toMutableList()
    var createCount = 0
    var updateCount = 0

    override suspend fun loadItems(): Result<List<SampleItem>> = Result.Success(items)

    override suspend fun loadItem(id: String): Result<SampleItem?> = Result.Success(items.firstOrNull { item -> item.id == id })

    override suspend fun createItem(
        title: String,
        description: String,
        category: SampleCategory,
    ): Result<SampleItem> {
        createCount++
        val item = SampleItem("created-$createCount", title.trim(), description.trim(), category, 500L, 500L)
        items.add(item)
        return Result.Success(item)
    }

    override suspend fun updateItem(item: SampleItem): Result<SampleItem> {
        updateCount++
        val index = items.indexOfFirst { existing -> existing.id == item.id }
        if (index >= 0) {
            items[index] = item
        }
        return Result.Success(item)
    }
}
