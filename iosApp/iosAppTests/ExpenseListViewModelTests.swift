import XCTest
@testable import iosApp

final class ExpenseListViewModelTests: XCTestCase {
    func testLoadSetsContentStateWhenBridgeReturnsItems() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.transactionsToReturn = [
            makeExpenseItem(id: "1", amount: 45, type: .expense, category: .food),
            makeExpenseItem(id: "2", amount: 120, type: .income, category: .salary)
        ]
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        await viewModel.load(force: true)

        if case .content(let items) = viewModel.contentState {
            XCTAssertEqual(items.count, 2)
        } else {
            XCTFail("Expected content state")
        }
    }

    func testLoadSetsEmptyStateWhenNoTransactions() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.transactionsToReturn = []
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        await viewModel.load(force: true)

        if case .empty = viewModel.contentState {
            // expected
        } else {
            XCTFail("Expected empty state")
        }
    }

    func testLoadSetsErrorStateWhenBridgeThrows() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.loadTransactionsError = MockBridgeError(errorDescription: "Database unavailable")
        let viewModel = ExpenseListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Database unavailable")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testToggleFormSheet() async {
        let bridge = MockTransactionRepositoryBridge()
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        XCTAssertFalse(viewModel.showFormSheet)

        viewModel.showFormSheet = true
        XCTAssertTrue(viewModel.showFormSheet)
    }

    func testSaveTransactionClosesFormSheet() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.addedItem = makeExpenseItem(id: "new-1", amount: 25, type: .expense, category: .food)
        bridge.transactionsToReturn = [makeExpenseItem(id: "new-1", amount: 25, type: .expense, category: .food)]
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        viewModel.showFormSheet = true
        viewModel.formState.amountText = "25.00"
        viewModel.formState.selectedType = .expense
        viewModel.formState.selectedCategory = .food
        viewModel.formState.noteText = "Lunch"

        await viewModel.saveTransaction()

        XCTAssertFalse(viewModel.showFormSheet)
    }

    func testSaveTransactionAddsAndReloads() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.addedItem = makeExpenseItem(id: "new-1", amount: 25, type: .expense, category: .food)
        bridge.transactionsToReturn = [makeExpenseItem(id: "new-1", amount: 25, type: .expense, category: .food)]
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        viewModel.formState.amountText = "25.00"
        viewModel.formState.selectedType = .expense
        viewModel.formState.selectedCategory = .food
        viewModel.formState.noteText = "Lunch"

        await viewModel.saveTransaction()

        XCTAssertEqual(bridge.addCallCount, 1)
        XCTAssertEqual(viewModel.formState.amountText, "")
    }

    func testSaveTransactionShowsErrorOnFailure() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.addTransactionError = MockBridgeError(errorDescription: "Save failed")
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        viewModel.formState.amountText = "25.00"
        viewModel.formState.selectedType = .expense
        viewModel.formState.selectedCategory = .food

        await viewModel.saveTransaction()

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Save failed")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testDeleteConfirmationRemovesTransaction() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.transactionsToReturn = [makeExpenseItem(id: "to-delete")]
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        await viewModel.load(force: true)

        viewModel.pendingDeleteId = "to-delete"
        viewModel.showingDeleteConfirmation = true
        await viewModel.confirmDelete()

        XCTAssertEqual(bridge.deleteCallCount, 1)
        XCTAssertEqual(bridge.lastDeletedId, "to-delete")
        XCTAssertFalse(viewModel.showingDeleteConfirmation)
    }

    func testDashboardComputesCorrectTotals() async {
        let bridge = MockTransactionRepositoryBridge()
        bridge.transactionsToReturn = [
            makeExpenseItem(id: "1", amount: 100, type: .income, category: .salary),
            makeExpenseItem(id: "2", amount: 45, type: .expense, category: .food),
            makeExpenseItem(id: "3", amount: 30, type: .expense, category: .transportation)
        ]
        let viewModel = ExpenseListViewModel(bridge: bridge, nowMillis: { 500 })

        await viewModel.load(force: true)

        XCTAssertEqual(viewModel.dashboard.totalIncome, 100)
        XCTAssertEqual(viewModel.dashboard.totalExpenses, 75)
        XCTAssertEqual(viewModel.dashboard.totalBalance, 25)
    }
}
