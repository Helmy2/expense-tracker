import Foundation

struct BudgetItem: Identifiable, Hashable {
    let id: String
    let category: ExpenseCategory
    let monthlyLimit: Double
    let createdAtMillis: Int64
    let updatedAtMillis: Int64

    var formattedLimit: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        return formatter.string(from: NSNumber(value: monthlyLimit)) ?? "$0.00"
    }
}
