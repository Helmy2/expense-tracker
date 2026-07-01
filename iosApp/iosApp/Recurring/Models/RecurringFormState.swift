import Foundation

@Observable
final class RecurringFormState {
    var templateId: String?
    var amountText: String = ""
    var selectedType: ExpenseType = .expense
    var selectedCategory: String = ExpenseCategory.food.rawValue
    var selectedFrequency: RecurringFrequencySwift = .monthly
    var noteText: String = ""
    var startDate: Date = Date()
    var endDate: Date?
    var hasEndDate: Bool = false
    var isSaving: Bool = false
    var amountError: Bool = false

    var categoryDisplayName: String {
        resolveCategoryDisplayName(selectedCategory, type: selectedType)
    }

    var isValid: Bool {
        guard let amount = Double(amountText), amount > 0 else { return false }
        return true
    }

    var isEditMode: Bool {
        templateId != nil
    }

    func reset() {
        templateId = nil
        amountText = ""
        selectedType = .expense
        selectedCategory = ExpenseCategory.food.rawValue
        selectedFrequency = .monthly
        noteText = ""
        startDate = Date()
        endDate = nil
        hasEndDate = false
        isSaving = false
        amountError = false
    }

    func populate(from template: RecurringTemplateItem) {
        templateId = template.id
        amountText = String(format: "%.2f", template.amount)
        selectedType = template.type
        selectedCategory = template.category
        selectedFrequency = template.frequency
        noteText = template.note
        startDate = Date(timeIntervalSince1970: TimeInterval(template.startDateMillis) / 1000)
        if let endMillis = template.endDateMillis {
            endDate = Date(timeIntervalSince1970: TimeInterval(endMillis) / 1000)
            hasEndDate = true
        } else {
            endDate = nil
            hasEndDate = false
        }
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

    /// Call when selectedType changes to reset category to the first entry of the new type.
    func resetCategoryForType() {
        selectedCategory = firstCategoryRawValue(for: selectedType)
    }

    var startDateMillis: Int64 {
        Int64(startDate.timeIntervalSince1970 * 1000)
    }

    var endDateMillis: Int64? {
        guard hasEndDate, let endDate else { return nil }
        return Int64(endDate.timeIntervalSince1970 * 1000)
    }
}
