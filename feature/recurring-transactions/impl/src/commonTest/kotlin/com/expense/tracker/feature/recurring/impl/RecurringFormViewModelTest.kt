package com.expense.tracker.feature.recurring.impl

import app.cash.turbine.test
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringFormViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val timeProvider = FakeTimeProvider(current = 1700000000000L)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsWithDefaultsForCreateMode() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        assertNull(viewModel.state.value.templateId)
        assertEquals("", viewModel.state.value.amountText)
        assertEquals(TransactionType.EXPENSE, viewModel.state.value.selectedType)
        assertEquals(TransactionCategory.OTHER, viewModel.state.value.selectedCategory)
        assertEquals(RecurringFrequency.MONTHLY, viewModel.state.value.selectedFrequency)
        assertNotNull(viewModel.state.value.startDateMillis)
    }

    @Test
    fun amountChangedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.AmountChanged("1500"))
        advanceUntilIdle()

        assertEquals("1500", viewModel.state.value.amountText)
    }

    @Test
    fun typeSelectedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.TypeSelected(TransactionType.INCOME))
        advanceUntilIdle()

        assertEquals(TransactionType.INCOME, viewModel.state.value.selectedType)
    }

    @Test
    fun categorySelectedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.CategorySelected(TransactionCategory.FOOD))
        advanceUntilIdle()

        assertEquals(TransactionCategory.FOOD, viewModel.state.value.selectedCategory)
    }

    @Test
    fun frequencySelectedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.FrequencySelected(RecurringFrequency.WEEKLY))
        advanceUntilIdle()

        assertEquals(RecurringFrequency.WEEKLY, viewModel.state.value.selectedFrequency)
    }

    @Test
    fun noteChangedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.NoteChanged("Test note"))
        advanceUntilIdle()

        assertEquals("Test note", viewModel.state.value.noteText)
    }

    @Test
    fun startDateSelectedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.StartDateSelected(1701000000000L))
        advanceUntilIdle()

        assertEquals(1701000000000L, viewModel.state.value.startDateMillis)
    }

    @Test
    fun endDateSelectedUpdatesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.EndDateSelected(1800000000000L))
        advanceUntilIdle()

        assertEquals(1800000000000L, viewModel.state.value.endDateMillis)
    }

    @Test
    fun clearEndDateClearsEndDate() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.EndDateSelected(1800000000000L))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.endDateMillis)

        viewModel.onAction(RecurringFormAction.ClearEndDate)
        advanceUntilIdle()

        assertNull(viewModel.state.value.endDateMillis)
    }

    @Test
    fun saveWithInvalidAmountDoesNotCreate() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.Save)
        advanceUntilIdle()

        assertEquals(0, repository.createCount)
    }

    @Test
    fun saveWithValidDataCreatesTemplateAndSendsEvent() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.AmountChanged("1500"))
        viewModel.onAction(RecurringFormAction.TypeSelected(TransactionType.INCOME))
        viewModel.onAction(RecurringFormAction.CategorySelected(TransactionCategory.SALARY))
        viewModel.onAction(RecurringFormAction.NoteChanged("Monthly salary"))
        viewModel.onAction(RecurringFormAction.FrequencySelected(RecurringFrequency.MONTHLY))
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(RecurringFormAction.Save)
            advanceUntilIdle()

            assertEquals(1, repository.createCount)
            assertEquals(RecurringFormEvent.RecurringSaved, awaitItem())
        }
    }

    @Test
    fun setTemplateInEditModeLoadsAndPrefillsForm() = runTest(mainDispatcher) {
        val template = RecurringTemplate(
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
        )
        val repository = fakeRepository(listOf(template))
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        viewModel.onAction(RecurringFormAction.SetTemplate("rt-1"))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("rt-1", viewModel.state.value.templateId)
        assertEquals("1500.0", viewModel.state.value.amountText)
        assertEquals(TransactionType.INCOME, viewModel.state.value.selectedType)
        assertEquals(TransactionCategory.SALARY, viewModel.state.value.selectedCategory)
        assertEquals("Monthly salary", viewModel.state.value.noteText)
        assertEquals(RecurringFrequency.MONTHLY, viewModel.state.value.selectedFrequency)
        assertEquals(1700000000000L, viewModel.state.value.startDateMillis)
    }

    @Test
    fun toggleCategoryMenuTogglesState() = runTest(mainDispatcher) {
        val repository = fakeRepository(emptyList())
        val viewModel = RecurringFormViewModel(
            repository = repository,
            timeProvider = timeProvider,
        )

        assertFalse(viewModel.state.value.categoryMenuExpanded)

        viewModel.onAction(RecurringFormAction.ToggleCategoryMenu)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.categoryMenuExpanded)

        viewModel.onAction(RecurringFormAction.DismissCategoryMenu)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.categoryMenuExpanded)
    }
}

private fun fakeRepository(templates: List<RecurringTemplate>): FakeFormRecurringRepository =
    FakeFormRecurringRepository(templates)

private class FakeFormRecurringRepository(
    initialTemplates: List<RecurringTemplate>,
) : com.expense.tracker.feature.recurring.domain.repository.RecurringTemplateRepository {
    private val templates = initialTemplates.toMutableList()
    var createCount = 0

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
        createCount++
        val template = RecurringTemplate(
            id = "new-$createCount",
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
        return Result.Failure(com.expense.tracker.shared.core.domain.AppError.Message("Not found"))
    }

    override suspend fun deleteTemplate(id: String): Result<Unit> =
        Result.Success(Unit)

    override suspend fun togglePause(id: String): Result<RecurringTemplate> =
        Result.Success(templates.first { it.id == id })

    override suspend fun processDueRecurring(): Result<Int> =
        Result.Success(0)

    override suspend fun loadUpcoming(count: Int): Result<List<com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring>> =
        Result.Success(emptyList())
}
