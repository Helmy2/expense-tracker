import Foundation

struct RecurringTemplateItem: Identifiable, Hashable {
    let id: String
    let amount: Double
    let type: ExpenseType
    let category: ExpenseCategory
    let note: String
    let frequency: RecurringFrequencySwift
    let startDateMillis: Int64
    let endDateMillis: Int64?
    let isPaused: Bool
    let lastGeneratedDateMillis: Int64?
    let createdAtMillis: Int64
    let updatedAtMillis: Int64

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

    var displayFrequency: String {
        frequency.displayName
    }

    var displayNextDueDate: String {
        guard let lastGenerated = lastGeneratedDateMillis else {
            // If never generated, use start date
            return displayDate(from: startDateMillis)
        }
        let lastDate = Date(timeIntervalSince1970: TimeInterval(lastGenerated) / 1000)
        let calendar = Calendar.current

        let nextDate: Date
        switch frequency {
        case .daily:
            nextDate = calendar.date(byAdding: .day, value: 1, to: lastDate) ?? lastDate
        case .weekly:
            nextDate = calendar.date(byAdding: .day, value: 7, to: lastDate) ?? lastDate
        case .monthly:
            nextDate = calendar.date(byAdding: .month, value: 1, to: lastDate) ?? lastDate
        case .yearly:
            nextDate = calendar.date(byAdding: .year, value: 1, to: lastDate) ?? lastDate
        }

        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: nextDate)
    }

    var displayStartDate: String {
        displayDate(from: startDateMillis)
    }

    var displayEndDate: String? {
        guard let endDateMillis else { return nil }
        return displayDate(from: endDateMillis)
    }

    private func displayDate(from millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(millis) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }
}
