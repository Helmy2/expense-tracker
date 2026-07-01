import Foundation
import SharedCore

protocol TransactionRepositoryBridge {
    func loadTransactions() async throws -> [ExpenseItem]
    func addTransaction(amount: Double, type: ExpenseType, category: String, note: String) async throws -> ExpenseItem
    func deleteTransaction(id: String) async throws
}

final class SharedTransactionRepositoryBridge: TransactionRepositoryBridge {
    let repository: TransactionRepository

    init() {
        repository = iosTransactionRepository()
    }

    func loadTransactions() async throws -> [ExpenseItem] {
        let kotlinTransactions = try await repository.loadTransactionsOrThrow()
        return kotlinTransactions.map { mapFromKotlin($0) }
    }

    func addTransaction(amount: Double, type: ExpenseType, category: String, note: String) async throws -> ExpenseItem {
        let kotlinTransaction = try await repository.addTransactionOrThrow(
            amount: amount,
            type: type.toKotlinType(),
            category: category,
            note: note
        )
        return mapFromKotlin(kotlinTransaction)
    }

    func deleteTransaction(id: String) async throws {
        try await repository.deleteTransactionOrThrow(id: id)
    }

    private func mapFromKotlin(_ transaction: SharedCore.Transaction) -> ExpenseItem {
        ExpenseItem(
            id: transaction.id,
            amount: transaction.amount,
            type: transaction.type.asSwiftType,
            category: transaction.category,
            note: transaction.note,
            createdAtMillis: transaction.createdAtMillis
        )
    }
}

// MARK: - Kotlin Type Mappings

extension ExpenseType {
    func toKotlinType() -> SharedCore.TransactionType {
        switch self {
        case .income: return SharedCore.TransactionType.income
        case .expense: return SharedCore.TransactionType.expense
        }
    }
}

extension SharedCore.TransactionType {
    var asSwiftType: ExpenseType {
        switch self {
        case .income: return .income
        case .expense: return .expense
        @unknown default: return .expense
        }
    }
}
