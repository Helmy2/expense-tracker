package com.expense.tracker.feature.budget.data.repository

import com.expense.tracker.feature.budget.data.mapper.toDomain
import com.expense.tracker.feature.budget.data.mapper.toEntity
import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.budget.domain.model.withSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.data.dao.BudgetDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.runSuspendCatching

class RoomBudgetRepository(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val timeProvider: TimeProvider,
) : BudgetRepository {

    override suspend fun loadBudgets(): Result<List<Budget>> = runSuspendCatching(
        block = {
            budgetDao.getAll().map { it.toDomain() }
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadBudgetById(id: String): Result<Budget?> = runSuspendCatching(
        block = {
            budgetDao.getById(id)?.toDomain()
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadBudgetByCategory(category: ExpenseCategory): Result<Budget?> = runSuspendCatching(
        block = {
            budgetDao.getByCategory(category.name)?.toDomain()
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun createBudget(
        category: ExpenseCategory,
        monthlyLimit: Double,
    ): Result<Budget> = runSuspendCatching(
        block = {
            val now = timeProvider.nowMillis()
            val budget = Budget(
                id = generateId(),
                category = category,
                monthlyLimit = monthlyLimit,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
            budgetDao.insert(budget.toEntity())
            budget
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun updateBudget(
        id: String,
        monthlyLimit: Double,
    ): Result<Budget> = runSuspendCatching(
        block = {
            val existing = budgetDao.getById(id)
                ?: throw IllegalStateException("Budget not found: $id")
            val updated = existing.toDomain().copy(
                monthlyLimit = monthlyLimit,
                updatedAtMillis = timeProvider.nowMillis(),
            )
            budgetDao.insert(updated.toEntity())
            updated
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun deleteBudget(id: String): Result<Unit> = runSuspendCatching(
        block = {
            budgetDao.deleteById(id)
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>> = runSuspendCatching(
        block = {
            val budgets = budgetDao.getAll().map { it.toDomain() }
            val range = timeProvider.yearMonthRangeMillis(timeProvider.currentYearMonth())
            budgets.map { budget ->
                val spent = transactionDao.sumExpenseForCategory(
                    category = budget.category.name,
                    startMillis = range.first,
                    endMillis = range.last + 1,
                )
                budget.withSpending(spent)
            }
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?> = runSuspendCatching(
        block = {
            val entity = budgetDao.getById(id)
            if (entity == null) {
                null
            } else {
                buildBudgetDetail(entity.toDomain())
            }
        },
        onFailure = { AppError.Unknown },
    )

    private suspend fun buildBudgetDetail(budget: Budget): BudgetDetail {
        val range = timeProvider.yearMonthRangeMillis(timeProvider.currentYearMonth())
        val spent = transactionDao.sumExpenseForCategory(
            category = budget.category.name,
            startMillis = range.first,
            endMillis = range.last + 1,
        )
        val budgetWithSpending = budget.withSpending(spent)
        val transactions = transactionDao.getAll()
            .filter { it.type == TransactionType.EXPENSE.name }
            .filter { it.category == budget.category.name }
            .filter { it.createdAtMillis in range }
            .sortedByDescending { it.createdAtMillis }
            .map { it.toDomain() }
        return BudgetDetail(
            budgetWithSpending = budgetWithSpending,
            transactions = transactions,
        )
    }

    private fun generateId(): String = "${timeProvider.nowMillis()}-${kotlin.random.Random.nextLong()}"
}
