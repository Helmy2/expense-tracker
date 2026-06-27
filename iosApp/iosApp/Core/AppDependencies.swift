import Foundation

@Observable
final class AppDependencies {
    static let shared = AppDependencies()

    let expenseBridge: any TransactionRepositoryBridge

    private init() {
        expenseBridge = SharedTransactionRepositoryBridge()
    }
}
