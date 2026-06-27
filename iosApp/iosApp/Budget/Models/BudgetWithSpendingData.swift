import SwiftUI

struct BudgetWithSpendingData: Identifiable {
    let budget: BudgetItem
    let spentAmount: Double
    let remainingAmount: Double
    let percentage: Double
    let status: BudgetStatus

    var id: String { budget.id }

    var formattedSpent: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        return formatter.string(from: NSNumber(value: spentAmount)) ?? "$0.00"
    }

    var formattedRemaining: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        let absValue = formatter.string(from: NSNumber(value: abs(remainingAmount))) ?? "$0.00"
        return remainingAmount < 0 ? "-\(absValue)" : absValue
    }

    var clampedPercentage: Double {
        min(max(percentage, 0.0), 1.0)
    }

    var isOverBudget: Bool {
        status == .overBudget
    }

    static func compute(
        budget: BudgetItem,
        spentAmount: Double
    ) -> BudgetWithSpendingData {
        let percentage = budget.monthlyLimit > 0 ? spentAmount / budget.monthlyLimit : 0.0
        let remaining = budget.monthlyLimit - spentAmount
        let status = BudgetStatus.compute(from: percentage)
        return BudgetWithSpendingData(
            budget: budget,
            spentAmount: spentAmount,
            remainingAmount: remaining,
            percentage: percentage,
            status: status
        )
    }
}

enum BudgetStatus: Equatable {
    case under75
    case between75_90
    case over90
    case overBudget

    var barColor: Color {
        switch self {
        case .under75: return Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255) // #4CAF50
        case .between75_90: return Color(red: 0xFF / 255, green: 0xC1 / 255, blue: 0x07 / 255) // #FFC107
        case .over90, .overBudget: return Color(red: 0xB4 / 255, green: 0x23 / 255, blue: 0x18 / 255) // error
        }
    }

    static func compute(from percentage: Double) -> BudgetStatus {
        switch percentage {
        case ..<0.75: return .under75
        case 0.75..<0.90: return .between75_90
        case 0.90...1.0: return .over90
        default: return .overBudget
        }
    }
}
