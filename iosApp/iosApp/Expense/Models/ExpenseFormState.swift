import Foundation

@Observable
final class ExpenseFormState {
    var amountText: String = ""
    var selectedType: ExpenseType = .expense
    var selectedCategory: ExpenseCategory = .food
    var noteText: String = ""
    var isSaving: Bool = false
    var amountError: Bool = false

    var isValid: Bool {
        guard let amount = Double(amountText), amount > 0 else { return false }
        return true
    }

    func reset() {
        amountText = ""
        selectedType = .expense
        selectedCategory = .food
        noteText = ""
        isSaving = false
        amountError = false
    }

    func validate() -> Bool {
        guard let amount = Double(amountText), amount > 0 else {
            amountError = true
            return false
        }
        amountError = false
        return true
    }
}
