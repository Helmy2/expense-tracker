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

    var contentState: ContentState = .loading
    var showFormSheet = false
    var formState = BudgetFormState()
    var editingBudgetId: String?
    var showingDeleteConfirmation = false
    var pendingDeleteId: String?

    init(
        bridge: any BudgetRepositoryBridge = AppDependencies.shared.budgetBridge
    ) {
        self.bridge = bridge
    }

    func load(force: Bool = false) async {
        if case .content = contentState, !force { return }
        contentState = .loading

        do {
            let items = try await bridge.loadBudgetsWithSpending()
            let sorted = items.sorted { $0.budget.category.displayName < $1.budget.category.displayName }
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
}
