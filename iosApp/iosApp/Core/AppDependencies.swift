import Foundation

@Observable
final class AppDependencies {
    static let shared = AppDependencies()

    let expenseBridge: any TransactionRepositoryBridge
    let budgetBridge: any BudgetRepositoryBridge

    private init() {
        expenseBridge = SharedTransactionRepositoryBridge()
        budgetBridge = SharedBudgetRepositoryBridge()
    }
}
