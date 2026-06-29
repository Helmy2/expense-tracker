package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.domain.AppError as CoreAppError
import com.expense.tracker.shared.core.domain.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Regression test for the iOS bridge exception wrapping fix.
 *
 * Background: the iOS-side `*OrThrow` extensions are called from a
 * `suspend` continuation resumed on SKIE's `SwiftCoroutineDispatcher`.
 * When a non-`CancellationException` reaches the top of a `suspend` chain
 * without being wrapped, the process aborts via
 * `propagateExceptionFinalResort` because no `CoroutineExceptionHandler`
 * catches it and SKIE does not always convert arbitrary Kotlin exception
 * types into a Swift `Error`. The fix wraps every `*OrThrow` body in a
 * defensive `try/catch` that funnels unexpected `Throwable`s into a
 * `RuntimeException` with a structured message and the original as
 * `cause`.
 *
 * These tests pin the bridge contract on `loadBudgetsWithSpendingOrThrow`
 * (the extension exercised by the new `BudgetListViewModel` / budget
 * flows); the same pattern is used by every other `*OrThrow` extension in
 * this package.
 */
class BudgetRepositoryExtensionsTest {

    @Test
    fun loadBudgetsWithSpendingOrThrow_wrapsNonRuntimeExceptionInRuntimeException() = runTest {
        val underlying = IllegalStateException("kaboom")
        val repository = ThrowingBudgetRepository(loadBudgetsWithSpendingError = underlying)

        val thrown = assertFailsWith<RuntimeException> {
            repository.loadBudgetsWithSpendingOrThrow()
        }

        assertEquals(
            "BudgetRepository.loadBudgetsWithSpending failed: IllegalStateException: kaboom",
            thrown.message,
        )
        assertSame(underlying, thrown.cause, "original throwable must be preserved as cause")
    }

    @Test
    fun loadBudgetsWithSpendingOrThrow_wrapsNullPointerException() = runTest {
        val underlying = NullPointerException("dao returned null")
        val repository = ThrowingBudgetRepository(loadBudgetsWithSpendingError = underlying)

        val thrown = assertFailsWith<RuntimeException> {
            repository.loadBudgetsWithSpendingOrThrow()
        }

        assertTrue(
            thrown.message!!.contains("NullPointerException"),
            "wrapper message should expose original exception type: ${thrown.message}",
        )
        assertTrue(
            thrown.message!!.contains("BudgetRepository.loadBudgetsWithSpending"),
            "wrapper message should include the operation name: ${thrown.message}",
        )
        assertSame(underlying, thrown.cause)
    }

    @Test
    fun loadBudgetsWithSpendingOrThrow_rethrowsCancellationExceptionUnchanged() = runTest {
        val cancellationException = CancellationException("cancelled by parent")
        val repository = ThrowingBudgetRepository(loadBudgetsWithSpendingError = cancellationException)

        val thrown = assertFailsWith<CancellationException> {
            repository.loadBudgetsWithSpendingOrThrow()
        }

        // CancellationException must NOT be wrapped — structured concurrency depends on it.
        assertSame(cancellationException, thrown, "CancellationException must propagate unchanged")
    }

    @Test
    fun loadBudgetsWithSpendingOrThrow_convertsResultFailureToRuntimeException() = runTest {
        val repository = ResultFailureBudgetRepository(
            error = CoreAppError.Message(value = "repository offline"),
        )

        val thrown = assertFailsWith<RuntimeException> {
            repository.loadBudgetsWithSpendingOrThrow()
        }

        // Result.Failure path always throws RuntimeException(result.error.toString()).
        // Confirm the original error string is in the message.
        assertTrue(
            thrown.message!!.contains("repository offline"),
            "Result.Failure message should contain the underlying error: ${thrown.message}",
        )
    }

    @Test
    fun loadBudgetsWithSpendingOrThrow_returnsValueOnSuccess() = runTest {
        val repository = StubBudgetRepository(
            withSpending = listOf(
                BudgetWithSpending(
                    budget = Budget(
                        id = "b1",
                        category = TransactionCategory.FOOD,
                        monthlyLimit = 500.0,
                        createdAtMillis = 1L,
                        updatedAtMillis = 1L,
                    ),
                    spentAmount = 100.0,
                    remainingAmount = 400.0,
                    percentage = 0.2,
                    status = com.expense.tracker.feature.budget.domain.model.BudgetStatus.UNDER_75,
                ),
            ),
        )

        val result = repository.loadBudgetsWithSpendingOrThrow()

        assertEquals(1, result.size)
        assertEquals("b1", result.first().budget.id)
    }

    @Test
    fun loadBudgetsWithSpendingOrThrow_wrappedExceptionHasOriginalAsCauseForDiagnostics() = runTest {
        val underlying = IllegalArgumentException("bad year-month window")
        val repository = ThrowingBudgetRepository(loadBudgetsWithSpendingError = underlying)

        val thrown = assertFailsWith<RuntimeException> {
            repository.loadBudgetsWithSpendingOrThrow()
        }

        // Stack-trace fidelity matters for the iOS log capture; the original
        // throwable must remain reachable through .cause for log scraping.
        val cause = thrown.cause
        assertNotNull(cause, "cause must not be null")
        assertSame(underlying, cause)
    }
}

/**
 * Minimal `BudgetRepository` that always throws the given [error] from
 * `loadBudgetsWithSpending()`. All other methods return
 * `Result.Failure(Unknown)` so the test stays focused on the method
 * under test.
 */
private class ThrowingBudgetRepository(
    private val loadBudgetsWithSpendingError: Throwable,
) : BudgetRepository {
    override suspend fun loadBudgets(): Result<List<Budget>> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun loadBudgetById(id: String): Result<Budget?> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun createBudget(category: TransactionCategory, monthlyLimit: Double): Result<Budget> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun updateBudget(id: String, monthlyLimit: Double): Result<Budget> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun deleteBudget(id: String): Result<Unit> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>> {
        throw loadBudgetsWithSpendingError
    }

    override suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?> =
        Result.Failure(CoreAppError.Unknown)
}

private class ResultFailureBudgetRepository(
    private val error: CoreAppError,
) : BudgetRepository {
    override suspend fun loadBudgets(): Result<List<Budget>> = Result.Failure(error)

    override suspend fun loadBudgetById(id: String): Result<Budget?> = Result.Failure(error)

    override suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?> =
        Result.Failure(error)

    override suspend fun createBudget(category: TransactionCategory, monthlyLimit: Double): Result<Budget> =
        Result.Failure(error)

    override suspend fun updateBudget(id: String, monthlyLimit: Double): Result<Budget> =
        Result.Failure(error)

    override suspend fun deleteBudget(id: String): Result<Unit> = Result.Failure(error)

    override suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>> =
        Result.Failure(error)

    override suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?> =
        Result.Failure(error)
}

private class StubBudgetRepository(
    private val withSpending: List<BudgetWithSpending>,
) : BudgetRepository {
    override suspend fun loadBudgets(): Result<List<Budget>> = Result.Success(emptyList())

    override suspend fun loadBudgetById(id: String): Result<Budget?> = Result.Success(null)

    override suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?> =
        Result.Success(null)

    override suspend fun createBudget(category: TransactionCategory, monthlyLimit: Double): Result<Budget> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun updateBudget(id: String, monthlyLimit: Double): Result<Budget> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun deleteBudget(id: String): Result<Unit> =
        Result.Failure(CoreAppError.Unknown)

    override suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>> =
        Result.Success(withSpending)

    override suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?> =
        Result.Failure(CoreAppError.Unknown)
}
