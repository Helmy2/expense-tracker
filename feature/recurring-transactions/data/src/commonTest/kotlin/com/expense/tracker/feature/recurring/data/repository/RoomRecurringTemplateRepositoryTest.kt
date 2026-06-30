package com.expense.tracker.feature.recurring.data.repository

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.shared.core.data.dao.RecurringTemplateDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.entity.RecurringTemplateEntity
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomRecurringTemplateRepositoryTest {
    private val startOfDay = 1_720_000_000_000L // 2024-07-03T00:00:00Z approx

    @Test
    fun loadTemplatesReturnsAll() = runTest {
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Test", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao)

        val result = assertIs<Result.Success<List<RecurringTemplate>>>(repository.loadTemplates()).value

        assertEquals(1, result.size)
        assertEquals("tmpl-1", result[0].id)
    }

    @Test
    fun createTemplateInsertsAndReturns() = runTest {
        val dao = FakeRecurringTemplateDao()
        val repository = createRepository(dao = dao)

        val created = assertIs<Result.Success<RecurringTemplate>>(
            repository.createTemplate(
                amount = 1500.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.RENT,
                note = "Monthly rent",
                frequency = RecurringFrequency.MONTHLY,
                startDateMillis = startOfDay,
                endDateMillis = null,
            ),
        ).value

        assertEquals(1500.0, created.amount)
        assertEquals(TransactionType.EXPENSE, created.type)
        assertEquals(TransactionCategory.RENT, created.category)
        assertEquals(RecurringFrequency.MONTHLY, created.frequency)
        assertEquals(false, created.isPaused)
        assertNull(created.lastGeneratedDateMillis)
        assertEquals(1, dao.items.size)
    }

    @Test
    fun loadTemplateByIdReturnsTemplate() = runTest {
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Test", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao)

        val loaded = assertIs<Result.Success<RecurringTemplate?>>(
            repository.loadTemplateById("tmpl-1"),
        ).value

        assertNotNull(loaded)
        assertEquals("tmpl-1", loaded.id)
    }

    @Test
    fun loadTemplateByIdReturnsNullForMissing() = runTest {
        val repository = createRepository()

        val loaded = assertIs<Result.Success<RecurringTemplate?>>(
            repository.loadTemplateById("nonexistent"),
        ).value

        assertNull(loaded)
    }

    @Test
    fun updateTemplateModifiesExisting() = runTest {
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Original", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao)

        val updated = assertIs<Result.Success<RecurringTemplate>>(
            repository.updateTemplate(
                id = "tmpl-1",
                amount = 200.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.UTILITIES,
                note = "Updated",
                frequency = RecurringFrequency.MONTHLY,
                startDateMillis = startOfDay + 86400000,
                endDateMillis = null,
            ),
        ).value

        assertEquals(200.0, updated.amount)
        assertEquals(TransactionCategory.UTILITIES, updated.category)
        assertEquals(RecurringFrequency.MONTHLY, updated.frequency)
        assertEquals(500L, updated.updatedAtMillis)
    }

    @Test
    fun deleteTemplateRemovesFromDao() = runTest {
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Test", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao)

        val result = repository.deleteTemplate("tmpl-1")
        assertIs<Result.Success<*>>(result)
        assertTrue(dao.items.isEmpty())
    }

    @Test
    fun togglePauseSwitchesPauseState() = runTest {
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Test", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao)

        val paused = assertIs<Result.Success<RecurringTemplate>>(
            repository.togglePause("tmpl-1"),
        ).value
        assertTrue(paused.isPaused)

        val resumed = assertIs<Result.Success<RecurringTemplate>>(
            repository.togglePause("tmpl-1"),
        ).value
        assertEquals(false, resumed.isPaused)
    }

    @Test
    fun processDueRecurringCreatesTransactionsForDueTemplates() = runTest {
        val timeProvider = FakeTimeProvider(current = startOfDay + 3 * 86400000L) // 3 days after start
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-daily", amount = 10.0, type = "EXPENSE", category = "FOOD",
                note = "Daily coffee", frequency = "DAILY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val txnDao = FakeTransactionDao()
        val repository = RoomRecurringTemplateRepository(
            templateDao = dao,
            transactionDao = txnDao,
            timeProvider = timeProvider,
        )

        val count = assertIs<Result.Success<Int>>(repository.processDueRecurring()).value

        // Should create 4 transactions: day 0, 1, 2, 3
        assertEquals(4, count)
        assertEquals(4, txnDao.items.size)
        assertEquals(10.0, txnDao.items.first().amount)
        assertEquals("DAILY", dao.items.first { it.id == "tmpl-daily" }.frequency)
    }

    @Test
    fun processDueRecurringSkipsPausedTemplates() = runTest {
        val timeProvider = FakeTimeProvider(current = startOfDay + 86400000L)
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-paused", amount = 10.0, type = "EXPENSE", category = "FOOD",
                note = "Paused", frequency = "DAILY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = true, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val txnDao = FakeTransactionDao()
        val repository = RoomRecurringTemplateRepository(
            templateDao = dao,
            transactionDao = txnDao,
            timeProvider = timeProvider,
        )

        val count = assertIs<Result.Success<Int>>(repository.processDueRecurring()).value

        assertEquals(0, count)
        assertTrue(txnDao.items.isEmpty())
    }

    @Test
    fun processDueRecurringRespectsLastGeneratedDate() = runTest {
        val timeProvider = FakeTimeProvider(current = startOfDay + 3 * 86400000L) // 3 days after start
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-daily", amount = 10.0, type = "EXPENSE", category = "FOOD",
                note = "Daily coffee", frequency = "DAILY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false,
                lastGeneratedDateMillis = startOfDay + 86400000L, // already generated up to day 1
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val txnDao = FakeTransactionDao()
        val repository = RoomRecurringTemplateRepository(
            templateDao = dao,
            transactionDao = txnDao,
            timeProvider = timeProvider,
        )

        val count = assertIs<Result.Success<Int>>(repository.processDueRecurring()).value

        // Should create 2 transactions: day 2 and day 3 (day 0 and 1 were already generated)
        assertEquals(2, count)
    }

    @Test
    fun loadUpcomingReturnsNextDueDates() = runTest {
        val timeProvider = FakeTimeProvider(current = startOfDay) // today = startOfDay
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Weekly", frequency = "WEEKLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
            RecurringTemplateEntity(
                id = "tmpl-2", amount = 200.0, type = "INCOME", category = "SALARY",
                note = "Monthly", frequency = "MONTHLY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = false, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao, timeProvider = timeProvider)

        val upcoming = assertIs<Result.Success<List<UpcomingRecurring>>>(
            repository.loadUpcoming(count = 5),
        ).value

        assertEquals(2, upcoming.size)
        assertEquals("tmpl-1", upcoming[0].templateId)
        assertEquals("tmpl-2", upcoming[1].templateId)
        assertTrue(upcoming[0].nextDueDateMillis > startOfDay)
    }

    @Test
    fun loadUpcomingReturnsEmptyWhenAllPaused() = runTest {
        val timeProvider = FakeTimeProvider(current = startOfDay)
        val dao = FakeRecurringTemplateDao(
            RecurringTemplateEntity(
                id = "tmpl-1", amount = 100.0, type = "EXPENSE", category = "FOOD",
                note = "Paused", frequency = "DAILY", startDateMillis = startOfDay,
                endDateMillis = null, isPaused = true, lastGeneratedDateMillis = null,
                createdAtMillis = startOfDay, updatedAtMillis = startOfDay,
            ),
        )
        val repository = createRepository(dao = dao, timeProvider = timeProvider)

        val upcoming = assertIs<Result.Success<List<UpcomingRecurring>>>(
            repository.loadUpcoming(),
        ).value

        assertTrue(upcoming.isEmpty())
    }

    @Test
    fun repositoryReturnsFailureOnDaoException() = runTest {
        val dao = FailingRecurringTemplateDao()
        val repository = createRepository(dao = dao)

        val result = repository.loadTemplates()

        assertIs<Result.Failure>(result)
    }

    private fun createRepository(
        dao: RecurringTemplateDao = FakeRecurringTemplateDao(),
        timeProvider: FakeTimeProvider = FakeTimeProvider(current = 500L),
        transactionDao: TransactionDao = FakeTransactionDao(),
    ): RoomRecurringTemplateRepository = RoomRecurringTemplateRepository(
        templateDao = dao,
        transactionDao = transactionDao,
        timeProvider = timeProvider,
    )
}

private class FakeRecurringTemplateDao(
    vararg seedItems: RecurringTemplateEntity,
) : RecurringTemplateDao {
    val items = seedItems.toMutableList()

    override suspend fun getAll(): List<RecurringTemplateEntity> = items.toList()

    override suspend fun getById(id: String): RecurringTemplateEntity? = items.find { it.id == id }

    override suspend fun getUnpaused(): List<RecurringTemplateEntity> =
        items.filter { !it.isPaused }

    override suspend fun insert(entity: RecurringTemplateEntity) {
        items.removeAll { existing -> existing.id == entity.id }
        items.add(entity)
    }

    override suspend fun updatePauseStatus(id: String, isPaused: Boolean) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(isPaused = isPaused)
        }
    }

    override suspend fun updateLastGenerated(id: String, lastGeneratedDateMillis: Long) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(lastGeneratedDateMillis = lastGeneratedDateMillis)
        }
    }

    override suspend fun deleteById(id: String) {
        items.removeAll { existing -> existing.id == id }
    }
}

private class FakeTransactionDao : TransactionDao {
    val items = mutableListOf<TransactionEntity>()

    override suspend fun getAll(): List<TransactionEntity> = items.toList()

    override suspend fun insert(entity: TransactionEntity) {
        items.removeAll { existing -> existing.id == entity.id }
        items.add(entity)
    }

    override suspend fun deleteById(id: String) {
        items.removeAll { existing -> existing.id == id }
    }

    override suspend fun sumExpenseForCategory(
        category: String,
        startMillis: Long,
        endMillis: Long,
    ): Double = items
        .filter { it.category == category && it.type == "EXPENSE" }
        .filter { it.createdAtMillis in startMillis until endMillis }
        .sumOf { it.amount }
}

private class FailingRecurringTemplateDao : RecurringTemplateDao {
    override suspend fun getAll(): List<RecurringTemplateEntity> =
        throw RuntimeException("DAO failure")

    override suspend fun getById(id: String): RecurringTemplateEntity? =
        throw RuntimeException("DAO failure")

    override suspend fun getUnpaused(): List<RecurringTemplateEntity> =
        throw RuntimeException("DAO failure")

    override suspend fun insert(entity: RecurringTemplateEntity) =
        throw RuntimeException("DAO failure")

    override suspend fun updatePauseStatus(id: String, isPaused: Boolean) =
        throw RuntimeException("DAO failure")

    override suspend fun updateLastGenerated(id: String, lastGeneratedDateMillis: Long) =
        throw RuntimeException("DAO failure")

    override suspend fun deleteById(id: String) =
        throw RuntimeException("DAO failure")
}
