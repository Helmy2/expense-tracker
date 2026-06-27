package com.expense.tracker.feature.budget.impl

import app.cash.turbine.test
import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetDetailViewModelTest {
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
    fun startsInLoadingState() = runTest(mainDispatcher) {
        val viewModel = BudgetDetailViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            transactionRepository = fakeTransactionRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        assertIs<BudgetDetailContentState.Loading>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithValidBudgetIdTransitionsToContent() = runTest(mainDispatcher) {
        val budget = Budget("b-1", TransactionCategory.FOOD, 100.0, 0L, 0L)
        val budgetRepo = fakeBudgetRepository(budgets = listOf(budget))
        val transactionRepo = fakeTransactionRepository(transactions = emptyList())
        val viewModel = BudgetDetailViewModel(
            budgetRepository = budgetRepo,
            transactionRepository = transactionRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetDetailAction.Load("b-1"))
        advanceUntilIdle()

        val contentState = assertIs<BudgetDetailContentState.Content>(viewModel.state.value.contentState)
        assertEquals("b-1", contentState.detail.budgetWithSpending.budget.id)
    }

    @Test
    fun loadWithInvalidBudgetIdTransitionsToError() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val transactionRepo = fakeTransactionRepository(transactions = emptyList())
        val viewModel = BudgetDetailViewModel(
            budgetRepository = budgetRepo,
            transactionRepository = transactionRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetDetailAction.Load("nonexistent-id"))
        advanceUntilIdle()

        assertIs<BudgetDetailContentState.Error>(viewModel.state.value.contentState)
    }

    @Test
    fun loadFiltersTransactionsToCurrentMonth() = runTest(mainDispatcher) {
        val budget = Budget("b-1", TransactionCategory.FOOD, 100.0, 0L, 0L)
        val budgetRepo = fakeBudgetRepository(budgets = listOf(budget))

        val janTransaction = Transaction("tx-1", 30.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "Lunch", 0L)
        val febTransaction = Transaction("tx-2", 50.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "Dinner", 2_678_400_000L)
        val transactionRepo = fakeTransactionRepository(transactions = listOf(janTransaction, febTransaction))

        val viewModel = BudgetDetailViewModel(
            budgetRepository = budgetRepo,
            transactionRepository = transactionRepo,
            timeProvider = FakeTimeProvider(current = 2_678_400_000L),
        )

        viewModel.onAction(BudgetDetailAction.Load("b-1"))
        advanceUntilIdle()

        val contentState = assertIs<BudgetDetailContentState.Content>(viewModel.state.value.contentState)
        assertEquals(1, contentState.detail.transactions.size)
        assertEquals("tx-2", contentState.detail.transactions.first().id)
        assertEquals(50.0, contentState.detail.budgetWithSpending.spentAmount)
    }

    @Test
    fun deleteBudgetCallsRepository() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = listOf(
            Budget("b-1", TransactionCategory.FOOD, 100.0, 0L, 0L),
        ))
        val transactionRepo = fakeTransactionRepository(transactions = emptyList())
        val viewModel = BudgetDetailViewModel(
            budgetRepository = budgetRepo,
            transactionRepository = transactionRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetDetailAction.Load("b-1"))
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(BudgetDetailAction.DeleteBudget)
            advanceUntilIdle()

            assertEquals(BudgetDetailEvent.BudgetDeleted, awaitItem())
        }
        assertEquals(1, budgetRepo.deleteCount)
    }

    @Test
    fun loadBudgetFailureExposesErrorState() = runTest(mainDispatcher) {
        val budgetRepo = mock<BudgetRepository> {
            everySuspend { loadBudgetById("b-1") } returns Result.Failure(AppError.Message("Database error"))
        }
        val transactionRepo = fakeTransactionRepository(transactions = emptyList())
        val viewModel = BudgetDetailViewModel(
            budgetRepository = budgetRepo,
            transactionRepository = transactionRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetDetailAction.Load("b-1"))
        advanceUntilIdle()

        val contentState = assertIs<BudgetDetailContentState.Error>(viewModel.state.value.contentState)
        assertEquals(AppError.Message("Database error"), contentState.error)
    }
}
