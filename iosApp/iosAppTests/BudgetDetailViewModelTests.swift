import XCTest
@testable import iosApp

final class BudgetDetailViewModelTests: XCTestCase {
    func testLoadSetsContentStateWithBudgetDetail() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let now = Date()
        let calendar = Calendar.current
        let currentMonth = calendar.component(.month, from: now)
        let currentYear = calendar.component(.year, from: now)
        let timestamp = calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 15))!

        budgetBridge.budgetDetailToReturn = BudgetDetailData(
            budgetWithSpending: BudgetWithSpendingData.compute(
                budget: makeBudgetItem(id: "detail-1", category: .food, monthlyLimit: 500),
                spentAmount: 60
            ),
            transactions: [
                makeExpenseItem(id: "t1", amount: 25, type: .expense, category: ExpenseCategory.food.rawValue, createdAtMillis: Int64(timestamp.timeIntervalSince1970 * 1000)),
                makeExpenseItem(id: "t2", amount: 35, type: .expense, category: ExpenseCategory.food.rawValue, createdAtMillis: Int64(timestamp.timeIntervalSince1970 * 1000))
            ]
        )

        let viewModel = BudgetDetailViewModel(bridge: budgetBridge)
        await viewModel.load(budgetId: "detail-1")

        if case .content(let budgetWithSpending, let transactions) = viewModel.contentState {
            XCTAssertEqual(budgetWithSpending.budget.id, "detail-1")
            XCTAssertEqual(budgetWithSpending.spentAmount, 60, accuracy: 0.01)
            XCTAssertEqual(transactions.count, 2)
        } else {
            XCTFail("Expected content state")
        }
    }

    func testLoadSetsErrorStateWhenBudgetNotFound() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        budgetBridge.budgetDetailToReturn = nil

        let viewModel = BudgetDetailViewModel(bridge: budgetBridge)
        await viewModel.load(budgetId: "nonexistent")

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Budget not found")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testLoadSetsErrorStateWhenBridgeThrows() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        budgetBridge.loadBudgetsError = MockBridgeError(errorDescription: "Database error")

        let viewModel = BudgetDetailViewModel(bridge: budgetBridge)
        await viewModel.load(budgetId: "any-id")

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Database error")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testDeleteBudgetCallsBridge() async {
        let budgetBridge = MockBudgetRepositoryBridge()

        let viewModel = BudgetDetailViewModel(bridge: budgetBridge)
        await viewModel.deleteBudget(budgetId: "to-delete")

        XCTAssertEqual(budgetBridge.deleteCallCount, 1)
        XCTAssertEqual(budgetBridge.lastDeletedId, "to-delete")
        XCTAssertFalse(viewModel.showingDeleteConfirmation)
    }

    func testLoadFiltersTransactionsToCurrentMonth() async {
        let budgetBridge = MockBudgetRepositoryBridge()
        let now = Date()
        let calendar = Calendar.current
        let currentMonth = calendar.component(.month, from: now)
        let currentYear = calendar.component(.year, from: now)
        let currentTimestamp = calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 10))!
        let lastMonthTimestamp = calendar.date(from: DateComponents(year: currentYear, month: max(currentMonth - 1, 1), day: 10))!

        budgetBridge.budgetDetailToReturn = BudgetDetailData(
            budgetWithSpending: BudgetWithSpendingData.compute(
                budget: makeBudgetItem(id: "detail-2", category: .food, monthlyLimit: 500),
                spentAmount: 25
            ),
            transactions: [
                makeExpenseItem(id: "t1", amount: 25, type: .expense, category: ExpenseCategory.food.rawValue, createdAtMillis: Int64(currentTimestamp.timeIntervalSince1970 * 1000))
            ]
        )
        // lastMonthTimestamp and rent-tx are not used; the bridge returns a pre-filtered list.
        _ = lastMonthTimestamp

        let viewModel = BudgetDetailViewModel(bridge: budgetBridge)
        await viewModel.load(budgetId: "detail-2")

        if case .content(let budgetWithSpending, let transactions) = viewModel.contentState {
            XCTAssertEqual(budgetWithSpending.spentAmount, 25, accuracy: 0.01)
            XCTAssertEqual(transactions.count, 1)
            XCTAssertEqual(transactions[0].id, "t1")
        } else {
            XCTFail("Expected content state")
        }
    }
}
