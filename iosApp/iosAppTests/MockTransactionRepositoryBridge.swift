import Foundation
@testable import iosApp

final class MockTransactionRepositoryBridge: TransactionRepositoryBridge {
    var transactionsToReturn: [ExpenseItem] = []
    var addedItem: ExpenseItem?
    var loadTransactionsError: Error?
    var addTransactionError: Error?
    var deleteTransactionError: Error?
    var addCallCount = 0
    var deleteCallCount = 0
    var lastDeletedId: String?

    func loadTransactions() async throws -> [ExpenseItem] {
        if let loadTransactionsError { throw loadTransactionsError }
        return transactionsToReturn
    }

    func addTransaction(amount: Double, type: ExpenseType, category: ExpenseCategory, note: String) async throws -> ExpenseItem {
        if let addTransactionError { throw addTransactionError }
        addCallCount += 1
        let item = ExpenseItem(
            id: addedItem?.id ?? "added-id",
            amount: amount,
            type: type,
            category: category,
            note: note,
            createdAtMillis: addedItem?.createdAtMillis ?? 100
        )
        addedItem = item
        return item
    }

    func deleteTransaction(id: String) async throws {
        if let deleteTransactionError { throw deleteTransactionError }
        deleteCallCount += 1
        lastDeletedId = id
    }
}

func makeExpenseItem(
    id: String = "expense-1",
    amount: Double = 45.0,
    type: ExpenseType = .expense,
    category: ExpenseCategory = .food,
    note: String = "Lunch",
    createdAtMillis: Int64 = 1_720_000_000_000
) -> ExpenseItem {
    ExpenseItem(
        id: id,
        amount: amount,
        type: type,
        category: category,
        note: note,
        createdAtMillis: createdAtMillis
    )
}

struct MockBridgeError: LocalizedError {
    let errorDescription: String?
}
