import Foundation

struct UpcomingRecurringData: Identifiable, Hashable {
    let templateId: String
    let amount: Double
    let type: ExpenseType
    let category: ExpenseCategory
    let note: String
    let frequency: RecurringFrequencySwift
    let nextDueDateMillis: Int64

    var id: String { templateId }

    var formattedAmount: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        let absoluteAmount = formatter.string(from: NSNumber(value: abs(amount))) ?? "$0.00"
        switch type {
        case .income: return "+\(absoluteAmount)"
        case .expense: return "-\(absoluteAmount)"
        }
    }

    var displayDueDate: String {
        let date = Date(timeIntervalSince1970: TimeInterval(nextDueDateMillis) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }

    var displayCategoryAndFrequency: String {
        "\(category.displayName) \u{00B7} \(frequency.displayName)"
    }
}
