import Foundation

@Observable
final class BudgetDetailViewModel {
    enum ContentState {
        case loading
        case content(BudgetWithSpendingData, [ExpenseItem])
        case error(String)
    }

    private let bridge: any BudgetRepositoryBridge

    var contentState: ContentState = .loading
    var showingDeleteConfirmation = false

    init(
        bridge: any BudgetRepositoryBridge = AppDependencies.shared.budgetBridge
    ) {
        self.bridge = bridge
    }

    func load(budgetId: String) async {
        contentState = .loading

        do {
            guard let detail = try await bridge.loadBudgetDetail(id: budgetId) else {
                contentState = .error("Budget not found")
                return
            }
            contentState = .content(detail.budgetWithSpending, detail.transactions)
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
}
