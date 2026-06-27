import Foundation

@Observable
final class ExpenseListViewModel {
    enum ContentState {
        case loading
        case empty
        case content([ExpenseItem])
        case error(String)
    }

    private let bridge: any TransactionRepositoryBridge
    private let nowMillis: () -> Int64

    var contentState: ContentState = .loading
    var dashboard = DashboardData()
    var formState = ExpenseFormState()
    var showingDeleteConfirmation = false
    var pendingDeleteId: String?
    var showFormSheet = false

    init(
        bridge: any TransactionRepositoryBridge = AppDependencies.shared.expenseBridge,
        nowMillis: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }
    ) {
        self.bridge = bridge
        self.nowMillis = nowMillis
    }

    func load(force: Bool = false) async {
        if case .content = contentState, !force { return }
        contentState = .loading

        do {
            let items = try await bridge.loadTransactions()
            computeDashboard(items)
            contentState = items.isEmpty ? .empty : .content(items)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func saveTransaction() async {
        guard formState.validate() else { return }
        formState.isSaving = true

        do {
            let amount = Double(formState.amountText) ?? 0
            _ = try await bridge.addTransaction(
                amount: amount,
                type: formState.selectedType,
                category: formState.selectedCategory,
                note: formState.noteText
            )
            formState.isSaving = false
            formState.reset()
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
            try await bridge.deleteTransaction(id: id)
            await load(force: true)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    private func computeDashboard(_ items: [ExpenseItem]) {
        dashboard = DashboardData.compute(from: items)
    }
}
