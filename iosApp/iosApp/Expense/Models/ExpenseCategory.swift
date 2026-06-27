import Foundation

enum ExpenseCategory: String, CaseIterable, Identifiable {
    case food = "FOOD"
    case rent = "RENT"
    case salary = "SALARY"
    case entertainment = "ENTERTAINMENT"
    case transportation = "TRANSPORTATION"
    case utilities = "UTILITIES"
    case shopping = "SHOPPING"
    case healthcare = "HEALTHCARE"
    case education = "EDUCATION"
    case other = "OTHER"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .food: String(localized: "expense_category_food")
        case .rent: String(localized: "expense_category_rent")
        case .salary: String(localized: "expense_category_salary")
        case .entertainment: String(localized: "expense_category_entertainment")
        case .transportation: String(localized: "expense_category_transportation")
        case .utilities: String(localized: "expense_category_utilities")
        case .shopping: String(localized: "expense_category_shopping")
        case .healthcare: String(localized: "expense_category_healthcare")
        case .education: String(localized: "expense_category_education")
        case .other: String(localized: "expense_category_other")
        }
    }
}
