package com.expense.tracker.feature.recurring.domain.repository

import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.shared.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecurringTemplateRepositoryTest {
    @Test
    fun loadTemplatesReturnsAll() = runTest {
        val repository = FakeRecurringTemplateRepository()

        val result = assertIs<Result.Success<List<RecurringTemplate>>>(repository.loadTemplates()).value

        assertTrue(result.isEmpty())
    }

    @Test
    fun createTemplateReturnsCreatedTemplate() = runTest {
        val repository = FakeRecurringTemplateRepository()

        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 1500.0,
                type = TransactionType.EXPENSE,
                category = "RENT",
                note = "Monthly rent",
                frequency = RecurringFrequency.MONTHLY,
                startDateMillis = 1_720_000_000_000,
                endDateMillis = null,
            ),
        ).value

        assertEquals(1500.0, created.amount)
        assertEquals(TransactionType.EXPENSE, created.type)
        assertEquals("RENT", created.category)
        assertEquals(RecurringFrequency.MONTHLY, created.frequency)
        assertEquals(false, created.isPaused)
    }

    @Test
    fun loadTemplateByIdReturnsTemplate() = runTest {
        val repository = FakeRecurringTemplateRepository()
        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "FOOD",
                note = "Groceries",
                frequency = RecurringFrequency.WEEKLY,
                startDateMillis = 1_720_000_000_000,
                endDateMillis = null,
            ),
        ).value

        val loaded = assertIs<Result.Success<RecurringTemplate?>>(
            repository.loadTemplateById(created.id),
        ).value

        assertNotNull(loaded)
        assertEquals(created.id, loaded.id)
    }

    @Test
    fun updateTemplateModifiesFields() = runTest {
        val repository = FakeRecurringTemplateRepository()
        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "FOOD",
                note = "Groceries",
                frequency = RecurringFrequency.WEEKLY,
                startDateMillis = 1_720_000_000_000,
                endDateMillis = null,
            ),
        ).value

        val updated = assertIs<Result.Success<RecurringTemplate>>(
            repository.updateTemplate(
                id = created.id,
                amount = 200.0,
                type = TransactionType.EXPENSE,
                category = "UTILITIES",
                note = "Updated",
                frequency = RecurringFrequency.MONTHLY,
                startDateMillis = 1_730_000_000_000,
                endDateMillis = null,
            ),
        ).value

        assertEquals(200.0, updated.amount)
        assertEquals("UTILITIES", updated.category)
        assertEquals(RecurringFrequency.MONTHLY, updated.frequency)
    }

    @Test
    fun deleteTemplateRemovesIt() = runTest {
        val repository = FakeRecurringTemplateRepository()
        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "OTHER_EXPENSE",
                note = "Temp",
                frequency = RecurringFrequency.DAILY,
                startDateMillis = 1_720_000_000_000,
                endDateMillis = null,
            ),
        ).value

        val deleteResult = repository.deleteTemplate(created.id)
        assertIs<Result.Success<*>>(deleteResult)

        val loaded = assertIs<Result.Success<RecurringTemplate?>>(
            repository.loadTemplateById(created.id),
        ).value
        assertNull(loaded)
    }

    @Test
    fun togglePauseSwitchesPauseState() = runTest {
        val repository = FakeRecurringTemplateRepository()
        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "OTHER_EXPENSE",
                note = "Test",
                frequency = RecurringFrequency.MONTHLY,
                startDateMillis = 1_720_000_000_000,
                endDateMillis = null,
            ),
        ).value

        val paused = assertIs<Result.Success<RecurringTemplate>>(
            repository.togglePause(created.id),
        ).value
        assertTrue(paused.isPaused)

        val resumed = assertIs<Result.Success<RecurringTemplate>>(
            repository.togglePause(created.id),
        ).value
        assertEquals(false, resumed.isPaused)
    }
}

private class FakeRecurringTemplateRepository : RecurringTemplateRepository {
    private val templates = mutableListOf<RecurringTemplate>()
    private var nextId = 1L

    override suspend fun loadTemplates(): Result<List<RecurringTemplate>> =
        Result.Success(templates.toList())

    override suspend fun loadTemplateById(id: String): Result<RecurringTemplate?> =
        Result.Success(templates.find { it.id == id })

    override suspend fun createTemplate(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> {
        val template = RecurringTemplate(
            id = "fake-${nextId++}",
            amount = amount,
            type = type,
            category = category,
            note = note,
            frequency = frequency,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )
        templates.add(template)
        return Result.Success(template)
    }

    override suspend fun updateTemplate(
        id: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        endDateMillis: Long?,
    ): Result<RecurringTemplate> {
        val index = templates.indexOfFirst { it.id == id }
        if (index == -1) return Result.Failure(com.expense.tracker.shared.core.domain.AppError.Message("Not found"))
        val updated = templates[index].copy(
            amount = amount,
            type = type,
            category = category,
            note = note,
            frequency = frequency,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            updatedAtMillis = 1_730_000_000_000,
        )
        templates[index] = updated
        return Result.Success(updated)
    }

    override suspend fun deleteTemplate(id: String): Result<Unit> {
        templates.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun togglePause(id: String): Result<RecurringTemplate> {
        val index = templates.indexOfFirst { it.id == id }
        if (index == -1) return Result.Failure(com.expense.tracker.shared.core.domain.AppError.Message("Not found"))
        val current = templates[index]
        val updated = current.copy(isPaused = !current.isPaused, updatedAtMillis = 1_730_000_000_000)
        templates[index] = updated
        return Result.Success(updated)
    }

    override suspend fun processDueRecurring(): Result<Int> = Result.Success(0)

    override suspend fun loadUpcoming(count: Int): Result<List<com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring>> =
        Result.Success(emptyList())
}
