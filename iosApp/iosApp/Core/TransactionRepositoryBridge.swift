import Foundation
import SharedCore

protocol TransactionRepositoryBridge {
    func loadTransactions() async throws -> [ExpenseItem]
    func addTransaction(amount: Double, type: ExpenseType, category: ExpenseCategory, note: String) async throws -> ExpenseItem
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

    func addTransaction(amount: Double, type: ExpenseType, category: ExpenseCategory, note: String) async throws -> ExpenseItem {
        let kotlinTransaction = try await repository.addTransactionOrThrow(
            amount: amount,
            type: type.toKotlinType(),
            category: category.toKotlinCategory(),
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
            category: transaction.category.asSwiftCategory,
            note: transaction.note,
            createdAtMillis: transaction.createdAtMillis
        )
    }
}

extension ExpenseType {
    func toKotlinType() -> SharedCore.TransactionType {
        switch self {
        case .income: return SharedCore.TransactionType.income
        case .expense: return SharedCore.TransactionType.expense
        }
    }
}

extension ExpenseCategory {
    func toKotlinCategory() -> SharedCore.TransactionCategory {
        switch self {
        case .food: return SharedCore.TransactionCategory.food
        case .rent: return SharedCore.TransactionCategory.rent
        case .salary: return SharedCore.TransactionCategory.salary
        case .entertainment: return SharedCore.TransactionCategory.entertainment
        case .transportation: return SharedCore.TransactionCategory.transportation
        case .utilities: return SharedCore.TransactionCategory.utilities
        case .shopping: return SharedCore.TransactionCategory.shopping
        case .healthcare: return SharedCore.TransactionCategory.healthcare
        case .education: return SharedCore.TransactionCategory.education
        case .other: return SharedCore.TransactionCategory.other
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

extension SharedCore.TransactionCategory {
    var asSwiftCategory: ExpenseCategory {
        switch self {
        case .food: return .food
        case .rent: return .rent
        case .salary: return .salary
        case .entertainment: return .entertainment
        case .transportation: return .transportation
        case .utilities: return .utilities
        case .shopping: return .shopping
        case .healthcare: return .healthcare
        case .education: return .education
        case .other: return .other
        @unknown default: return .other
        }
    }
}
