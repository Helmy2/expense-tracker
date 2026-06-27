import Foundation

@Observable
final class BudgetListViewModel {
    enum ContentState {
        case loading
        case empty
        case content([BudgetWithSpendingData])
        case error(String)
    }

    private let bridge: any BudgetRepositoryBridge
    private let transactionBridge: any TransactionRepositoryBridge

    var contentState: ContentState = .loading
    var showFormSheet = false
    var formState = BudgetFormState()
    var editingBudgetId: String?
    var showingDeleteConfirmation = false
    var pendingDeleteId: String?

    init(
        bridge: any BudgetRepositoryBridge = AppDependencies.shared.budgetBridge,
        transactionBridge: any TransactionRepositoryBridge = AppDependencies.shared.expenseBridge
    ) {
        self.bridge = bridge
        self.transactionBridge = transactionBridge
    }

    func load(force: Bool = false) async {
        if case .content = contentState, !force { return }
        contentState = .loading

        do {
            let budgets = try await bridge.loadBudgets()
            let transactions = try await transactionBridge.loadTransactions()

            let budgetsWithSpending = budgets.map { budget in
                let spent = computeSpending(
                    for: budget.category,
                    transactions: transactions
                )
                return BudgetWithSpendingData.compute(budget: budget, spentAmount: spent)
            }

            let sorted = budgetsWithSpending.sorted { $0.budget.category.displayName < $1.budget.category.displayName }
            contentState = sorted.isEmpty ? .empty : .content(sorted)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func saveBudget() async {
        guard formState.validate() else { return }
        formState.isSaving = true

        do {
            let limit = Double(formState.limitText) ?? 0

            if let editId = editingBudgetId {
                _ = try await bridge.updateBudget(id: editId, monthlyLimit: limit)
            } else {
                _ = try await bridge.createBudget(
                    category: formState.selectedCategory,
                    monthlyLimit: limit
                )
            }

            formState.isSaving = false
            formState.reset()
            editingBudgetId = nil
            showFormSheet = false
            await load(force: true)
        } catch {
            formState.isSaving = false
            contentState = .error(error.localizedDescription)
        }
    }

    func confirmDelete() async {
        guard let id = pendingDeleteId else { return }
        showingDeleteConfirmation = false
        pendingDeleteId = nil

        do {
            try await bridge.deleteBudget(id: id)
            await load(force: true)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func startEdit(budget: BudgetWithSpendingData) {
        editingBudgetId = budget.budget.id
        formState.populate(from: budget.budget)
        showFormSheet = true
    }

    func startCreate() {
        editingBudgetId = nil
        formState.reset()
        showFormSheet = true
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
}
