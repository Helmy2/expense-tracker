import Foundation
@testable import iosApp

final class MockBudgetRepositoryBridge: BudgetRepositoryBridge {
    var budgetsToReturn: [BudgetItem] = []
    var budgetToReturn: BudgetItem?
    var createdBudget: BudgetItem?
    var loadBudgetsError: Error?
    var loadBudgetByIdError: Error?
    var createBudgetError: Error?
    var updateBudgetError: Error?
    var deleteBudgetError: Error?
    var createCallCount = 0
    var updateCallCount = 0
    var deleteCallCount = 0
    var lastDeletedId: String?
    var lastUpdatedId: String?
    var lastUpdatedLimit: Double?

    var budgetsWithSpendingToReturn: [BudgetWithSpendingData] = []
    var budgetDetailToReturn: BudgetDetailData? = nil

    func loadBudgets() async throws -> [BudgetItem] {
        if let error = loadBudgetsError { throw error }
        return budgetsToReturn
    }

    func loadBudgetById(id: String) async throws -> BudgetItem? {
        if let error = loadBudgetByIdError { throw error }
        return budgetToReturn
    }

    func loadBudgetByCategory(category: ExpenseCategory) async throws -> BudgetItem? {
        return budgetsToReturn.first { $0.category == category }
    }

    func createBudget(category: ExpenseCategory, monthlyLimit: Double) async throws -> BudgetItem {
        createCallCount += 1
        if let error = createBudgetError { throw error }
        let item = BudgetItem(
            id: createdBudget?.id ?? "new-budget-id",
            category: category,
            monthlyLimit: monthlyLimit,
            createdAtMillis: Int64(Date().timeIntervalSince1970 * 1000),
            updatedAtMillis: Int64(Date().timeIntervalSince1970 * 1000)
        )
        createdBudget = item
        return item
    }

    func updateBudget(id: String, monthlyLimit: Double) async throws -> BudgetItem {
        updateCallCount += 1
        lastUpdatedId = id
        lastUpdatedLimit = monthlyLimit
        if let error = updateBudgetError { throw error }
        return BudgetItem(
            id: id,
            category: .food,
            monthlyLimit: monthlyLimit,
            createdAtMillis: 0,
            updatedAtMillis: Int64(Date().timeIntervalSince1970 * 1000)
        )
    }

    func deleteBudget(id: String) async throws {
        deleteCallCount += 1
        lastDeletedId = id
        if let error = deleteBudgetError { throw error }
    }

    func loadBudgetsWithSpending() async throws -> [BudgetWithSpendingData] {
        if let error = loadBudgetsError { throw error }
        return budgetsWithSpendingToReturn
    }

    func loadBudgetDetail(id: String) async throws -> BudgetDetailData? {
        if let error = loadBudgetsError { throw error }
        return budgetDetailToReturn
    }
}

func makeBudgetItem(
    id: String = "budget-1",
    category: ExpenseCategory = .food,
    monthlyLimit: Double = 500.0,
    createdAtMillis: Int64 = 1_720_000_000_000,
    updatedAtMillis: Int64 = 1_720_000_000_000
) -> BudgetItem {
    BudgetItem(
        id: id,
        category: category,
        monthlyLimit: monthlyLimit,
        createdAtMillis: createdAtMillis,
        updatedAtMillis: updatedAtMillis
    )
}
