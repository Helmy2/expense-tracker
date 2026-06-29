package com.expense.tracker.feature.budget.impl

import app.cash.turbine.test
import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetStatus
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.budget.domain.model.withSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
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
class BudgetViewModelTest {
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
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        assertIs<BudgetContentState.Loading>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithEmptyListTransitionsToEmpty() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(
            budgets = emptyList(),
            budgetsWithSpendingToReturn = emptyList(),
        )
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        assertIs<BudgetContentState.Empty>(viewModel.state.value.contentState)
    }

    @Test
    fun loadWithBudgetsTransitionsToContent() = runTest(mainDispatcher) {
        val budgetsWithSpending = sampleBudgetsWithSpending()
        val budgetRepo = fakeBudgetRepository(
            budgets = sampleBudgets(),
            budgetsWithSpendingToReturn = budgetsWithSpending,
        )
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<BudgetContentState.Content>(viewModel.state.value.contentState)
        assertEquals(2, contentState.budgets.size)
    }

    @Test
    fun loadWithBudgetsCalculatesSpending() = runTest(mainDispatcher) {
        val budgets = sampleBudgets()
        val foodBudget = budgets[0].withSpending(45.0)
        val transportBudget = budgets[1].withSpending(0.0)
        val budgetRepo = fakeBudgetRepository(
            budgets = budgets,
            budgetsWithSpendingToReturn = listOf(foodBudget, transportBudget),
        )
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<BudgetContentState.Content>(viewModel.state.value.contentState)
        val foodEntry = contentState.budgets.find { it.budget.category == TransactionCategory.FOOD }
        assertTrue(foodEntry != null)
        assertEquals(45.0, foodEntry.spentAmount)
    }

    @Test
    fun loadComputesCorrectStatus() = runTest(mainDispatcher) {
        val budget = Budget(
            id = "b-1",
            category = TransactionCategory.FOOD,
            monthlyLimit = 50.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        val bws = budget.withSpending(40.0)
        val budgetRepo = fakeBudgetRepository(
            budgets = listOf(budget),
            budgetsWithSpendingToReturn = listOf(bws),
        )
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<BudgetContentState.Content>(viewModel.state.value.contentState)
        val foodBudget = contentState.budgets.first()
        assertEquals(BudgetStatus.BETWEEN_75_90, foodBudget.status)
    }

    @Test
    fun saveBudgetWithValidInputCreatesAndReloads() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(current = 5000L),
        )
        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        viewModel.onAction(BudgetAction.StartCreate(emptyList()))
        viewModel.onAction(BudgetAction.LimitChanged("100"))
        viewModel.onAction(BudgetAction.CategorySelected(TransactionCategory.FOOD))
        viewModel.onAction(BudgetAction.SaveBudget)
        advanceUntilIdle()

        assertEquals(1, budgetRepo.createCount)
        assertEquals("", viewModel.state.value.limitText)
        assertEquals(false, viewModel.state.value.showFormSheet)
    }

    @Test
    fun saveBudgetEmitsSavedEvent() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(current = 5000L),
        )
        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(BudgetAction.StartCreate(emptyList()))
            viewModel.onAction(BudgetAction.LimitChanged("100"))
            viewModel.onAction(BudgetAction.CategorySelected(TransactionCategory.FOOD))
            viewModel.onAction(BudgetAction.SaveBudget)
            advanceUntilIdle()

            assertEquals(BudgetEvent.BudgetSaved, awaitItem())
        }
    }

    @Test
    fun saveBudgetWithInvalidLimitDoesNotCallRepository() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartCreate(emptyList()))
        viewModel.onAction(BudgetAction.LimitChanged("0"))
        viewModel.onAction(BudgetAction.SaveBudget)
        advanceUntilIdle()

        assertEquals(0, budgetRepo.createCount)
    }

    @Test
    fun saveBudgetWithNegativeLimitDoesNotCallRepository() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartCreate(emptyList()))
        viewModel.onAction(BudgetAction.LimitChanged("-5"))
        viewModel.onAction(BudgetAction.SaveBudget)
        advanceUntilIdle()

        assertEquals(0, budgetRepo.createCount)
    }

    @Test
    fun saveBudgetWithNonNumericLimitDoesNotCallRepository() = runTest(mainDispatcher) {
        val budgetRepo = fakeBudgetRepository(budgets = emptyList())
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartCreate(emptyList()))
        viewModel.onAction(BudgetAction.LimitChanged("abc"))
        viewModel.onAction(BudgetAction.SaveBudget)
        advanceUntilIdle()

        assertEquals(0, budgetRepo.createCount)
    }

    @Test
    fun deleteBudgetRemovesAndReloads() = runTest(mainDispatcher) {
        val budgets = sampleBudgets()
        val budgetRepo = fakeBudgetRepository(budgets = budgets)
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )
        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        viewModel.onAction(BudgetAction.DeleteBudget("b-1"))
        viewModel.onAction(BudgetAction.ConfirmDelete)
        advanceUntilIdle()

        assertEquals(1, budgetRepo.deleteCount)
    }

    @Test
    fun deleteBudgetEmitsDeletedEvent() = runTest(mainDispatcher) {
        val budgets = sampleBudgets()
        val budgetRepo = fakeBudgetRepository(budgets = budgets)
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )
        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onAction(BudgetAction.DeleteBudget("b-1"))
            viewModel.onAction(BudgetAction.ConfirmDelete)
            advanceUntilIdle()

            assertEquals(BudgetEvent.BudgetDeleted, awaitItem())
        }
    }

    @Test
    fun cancelDeleteDoesNotCallRepository() = runTest(mainDispatcher) {
        val budgets = sampleBudgets()
        val budgetRepo = fakeBudgetRepository(budgets = budgets)
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )
        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        viewModel.onAction(BudgetAction.DeleteBudget("b-1"))
        viewModel.onAction(BudgetAction.CancelDelete)
        advanceUntilIdle()

        assertEquals(0, budgetRepo.deleteCount)
    }

    @Test
    fun categorySelectionUpdatesStateAndClosesMenu() = runTest(mainDispatcher) {
        val viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.ToggleCategoryMenu)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.categoryMenuExpanded)

        viewModel.onAction(BudgetAction.CategorySelected(TransactionCategory.FOOD))
        advanceUntilIdle()
        assertEquals(TransactionCategory.FOOD, viewModel.state.value.selectedCategory)
        assertEquals(false, viewModel.state.value.categoryMenuExpanded)
    }

    @Test
    fun limitChangedUpdatesState() = runTest(mainDispatcher) {
        val viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.LimitChanged("42.50"))
        advanceUntilIdle()
        assertEquals("42.50", viewModel.state.value.limitText)
    }

    @Test
    fun startCreateSetsFormState() = runTest(mainDispatcher) {
        val viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartCreate(listOf(TransactionCategory.FOOD)))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.showFormSheet)
        assertEquals(BudgetFormMode.Create, viewModel.state.value.formMode)
        assertEquals(TransactionCategory.OTHER, viewModel.state.value.selectedCategory)
    }

    @Test
    fun startEditSetsFormState() = runTest(mainDispatcher) {
        val viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartEdit("b-1", 100.0, TransactionCategory.FOOD))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.showFormSheet)
        assertEquals(BudgetFormMode.Edit, viewModel.state.value.formMode)
        assertEquals("b-1", viewModel.state.value.editingBudgetId)
        assertEquals(100.0, viewModel.state.value.limitText.toDouble())
        assertEquals(TransactionCategory.FOOD, viewModel.state.value.selectedCategory)
    }

    @Test
    fun dismissFormSheetResetsFormState() = runTest(mainDispatcher) {
        val viewModel = BudgetViewModel(
            budgetRepository = fakeBudgetRepository(budgets = emptyList()),
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.StartCreate(emptyList()))
        viewModel.onAction(BudgetAction.LimitChanged("100"))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.showFormSheet)

        viewModel.onAction(BudgetAction.DismissFormSheet)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.showFormSheet)
        assertEquals("", viewModel.state.value.limitText)
    }

    @Test
    fun loadErrorExposesErrorState() = runTest(mainDispatcher) {
        val budgetRepo = mock<BudgetRepository> {
            everySuspend { loadBudgetsWithSpending() } returns Result.Failure(AppError.Message("Database error"))
        }
        val viewModel = BudgetViewModel(
            budgetRepository = budgetRepo,
            timeProvider = FakeTimeProvider(),
        )

        viewModel.onAction(BudgetAction.Load)
        advanceUntilIdle()

        val contentState = assertIs<BudgetContentState.Error>(viewModel.state.value.contentState)
        assertEquals(AppError.Message("Database error"), contentState.error)
    }
}

private fun sampleBudgets(): List<Budget> = listOf(
    Budget("b-1", TransactionCategory.FOOD, 100.0, 0L, 0L),
    Budget("b-2", TransactionCategory.TRANSPORTATION, 50.0, 0L, 0L),
)

private fun sampleBudgetsWithSpending(): List<BudgetWithSpending> = sampleBudgets().map {
    it.withSpending(0.0)
}

internal fun fakeBudgetRepository(
    budgets: List<Budget>,
    budgetsWithSpendingToReturn: List<BudgetWithSpending>? = null,
    budgetDetailToReturn: BudgetDetail? = null,
): FakeBudgetRepository = FakeBudgetRepository(
    initialBudgets = budgets,
    budgetsWithSpendingToReturn = budgetsWithSpendingToReturn,
    budgetDetailToReturn = budgetDetailToReturn,
)

internal class FakeBudgetRepository(
    initialBudgets: List<Budget>,
    private val budgetsWithSpendingToReturn: List<BudgetWithSpending>? = null,
    private val budgetDetailToReturn: BudgetDetail? = null,
) : BudgetRepository {
    private val budgets = initialBudgets.toMutableList()
    var createCount = 0
    var deleteCount = 0

    override suspend fun loadBudgets(): Result<List<Budget>> =
        Result.Success(budgets.toList())

    override suspend fun loadBudgetById(id: String): Result<Budget?> =
        Result.Success(budgets.find { it.id == id })

    override suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?> =
        Result.Success(budgets.find { it.category == category })

    override suspend fun createBudget(
        category: TransactionCategory,
        monthlyLimit: Double,
    ): Result<Budget> {
        createCount++
        val budget = Budget(
            id = "new-$createCount",
            category = category,
            monthlyLimit = monthlyLimit,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        budgets.add(budget)
        return Result.Success(budget)
    }

    override suspend fun updateBudget(
        id: String,
        monthlyLimit: Double,
    ): Result<Budget> {
        val index = budgets.indexOfFirst { it.id == id }
        if (index >= 0) {
            val updated = budgets[index].copy(monthlyLimit = monthlyLimit)
            budgets[index] = updated
            return Result.Success(updated)
        }
        return Result.Failure(AppError.Unknown)
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        deleteCount++
        budgets.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>> {
        val payload = budgetsWithSpendingToReturn
            ?: budgets.map { it.withSpending(0.0) }
        return Result.Success(payload)
    }

    override suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?> {
        val explicit = budgetDetailToReturn
        if (explicit != null) return Result.Success(explicit)
        val budget = budgets.find { it.id == id }
            ?: return Result.Success(null)
        return Result.Success(
            BudgetDetail(
                budgetWithSpending = budget.withSpending(0.0),
                transactions = emptyList(),
            ),
        )
    }
}
