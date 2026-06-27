import XCTest
@testable import iosApp

final class BudgetListViewModelTests: XCTestCase {
    func testLoadSetsContentStateWhenBudgetsExist() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.budgetsToReturn = [
            makeBudgetItem(id: "1", category: .food, monthlyLimit: 500),
            makeBudgetItem(id: "2", category: .rent, monthlyLimit: 1500)
        ]
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        await viewModel.load(force: true)

        if case .content(let items) = viewModel.contentState {
            XCTAssertEqual(items.count, 2)
        } else {
            XCTFail("Expected content state")
        }
    }

    func testLoadSetsEmptyStateWhenNoBudgets() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.budgetsToReturn = []
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        await viewModel.load(force: true)

        if case .empty = viewModel.contentState {
            // expected
        } else {
            XCTFail("Expected empty state")
        }
    }

    func testLoadSetsErrorStateWhenBridgeThrows() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.loadBudgetsError = MockBridgeError(errorDescription: "Database unavailable")
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        await viewModel.load(force: true)

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Database unavailable")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testSaveBudgetCreatesAndReloads() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.createdBudget = makeBudgetItem(id: "new-1", category: .food, monthlyLimit: 300)
        budgetBridge.budgetsToReturn = [makeBudgetItem(id: "new-1", category: .food, monthlyLimit: 300)]
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        viewModel.formState.limitText = "300"
        viewModel.formState.selectedCategory = .food

        await viewModel.saveBudget()

        XCTAssertEqual(budgetBridge.createCallCount, 1)
        XCTAssertFalse(viewModel.showFormSheet)
        XCTAssertEqual(viewModel.formState.limitText, "")
    }

    func testSaveBudgetUpdatesExistingBudget() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.budgetsToReturn = [makeBudgetItem(id: "existing-1", category: .food, monthlyLimit: 400)]
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        viewModel.editingBudgetId = "existing-1"
        viewModel.formState.limitText = "600"
        viewModel.formState.selectedCategory = .food

        await viewModel.saveBudget()

        XCTAssertEqual(budgetBridge.updateCallCount, 1)
        XCTAssertEqual(budgetBridge.lastUpdatedId, "existing-1")
        XCTAssertEqual(budgetBridge.lastUpdatedLimit, 600)
    }

    func testSaveBudgetShowsErrorOnFailure() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.createBudgetError = MockBridgeError(errorDescription: "Save failed")
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        viewModel.formState.limitText = "300"
        viewModel.formState.selectedCategory = .food

        await viewModel.saveBudget()

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Save failed")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testDeleteConfirmationRemovesBudget() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        budgetBridge.budgetsToReturn = [makeBudgetItem(id: "to-delete")]
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        await viewModel.load(force: true)

        viewModel.pendingDeleteId = "to-delete"
        viewModel.showingDeleteConfirmation = true
        await viewModel.confirmDelete()

        XCTAssertEqual(budgetBridge.deleteCallCount, 1)
        XCTAssertEqual(budgetBridge.lastDeletedId, "to-delete")
        XCTAssertFalse(viewModel.showingDeleteConfirmation)
    }

    func testStartCreateResetsFormState() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        viewModel.editingBudgetId = "some-id"
        viewModel.formState.limitText = "500"

        viewModel.startCreate()

        XCTAssertNil(viewModel.editingBudgetId)
        XCTAssertTrue(viewModel.showFormSheet)
        XCTAssertEqual(viewModel.formState.limitText, "")
    }

    func testStartEditPopulatesFormState() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()
        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)

        let budgetData = BudgetWithSpendingData.compute(
            budget: makeBudgetItem(id: "edit-1", category: .entertainment, monthlyLimit: 200),
            spentAmount: 50
        )

        viewModel.startEdit(budget: budgetData)

        XCTAssertEqual(viewModel.editingBudgetId, "edit-1")
        XCTAssertTrue(viewModel.showFormSheet)
        XCTAssertEqual(viewModel.formState.limitText, "200.0")
    }

    func testComputeSpendingCalculatesCurrentMonthExpenses() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let transactionBridge = MockTransactionRepositoryBridge()

        let now = Date()
        let calendar = Calendar.current
        let currentMonth = calendar.component(.month, from: now)
        let currentYear = calendar.component(.year, from: now)
        let timestamp = calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 15))!

        budgetBridge.budgetsToReturn = [
            makeBudgetItem(id: "1", category: .food, monthlyLimit: 500)
        ]
        transactionBridge.transactionsToReturn = [
            makeExpenseItem(id: "t1", amount: 25, type: .expense, category: .food, createdAtMillis: Int64(timestamp.timeIntervalSince1970 * 1000)),
            makeExpenseItem(id: "t2", amount: 35, type: .expense, category: .food, createdAtMillis: Int64(timestamp.timeIntervalSince1970 * 1000)),
            makeExpenseItem(id: "t3", amount: 100, type: .income, category: .salary, createdAtMillis: Int64(timestamp.timeIntervalSince1970 * 1000))
        ]

        let viewModel = BudgetListViewModel(bridge: budgetBridge, transactionBridge: transactionBridge)
        await viewModel.load(force: true)

        if case .content(let items) = viewModel.contentState {
            XCTAssertEqual(items.count, 1)
            XCTAssertEqual(items[0].spentAmount, 60, accuracy: 0.01)
        } else {
            XCTFail("Expected content state")
        }
    }
}
