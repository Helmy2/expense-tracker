package com.expense.tracker.feature.expense.impl

import app.cash.turbine.test
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {
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
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        assertIs<ExpenseContentState.Loading>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithEmptyListTransitionsToEmpty() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        assertIs<ExpenseContentState.Empty>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithTransactionsTransitionsToContent() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<ExpenseContentState.Content>(viewModel.state.value.contentState)
        assertEquals(3, contentState.transactions.size)
    }

    @Test
    fun loadComputesDashboard() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        val dashboard = viewModel.state.value.dashboard
        assertEquals(100.0, dashboard.totalIncome)
        assertEquals(60.0, dashboard.totalExpenses)
        assertEquals(40.0, dashboard.totalBalance)
    }

    @Test
    fun saveTransactionWithValidInputAddsAndReloads() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(current = 5000L),
        )
        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        viewModel.onAction(ExpenseAction.AmountChanged("25.50"))
        viewModel.onAction(ExpenseAction.TypeSelected(TransactionType.EXPENSE))
        viewModel.onAction(ExpenseAction.CategorySelected(TransactionCategory.FOOD))
        viewModel.onAction(ExpenseAction.NoteChanged("Lunch"))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        assertEquals(1, repository.addCount)
        assertEquals("", viewModel.state.value.amountText)
        assertEquals("", viewModel.state.value.noteText)
    }

    @Test
    fun saveTransactionEmitsSavedEvent() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(current = 5000L),
        )
        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(ExpenseAction.AmountChanged("25.50"))
            viewModel.onAction(ExpenseAction.TypeSelected(TransactionType.EXPENSE))
            viewModel.onAction(ExpenseAction.CategorySelected(TransactionCategory.FOOD))
            viewModel.onAction(ExpenseAction.SaveTransaction)
            advanceUntilIdle()

            assertEquals(ExpenseEvent.TransactionSaved, awaitItem())
        }
    }

    @Test
    fun saveTransactionWithInvalidAmountDoesNotCallRepository() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.AmountChanged("0"))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        assertEquals(0, repository.addCount)
    }

    @Test
    fun saveTransactionWithNegativeAmountDoesNotCallRepository() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.AmountChanged("-5"))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        assertEquals(0, repository.addCount)
    }

    @Test
    fun saveTransactionWithNonNumericAmountDoesNotCallRepository() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.AmountChanged("abc"))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        assertEquals(0, repository.addCount)
    }

    @Test
    fun deleteTransactionRemovesAndReloads() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )
        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        viewModel.onAction(ExpenseAction.DeleteTransaction("tx-1"))
        viewModel.onAction(ExpenseAction.ConfirmDelete)
        advanceUntilIdle()

        assertEquals(1, repository.deleteCount)
    }

    @Test
    fun cancelDeleteDoesNotCallRepository() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )
        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        viewModel.onAction(ExpenseAction.DeleteTransaction("tx-1"))
        viewModel.onAction(ExpenseAction.CancelDelete)
        advanceUntilIdle()

        assertEquals(0, repository.deleteCount)
    }

    @Test
    fun typeSelectionUpdatesState() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.TypeSelected(TransactionType.INCOME))
        advanceUntilIdle()
        assertEquals(TransactionType.INCOME, viewModel.state.value.selectedType)

        viewModel.onAction(ExpenseAction.TypeSelected(TransactionType.EXPENSE))
        advanceUntilIdle()
        assertEquals(TransactionType.EXPENSE, viewModel.state.value.selectedType)
    }

    @Test
    fun categorySelectionUpdatesStateAndClosesMenu() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.ToggleCategoryMenu)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.categoryMenuExpanded)

        viewModel.onAction(ExpenseAction.CategorySelected(TransactionCategory.FOOD))
        advanceUntilIdle()
        assertEquals(TransactionCategory.FOOD, viewModel.state.value.selectedCategory)
        assertEquals(false, viewModel.state.value.categoryMenuExpanded)
    }

    @Test
    fun amountChangedUpdatesState() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.AmountChanged("42.50"))
        advanceUntilIdle()
        assertEquals("42.50", viewModel.state.value.amountText)
    }

    @Test
    fun noteChangedUpdatesState() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.NoteChanged("Groceries"))
        advanceUntilIdle()
        assertEquals("Groceries", viewModel.state.value.noteText)
    }

    @Test
    fun dashboardRecomputesAfterAdd() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = sampleTransactions())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(current = 5000L),
        )
        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        val initialBalance = viewModel.state.value.dashboard.totalBalance

        viewModel.onAction(ExpenseAction.AmountChanged("50"))
        viewModel.onAction(ExpenseAction.TypeSelected(TransactionType.INCOME))
        viewModel.onAction(ExpenseAction.CategorySelected(TransactionCategory.SALARY))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        val newBalance = viewModel.state.value.dashboard.totalBalance
        assertEquals(initialBalance + 50.0, newBalance)
    }

    @Test
    fun toggleFormSheetSetsShowBottomSheetToTrue() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        assertEquals(false, viewModel.state.value.showBottomSheet)

        viewModel.onAction(ExpenseAction.ToggleFormSheet)
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.showBottomSheet)
    }

    @Test
    fun dismissFormSheetSetsShowBottomSheetToFalse() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.ToggleFormSheet)
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.showBottomSheet)

        viewModel.onAction(ExpenseAction.DismissFormSheet)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.showBottomSheet)
    }

    @Test
    fun toggleFormSheetTwiceToggles() = runTest(mainDispatcher) {
        val viewModel = ExpenseViewModel(
            repository = fakeRepository(transactions = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        assertEquals(false, viewModel.state.value.showBottomSheet)
        viewModel.onAction(ExpenseAction.ToggleFormSheet)
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.showBottomSheet)
        viewModel.onAction(ExpenseAction.ToggleFormSheet)
        advanceUntilIdle()
        assertEquals(false, viewModel.state.value.showBottomSheet)
    }

    @Test
    fun saveTransactionClosesBottomSheet() = runTest(mainDispatcher) {
        val repository = fakeRepository(transactions = emptyList())
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(current = 5000L),
        )

        viewModel.onAction(ExpenseAction.ToggleFormSheet)
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.showBottomSheet)

        viewModel.onAction(ExpenseAction.AmountChanged("25.50"))
        viewModel.onAction(ExpenseAction.SaveTransaction)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.showBottomSheet)
    }

    @Test
    fun loadErrorExposesErrorState() = runTest(mainDispatcher) {
        val repository = mock<TransactionRepository> {
            everySuspend { loadTransactions() } returns Result.Failure(AppError.Message("Database error"))
        }
        val viewModel = ExpenseViewModel(
            repository = repository,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(ExpenseAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<ExpenseContentState.Error>(viewModel.state.value.contentState)
        assertEquals(AppError.Message("Database error"), contentState.error)
    }
}

private fun sampleTransactions(): List<Transaction> = listOf(
    Transaction("tx-1", 45.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "Lunch", 0L),
    Transaction("tx-2", 100.0, TransactionType.INCOME, TransactionCategory.SALARY, "Paycheck", 1000L),
    Transaction("tx-3", 15.0, TransactionType.EXPENSE, TransactionCategory.TRANSPORTATION, "Bus pass", 2000L),
)

private fun fakeRepository(transactions: List<Transaction>): FakeTransactionRepository =
    FakeTransactionRepository(transactions)

private class FakeTransactionRepository(
    initialTransactions: List<Transaction>,
) : TransactionRepository {
    private val transactions = initialTransactions.toMutableList()
    var addCount = 0
    var deleteCount = 0

    override suspend fun loadTransactions(): Result<List<Transaction>> =
        Result.Success(transactions.toList())

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
    ): Result<Transaction> {
        addCount++
        val transaction = Transaction(
            id = "new-$addCount",
            amount = amount,
            type = type,
            category = category,
            note = note,
            createdAtMillis = 5000L,
        )
        transactions.add(transaction)
        return Result.Success(transaction)
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        deleteCount++
        transactions.removeAll { it.id == id }
        return Result.Success(Unit)
    }
}
