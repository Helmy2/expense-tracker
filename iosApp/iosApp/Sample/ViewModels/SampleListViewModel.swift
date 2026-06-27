import Foundation

@Observable
final class SampleListViewModel {
    enum ContentState {
        case loading
        case empty
        case content([SampleItemSwift])
        case error(String)
    }

    private let bridge: any SampleRepositoryBridge
    private let nowMillis: () -> Int64

    var contentState: ContentState = .loading
    var selectedItem: SampleItemSwift?
    var showForm: Bool = false
    var formState = SampleFormState()

    init(
        bridge: any SampleRepositoryBridge = AppDependencies.shared.bridge,
        nowMillis: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }
    ) {
        self.bridge = bridge
        self.nowMillis = nowMillis
    }

    func load(force: Bool = false) async {
        if case .content = contentState, !force { return }
        contentState = .loading

        do {
            let items = try await bridge.loadItems()
            contentState = items.isEmpty ? .empty : .content(items)
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func selectItem(_ item: SampleItemSwift) {
        selectedItem = item
    }

    func dismissDetail() {
        selectedItem = nil
    }

    func startCreate() {
        formState.reset()
        showForm = true
    }

    func saveItem() async {
        guard formState.validate() else { return }
        formState.isSaving = true

        do {
            if let id = formState.id, let existing = try await bridge.loadItem(id: id) {
                var updated = existing
                updated = SampleItemSwift(
                    id: existing.id,
                    title: formState.title,
                    description: formState.description,
                    category: formState.category,
                    occurredAtMillis: existing.occurredAtMillis,
                    updatedAtMillis: nowMillis()
                )
                _ = try await bridge.updateItem(updated)
            } else {
                _ = try await bridge.createItem(
                    title: formState.title,
                    description: formState.description,
                    category: formState.category
                )
            }

            formState.isSaving = false
            showForm = false
            await load(force: true)
        } catch {
            formState.isSaving = false
            contentState = .error(error.localizedDescription)
        }
    }
}
