package com.expense.tracker.feature.budget.data.repository

import com.expense.tracker.feature.budget.data.mapper.toDomain
import com.expense.tracker.feature.budget.data.mapper.toEntity
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.data.dao.BudgetDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.entity.BudgetEntity
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomBudgetRepositoryTest {

    private val fakeTimeProvider = FakeTimeProvider()

    private fun createRepository(
        budgets: List<BudgetEntity> = emptyList(),
        transactions: List<TransactionEntity> = emptyList(),
        timeProvider: FakeTimeProvider = fakeTimeProvider,
    ): RoomBudgetRepository {
        val dao = FakeBudgetDao(budgets.toMutableList())
        val txDao = FakeTransactionDao(transactions.toMutableList())
        return RoomBudgetRepository(dao, txDao, timeProvider)
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
        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L))
        )
        val result = repo.deleteBudget("b1")
        assertTrue(result is Result.Success)
    }

    @Test
    fun loadBudgetsWithSpendingReturnsEmptyForEmptyBudgets() = runTest {
        val repo = createRepository()
        val result = repo.loadBudgetsWithSpending()
        assertTrue(result is Result.Success)
        assertEquals(emptyList(), (result as Result.Success).value)
    }

    @Test
    fun loadBudgetsWithSpendingReturnsZeroSpentWhenNoTransactionsInCurrentMonth() = runTest {
        // Set current time to June 2026 15th
        fakeTimeProvider.setNowMillis(
            LocalDateTime(2026, 6, 15, 12, 0, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        )

        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L))
        )
        val result = repo.loadBudgetsWithSpending()
        assertTrue(result is Result.Success)
        val list = (result as Result.Success).value
        assertEquals(1, list.size)
        assertEquals(0.0, list[0].spentAmount)
        assertEquals(500.0, list[0].remainingAmount)
    }

    @Test
    fun loadBudgetsWithSpendingOnlyCountsCurrentMonth() = runTest {
        fakeTimeProvider.setNowMillis(
            LocalDateTime(2026, 6, 15, 12, 0, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        )

        val transactions = listOf(
            TransactionEntity("t1", 100.0, "EXPENSE", "FOOD", "lunch", toMillis(2026, 6, 10)),
            TransactionEntity("t2", 50.0, "EXPENSE", "FOOD", "dinner", toMillis(2026, 6, 12)),
            TransactionEntity("t3", 200.0, "EXPENSE", "RENT", "rent", toMillis(2026, 6, 1)),
            TransactionEntity("t4", 75.0, "EXPENSE", "FOOD", "snacks", toMillis(2026, 5, 20)), // wrong month
            TransactionEntity("t5", 30.0, "INCOME", "SALARY", "bonus", toMillis(2026, 6, 5)), // income, not expense
        )
        val repo = createRepository(
            budgets = listOf(
                BudgetEntity("b1", "FOOD", 500.0, 100L, 100L),
                BudgetEntity("b2", "RENT", 1500.0, 200L, 200L),
            ),
            transactions = transactions,
        )
        val result = repo.loadBudgetsWithSpending()
        assertTrue(result is Result.Success)
        val list = (result as Result.Success).value
        val food = list.first { it.budget.category == TransactionCategory.FOOD }
        val rent = list.first { it.budget.category == TransactionCategory.RENT }
        // Only t1 + t2 (100 + 50); t4 wrong month, t5 income
        assertEquals(150.0, food.spentAmount)
        // t3 only
        assertEquals(200.0, rent.spentAmount)
    }

    @Test
    fun loadBudgetDetailReturnsNullForMissingId() = runTest {
        val repo = createRepository()
        val result = repo.loadBudgetDetail("missing")
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).value)
    }

    @Test
    fun loadBudgetDetailReturnsBudgetWithSpendingAndSortedTransactions() = runTest {
        fakeTimeProvider.setNowMillis(
            LocalDateTime(2026, 6, 15, 12, 0, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        )

        val transactions = listOf(
            TransactionEntity("t1", 100.0, "EXPENSE", "FOOD", "lunch early", toMillis(2026, 6, 1)),
            TransactionEntity("t2", 50.0, "EXPENSE", "FOOD", "dinner late", toMillis(2026, 6, 12)),
            TransactionEntity("t3", 200.0, "EXPENSE", "RENT", "rent", toMillis(2026, 6, 5)),
            TransactionEntity("t4", 75.0, "EXPENSE", "FOOD", "snacks", toMillis(2026, 5, 20)), // wrong month
            TransactionEntity("t5", 30.0, "INCOME", "SALARY", "bonus", toMillis(2026, 6, 10)), // income
        )
        val repo = createRepository(
            budgets = listOf(BudgetEntity("b1", "FOOD", 500.0, 100L, 100L)),
            transactions = transactions,
        )
        val result = repo.loadBudgetDetail("b1")
        val detail = assertNotNull((result as Result.Success).value)
        assertEquals(TransactionCategory.FOOD, detail.budgetWithSpending.budget.category)
        // 100 + 50 = 150 (only t1 and t2 are EXPENSE in FOOD in current month)
        assertEquals(150.0, detail.budgetWithSpending.spentAmount)
        assertEquals(2, detail.transactions.size)
        // sorted by createdAtMillis DESC
        assertEquals("t2", detail.transactions[0].id)
        assertEquals("t1", detail.transactions[1].id)
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

private class FakeTransactionDao(
    private val items: MutableList<TransactionEntity> = mutableListOf(),
) : TransactionDao {
    override suspend fun getAll(): List<TransactionEntity> = items.sortedByDescending { it.createdAtMillis }

    override suspend fun insert(entity: TransactionEntity) {
        items.removeAll { it.id == entity.id }
        items.add(entity)
    }

    override suspend fun deleteById(id: String) {
        items.removeAll { it.id == id }
    }

    override suspend fun sumExpenseForCategory(
        category: String,
        startMillis: Long,
        endMillis: Long,
    ): Double = items
        .filter { it.type == "EXPENSE" && it.category == category }
        .filter { it.createdAtMillis in startMillis until endMillis }
        .sumOf { it.amount }
}
