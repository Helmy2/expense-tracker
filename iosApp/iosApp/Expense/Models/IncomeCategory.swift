import Foundation

enum IncomeCategory: String, CaseIterable, Identifiable {
    case salary = "SALARY"
    case freelance = "FREELANCE"
    case investment = "INVESTMENT"
    case business = "BUSINESS"
    case rental = "RENTAL"
    case gift = "GIFT"
    case refund = "REFUND"
    case otherIncome = "OTHER_INCOME"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .salary: String(localized: "income_category_salary")
        case .freelance: String(localized: "income_category_freelance")
        case .investment: String(localized: "income_category_investment")
        case .business: String(localized: "income_category_business")
        case .rental: String(localized: "income_category_rental")
        case .gift: String(localized: "income_category_gift")
        case .refund: String(localized: "income_category_refund")
        case .otherIncome: String(localized: "income_category_other_income")
        }
    }
}

/// Resolve category display name from a raw string and transaction type.
func resolveCategoryDisplayName(_ categoryName: String, type: ExpenseType) -> String {
    switch type {
    case .income:
        return IncomeCategory(rawValue: categoryName)?.displayName ?? categoryName
    case .expense:
        return ExpenseCategory(rawValue: categoryName)?.displayName ?? categoryName
    }
}

/// Return the list of category entries (rawValue + displayName) for a given transaction type.
func categoriesForType(_ type: ExpenseType) -> [(rawValue: String, displayName: String)] {
    switch type {
    case .income:
        return IncomeCategory.allCases.map { ($0.rawValue, $0.displayName) }
    case .expense:
        return ExpenseCategory.allCases.map { ($0.rawValue, $0.displayName) }
    }
}

/// Return the first category raw value for a given transaction type.
func firstCategoryRawValue(for type: ExpenseType) -> String {
    switch type {
    case .income:
        return IncomeCategory.salary.rawValue
    case .expense:
        return ExpenseCategory.food.rawValue
    }
}
