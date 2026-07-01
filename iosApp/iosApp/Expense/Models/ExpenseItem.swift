import Foundation

struct ExpenseItem: Identifiable, Hashable {
    let id: String
    let amount: Double
    let type: ExpenseType
    let category: String
    let note: String
    let createdAtMillis: Int64

    /// Display name for the category, resolved based on transaction type.
    var categoryDisplayName: String {
        resolveCategoryDisplayName(category, type: type)
    }

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

    var displayDate: String {
        let date = Date(timeIntervalSince1970: TimeInterval(createdAtMillis) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }

    init(
        id: String,
        amount: Double,
        type: ExpenseType,
        category: String,
        note: String,
        createdAtMillis: Int64
    ) {
        self.id = id
        self.amount = amount
        self.type = type
        self.category = category
        self.note = note
        self.createdAtMillis = createdAtMillis
    }
}
