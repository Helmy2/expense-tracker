import Foundation

struct DashboardData {
    let totalBalance: Double
    let totalIncome: Double
    let totalExpenses: Double

    init(
        totalBalance: Double = 0,
        totalIncome: Double = 0,
        totalExpenses: Double = 0
    ) {
        self.totalBalance = totalBalance
        self.totalIncome = totalIncome
        self.totalExpenses = totalExpenses
    }

    static func compute(from transactions: [ExpenseItem]) -> DashboardData {
        let totalIncome = transactions
            .filter { $0.type == .income }
            .reduce(0) { $0 + $1.amount }
        let totalExpenses = transactions
            .filter { $0.type == .expense }
            .reduce(0) { $0 + $1.amount }
        return DashboardData(
            totalBalance: totalIncome - totalExpenses,
            totalIncome: totalIncome,
            totalExpenses: totalExpenses
        )
    }
}
