import Foundation

@Observable
final class RecurringListViewModel {
    enum ContentState {
        case loading
        case empty
        case content([RecurringTemplateItem])
        case error(String)
    }

    private let bridge: any RecurringRepositoryBridge

    var contentState: ContentState = .loading
    var selectedTemplateId: String?
    var showingDeleteConfirmation = false
    var pendingDeleteId: String?
    var toastMessage: String?
    var showFormView = false
    var editingTemplateId: String?

    init(
        bridge: any RecurringRepositoryBridge = AppDependencies.shared.recurringBridge
    ) {
        self.bridge = bridge
    }

    func load(force: Bool = false) async {
        if case .content = contentState, !force { return }
        contentState = .loading

        do {
            let items = try await bridge.loadTemplates()
            let sorted = items.sorted { $0.displayNextDueDate < $1.displayNextDueDate }
            contentState = sorted.isEmpty ? .empty : .content(sorted)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func togglePause(id: String) async {
        do {
            let _ = try await bridge.togglePause(id: id)
            await load(force: true)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func confirmDelete() async {
        guard let id = pendingDeleteId else { return }
        showingDeleteConfirmation = false
        pendingDeleteId = nil

        do {
            try await bridge.deleteTemplate(id: id)
            toastMessage = String(localized: "recurring_deleted_snackbar")
            await load(force: true)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func startCreate() {
        editingTemplateId = nil
        showFormView = true
    }

    func startEdit(templateId: String) {
        editingTemplateId = templateId
        showFormView = true
    }
}
