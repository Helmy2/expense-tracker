package com.expense.tracker.feature.expense.data.repository

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RoomTransactionRepositoryTest {
    @Test
    fun loadTransactionsReturnsAllFromDao() = runTest {
        val dao = FakeTransactionDao(
            TransactionEntity("txn-1", 100.0, "INCOME", "SALARY", "Monthly", 1_720_000_000_000),
            TransactionEntity("txn-2", 50.0, "EXPENSE", "FOOD", "Lunch", 1_720_000_001_000),
        )
        val repository = RoomTransactionRepository(
            dao = dao,
            timeProvider = FakeTimeProvider(current = 500L),
        )

        val result = assertIs<Result.Success<List<Transaction>>>(repository.loadTransactions()).value

        assertEquals(2, result.size)
        // DAO sorts by createdAtMillis DESC
        assertEquals("txn-2", result[0].id)
        assertEquals("txn-1", result[1].id)
    }

    @Test
    fun addTransactionInsertsThroughDaoWithCurrentTime() = runTest {
        val dao = FakeTransactionDao()
        val repository = RoomTransactionRepository(
            dao = dao,
            timeProvider = FakeTimeProvider(current = 500L),
        )

        val created = assertIs<Result.Success<Transaction>>(
            repository.addTransaction(
                amount = 42.50,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                note = "Lunch",
            ),
        ).value

        assertEquals(42.50, created.amount)
        assertEquals(TransactionType.EXPENSE, created.type)
        assertEquals(TransactionCategory.FOOD, created.category)
        assertEquals("Lunch", created.note)
        assertEquals(500L, created.createdAtMillis)
        assertEquals(1, dao.items.size)
        assertTrue(created.id.startsWith("500-"))
    }

    @Test
    fun addTransactionTrimsNote() = runTest {
        val dao = FakeTransactionDao()
        val repository = RoomTransactionRepository(
            dao = dao,
            timeProvider = FakeTimeProvider(current = 500L),
        )

        val created = assertIs<Result.Success<Transaction>>(
            repository.addTransaction(
                amount = 10.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                note = "  Dinner  ",
            ),
        ).value

        assertEquals("Dinner", created.note)
    }

    @Test
    fun deleteTransactionRemovesFromDao() = runTest {
        val dao = FakeTransactionDao(
            TransactionEntity("txn-1", 100.0, "INCOME", "SALARY", "Monthly", 1_720_000_000_000),
        )
        val repository = RoomTransactionRepository(
            dao = dao,
            timeProvider = FakeTimeProvider(current = 500L),
        )

        val result = repository.deleteTransaction("txn-1")

        assertIs<Result.Success<*>>(result)
        assertTrue(dao.items.isEmpty())
    }

    @Test
    fun repositoryReturnsFailureOnDaoException() = runTest {
        val dao = FailingTransactionDao()
        val repository = RoomTransactionRepository(
            dao = dao,
            timeProvider = FakeTimeProvider(current = 500L),
        )

        val result = repository.loadTransactions()

        assertIs<Result.Failure>(result)
    }
}

private class FakeTransactionDao(
    vararg seedItems: TransactionEntity,
) : TransactionDao {
    val items = seedItems.toMutableList()

    override suspend fun getAll(): List<TransactionEntity> = items.sortedByDescending { it.createdAtMillis }

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

private class FailingTransactionDao : TransactionDao {
    override suspend fun getAll(): List<TransactionEntity> = throw RuntimeException("DAO failure")

    override suspend fun insert(entity: TransactionEntity) = throw RuntimeException("DAO failure")

    override suspend fun deleteById(id: String) = throw RuntimeException("DAO failure")

    override suspend fun sumExpenseForCategory(
        category: String,
        startMillis: Long,
        endMillis: Long,
    ): Double = throw RuntimeException("DAO failure")
}
