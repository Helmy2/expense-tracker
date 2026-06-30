import Foundation

@Observable
final class AppDependencies {
    static let shared = AppDependencies()

    let expenseBridge: any TransactionRepositoryBridge
    let budgetBridge: any BudgetRepositoryBridge
    let recurringBridge: any RecurringRepositoryBridge

    private init() {
        expenseBridge = SharedTransactionRepositoryBridge()
        budgetBridge = SharedBudgetRepositoryBridge()
        recurringBridge = SharedRecurringRepositoryBridge()
    }
}
