import Foundation

struct BudgetDetailData: Equatable {
    let budgetWithSpending: BudgetWithSpendingData
    let transactions: [ExpenseItem]
}
