import Foundation

enum ExpenseCategory: String, CaseIterable, Identifiable {
    case food = "FOOD"
    case rent = "RENT"
    case entertainment = "ENTERTAINMENT"
    case transportation = "TRANSPORTATION"
    case utilities = "UTILITIES"
    case shopping = "SHOPPING"
    case healthcare = "HEALTHCARE"
    case education = "EDUCATION"
    case bills = "BILLS"
    case otherExpense = "OTHER_EXPENSE"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .food: String(localized: "expense_category_food")
        case .rent: String(localized: "expense_category_rent")
        case .entertainment: String(localized: "expense_category_entertainment")
        case .transportation: String(localized: "expense_category_transportation")
        case .utilities: String(localized: "expense_category_utilities")
        case .shopping: String(localized: "expense_category_shopping")
        case .healthcare: String(localized: "expense_category_healthcare")
        case .education: String(localized: "expense_category_education")
        case .bills: String(localized: "expense_category_bills")
        case .otherExpense: String(localized: "expense_category_other_expense")
        }
    }
}
