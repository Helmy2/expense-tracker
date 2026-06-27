package com.expense.tracker.feature.budget.data.repository

import com.expense.tracker.feature.budget.data.local.BudgetDao
import com.expense.tracker.feature.budget.data.mapper.toDomain
import com.expense.tracker.feature.budget.data.mapper.toEntity
import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.YearMonth
import com.expense.tracker.shared.core.domain.runSuspendCatching
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RoomBudgetRepository(
    private val budgetDao: BudgetDao,
    private val transactionRepository: TransactionRepository,
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

    override suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?> = runSuspendCatching(
        block = {
            budgetDao.getByCategory(category.name)?.toDomain()
        },
        onFailure = { AppError.Unknown },
    )

    override suspend fun createBudget(
        category: TransactionCategory,
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

    suspend fun calculateSpending(category: TransactionCategory): Result<Double> = runSuspendCatching(
        block = {
            val yearMonth = timeProvider.currentYearMonth()
            val transactions = transactionRepository.loadTransactions()
            when (transactions) {
                is Result.Success -> transactions.value
                    .filter { it.type == TransactionType.EXPENSE }
                    .filter { it.category == category }
                    .filter {
                        val localDateTime = Instant.fromEpochMilliseconds(it.createdAtMillis)
                            .toLocalDateTime(timeProvider.timeZone())
                        val txYearMonth = YearMonth(
                            year = localDateTime.year,
                            month = localDateTime.monthNumber,
                        )
                        txYearMonth == yearMonth
                    }
                    .sumOf { it.amount }
                is Result.Failure -> 0.0
            }
        },
        onFailure = { AppError.Unknown },
    )

    private fun generateId(): String = "${timeProvider.nowMillis()}-${kotlin.random.Random.nextLong()}"
}
