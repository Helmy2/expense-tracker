package com.expense.tracker.feature.budget.data.repository

import com.expense.tracker.feature.budget.data.local.BudgetDao
import com.expense.tracker.feature.budget.data.local.BudgetEntity
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomBudgetRepositoryTest {

    private val fakeTimeProvider = FakeTimeProvider()

    private fun createRepository(
        budgets: List<BudgetEntity> = emptyList(),
        transactions: List<Transaction> = emptyList(),
    ): RoomBudgetRepository {
        val dao = FakeBudgetDao(budgets.toMutableList())
        val txRepo = FakeTransactionRepository(transactions)
        return RoomBudgetRepository(dao, txRepo, fakeTimeProvider)
    }

    @Test
    fun loadBudgetsReturnsAll() = runTest {
        val repo = createRepository(
            budgets = listOf(
                BudgetEntity("b1", "FOOD", 500.0, 100L, 100L),
                BudgetEntity("b2", "RENT", 1000.0, 200L, 200L),
            )
        )
        val result = repo.loadBudgets()
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).value.size)
        assertEquals("b2", result.value[0].id) // ordered by createdAtMillis DESC
        assertEquals("b1", result.value[1].id)
    }

    @Test
    fun loadBudgetByIdFound() = runTest {
        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L))
        )
        val result = repo.loadBudgetById("b1")
        assertTrue(result is Result.Success)
        assertEquals("b1", (result as Result.Success).value?.id)
    }

    @Test
    fun loadBudgetByIdNotFound() = runTest {
        val repo = createRepository()
        val result = repo.loadBudgetById("missing")
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).value)
    }

    @Test
    fun loadBudgetByCategory() = runTest {
        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L))
        )
        val result = repo.loadBudgetByCategory(TransactionCategory.FOOD)
        assertTrue(result is Result.Success)
        assertEquals("b1", (result as Result.Success).value?.id)
    }

    @Test
    fun createBudget() = runTest {
        fakeTimeProvider.setNowMillis(999L)
        val repo = createRepository()
        val result = repo.createBudget(TransactionCategory.SHOPPING, 200.0)
        assertTrue(result is Result.Success)
        val budget = (result as Result.Success).value
        assertEquals(TransactionCategory.SHOPPING, budget.category)
        assertEquals(200.0, budget.monthlyLimit)
        assertEquals(999L, budget.createdAtMillis)
        assertEquals(999L, budget.updatedAtMillis)
    }

    @Test
    fun updateBudget() = runTest {
        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L))
        )
        fakeTimeProvider.setNowMillis(999L)
        val result = repo.updateBudget("b1", 750.0)
        assertTrue(result is Result.Success)
        val budget = (result as Result.Success).value
        assertEquals(750.0, budget.monthlyLimit)
        assertEquals(999L, budget.updatedAtMillis)
    }

    @Test
    fun deleteBudget() = runTest {
        val dao = FakeBudgetDao(mutableListOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L)))
        val repo = RoomBudgetRepository(dao, FakeTransactionRepository(), fakeTimeProvider)
        val result = repo.deleteBudget("b1")
        assertTrue(result is Result.Success)
        assertNull(dao.getById("b1"))
    }

    @Test
    fun calculateSpendingFiltersCorrectly() = runTest {
        // Set current time to June 2026 15th
        // 2026-06-15 in UTC: year=2026, month=6
        fakeTimeProvider.setNowMillis(
            LocalDateTime(2026, 6, 15, 12, 0, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        )

        val transactions = listOf(
            Transaction("t1", 100.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "lunch", toMillis(2026, 6, 10)),
            Transaction("t2", 50.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "dinner", toMillis(2026, 6, 12)),
            Transaction("t3", 200.0, TransactionType.EXPENSE, TransactionCategory.RENT, "rent", toMillis(2026, 6, 1)),
            Transaction("t4", 75.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "snacks", toMillis(2026, 5, 20)), // different month
            Transaction("t5", 30.0, TransactionType.INCOME, TransactionCategory.SALARY, "bonus", toMillis(2026, 6, 5)), // income, not expense
        )
        val repo = createRepository(transactions = transactions)
        val result = repo.calculateSpending(TransactionCategory.FOOD)
        assertTrue(result is Result.Success)
        assertEquals(150.0, (result as Result.Success).value) // 100 + 50, not t4 (wrong month), not t5 (income)
    }

    private fun toMillis(year: Int, month: Int, day: Int): Long =
        LocalDateTime(year, month, day, 0, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
}

private class FakeBudgetDao(
    private val budgets: MutableList<BudgetEntity> = mutableListOf(),
) : BudgetDao {
    override suspend fun getAll(): List<BudgetEntity> = budgets.sortedByDescending { it.createdAtMillis }

    override suspend fun getById(id: String): BudgetEntity? = budgets.find { it.id == id }

    override suspend fun getByCategory(category: String): BudgetEntity? = budgets.find { it.category == category }

    override suspend fun insert(entity: BudgetEntity) {
        budgets.removeAll { it.id == entity.id }
        budgets.add(entity)
    }

    override suspend fun deleteById(id: String) {
        budgets.removeAll { it.id == id }
    }
}

private class FakeTransactionRepository(
    private val transactions: List<Transaction> = emptyList(),
) : TransactionRepository {
    override suspend fun loadTransactions(): Result<List<Transaction>> = Result.Success(transactions)
    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
    ): Result<Transaction> = TODO("Not used in tests")

    override suspend fun deleteTransaction(id: String): Result<Unit> = TODO("Not used in tests")
}
