import Foundation

@Observable
final class BudgetDetailViewModel {
    enum ContentState {
        case loading
        case content(BudgetWithSpendingData, [ExpenseItem])
        case error(String)
    }

    private let bridge: any BudgetRepositoryBridge
    private let transactionBridge: any TransactionRepositoryBridge

    var contentState: ContentState = .loading
    var showingDeleteConfirmation = false

    init(
        bridge: any BudgetRepositoryBridge = AppDependencies.shared.budgetBridge,
        transactionBridge: any TransactionRepositoryBridge = AppDependencies.shared.expenseBridge
    ) {
        self.bridge = bridge
        self.transactionBridge = transactionBridge
    }

    func load(budgetId: String) async {
        contentState = .loading

        do {
            guard let budget = try await bridge.loadBudgetById(id: budgetId) else {
                contentState = .error("Budget not found")
                return
            }

            let transactions = try await transactionBridge.loadTransactions()
            let spent = computeSpending(
                for: budget.category,
                transactions: transactions
            )
            let budgetWithSpending = BudgetWithSpendingData.compute(budget: budget, spentAmount: spent)

            let categoryTransactions = filterTransactions(
                for: budget.category,
                transactions: transactions
            )

            contentState = .content(budgetWithSpending, categoryTransactions)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func deleteBudget(budgetId: String) async {
        showingDeleteConfirmation = false

        do {
            try await bridge.deleteBudget(id: budgetId)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    private func computeSpending(
        for category: ExpenseCategory,
        transactions: [ExpenseItem]
    ) -> Double {
        let now = Date()
        let calendar = Calendar.current
        let currentMonth = calendar.component(.month, from: now)
        let currentYear = calendar.component(.year, from: now)

        return transactions
            .filter { $0.type == .expense && $0.category == category }
            .filter { item in
                let date = Date(timeIntervalSince1970: TimeInterval(item.createdAtMillis) / 1000)
                return calendar.component(.month, from: date) == currentMonth
                    && calendar.component(.year, from: date) == currentYear
            }
            .reduce(0) { $0 + $1.amount }
    }

    private func filterTransactions(
        for category: ExpenseCategory,
        transactions: [ExpenseItem]
    ) -> [ExpenseItem] {
        let now = Date()
        let calendar = Calendar.current
        let currentMonth = calendar.component(.month, from: now)
        let currentYear = calendar.component(.year, from: now)

        return transactions
            .filter { $0.type == .expense && $0.category == category }
            .filter { item in
                let date = Date(timeIntervalSince1970: TimeInterval(item.createdAtMillis) / 1000)
                return calendar.component(.month, from: date) == currentMonth
                    && calendar.component(.year, from: date) == currentYear
            }
            .sorted { $0.createdAtMillis > $1.createdAtMillis }
    }
}
