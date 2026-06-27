import Foundation

@Observable
final class BudgetFormState {
    var selectedCategory: ExpenseCategory = .other
    var limitText: String = ""
    var isSaving: Bool = false
    var limitError: Bool = false
    var categoryMenuExpanded: Bool = false
    var availableCategories: [ExpenseCategory] = ExpenseCategory.allCases

    var isFormValid: Bool {
        guard let limit = Double(limitText), limit > 0 else { return false }
        return true
    }

    func reset() {
        selectedCategory = .other
        limitText = ""
        isSaving = false
        limitError = false
        categoryMenuExpanded = false
    }

    func populate(from budget: BudgetItem) {
        selectedCategory = budget.category
        limitText = String(budget.monthlyLimit)
        isSaving = false
        limitError = false
        categoryMenuExpanded = false
    }

    func validate() -> Bool {
        guard let limit = Double(limitText), limit > 0 else {
            limitError = true
            return false
        }
        limitError = false
        return true
    }
}
