import Foundation

enum ExpenseType: String, CaseIterable, Identifiable {
    case income = "INCOME"
    case expense = "EXPENSE"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .income: String(localized: "expense_ios_type_income")
        case .expense: String(localized: "expense_ios_type_expense")
        }
    }
}
