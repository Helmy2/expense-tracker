import Foundation
import SharedCore

protocol BudgetRepositoryBridge {
    func loadBudgets() async throws -> [BudgetItem]
    func loadBudgetById(id: String) async throws -> BudgetItem?
    func loadBudgetByCategory(category: ExpenseCategory) async throws -> BudgetItem?
    func createBudget(category: ExpenseCategory, monthlyLimit: Double) async throws -> BudgetItem
    func updateBudget(id: String, monthlyLimit: Double) async throws -> BudgetItem
    func deleteBudget(id: String) async throws
    func loadBudgetsWithSpending() async throws -> [BudgetWithSpendingData]
    func loadBudgetDetail(id: String) async throws -> BudgetDetailData?
}

final class SharedBudgetRepositoryBridge: BudgetRepositoryBridge {
    let repository: BudgetRepository

    init() {
        repository = iosBudgetRepository()
    }

    func loadBudgets() async throws -> [BudgetItem] {
        let kotlinBudgets = try await repository.loadBudgetsOrThrow()
        return kotlinBudgets.map { mapFromKotlin($0) }
    }

    func loadBudgetById(id: String) async throws -> BudgetItem? {
        guard let kotlinBudget = try await repository.loadBudgetByIdOrThrow(id: id) else {
            return nil
        }
        return mapFromKotlin(kotlinBudget)
    }

    func loadBudgetByCategory(category: ExpenseCategory) async throws -> BudgetItem? {
        guard let kotlinBudget = try await repository.loadBudgetByCategoryOrThrow(category: category.toKotlinCategory()) else {
            return nil
        }
        return mapFromKotlin(kotlinBudget)
    }

    func createBudget(category: ExpenseCategory, monthlyLimit: Double) async throws -> BudgetItem {
        let kotlinBudget = try await repository.createBudgetOrThrow(
            category: category.toKotlinCategory(),
            monthlyLimit: monthlyLimit
        )
        return mapFromKotlin(kotlinBudget)
    }

    func updateBudget(id: String, monthlyLimit: Double) async throws -> BudgetItem {
        let kotlinBudget = try await repository.updateBudgetOrThrow(id: id, monthlyLimit: monthlyLimit)
        return mapFromKotlin(kotlinBudget)
    }

    func deleteBudget(id: String) async throws {
        try await repository.deleteBudgetOrThrow(id: id)
    }

    func loadBudgetsWithSpending() async throws -> [BudgetWithSpendingData] {
        let kotlinList = try await repository.loadBudgetsWithSpendingOrThrow()
        return kotlinList.map { mapBudgetWithSpendingFromKotlin($0) }
    }

    func loadBudgetDetail(id: String) async throws -> BudgetDetailData? {
        guard let kotlinDetail = try await repository.loadBudgetDetailOrThrow(id: id) else {
            return nil
        }
        return BudgetDetailData(
            budgetWithSpending: mapBudgetWithSpendingFromKotlin(kotlinDetail.budgetWithSpending),
            transactions: kotlinDetail.transactions.map { mapTransactionFromKotlin($0) }
        )
    }

    private func mapFromKotlin(_ budget: SharedCore.Budget) -> BudgetItem {
        BudgetItem(
            id: budget.id,
            category: budget.category.asSwiftCategory,
            monthlyLimit: Double(budget.monthlyLimit),
            createdAtMillis: budget.createdAtMillis,
            updatedAtMillis: budget.updatedAtMillis
        )
    }

    private func mapBudgetWithSpendingFromKotlin(_ bws: SharedCore.BudgetWithSpending) -> BudgetWithSpendingData {
        BudgetWithSpendingData.compute(
            budget: mapFromKotlin(bws.budget),
            spentAmount: Double(bws.spentAmount)
        )
    }

    private func mapTransactionFromKotlin(_ transaction: SharedCore.Transaction) -> ExpenseItem {
        ExpenseItem(
            id: transaction.id,
            amount: transaction.amount,
            type: transaction.type.asSwiftType,
            category: transaction.category.asSwiftCategory,
            note: transaction.note,
            createdAtMillis: transaction.createdAtMillis
        )
    }
}
