import Foundation

@Observable
final class SampleDetailViewModel {
    private let bridge: any SampleRepositoryBridge
    private let nowMillis: () -> Int64

    var isEditing = false
    var formState = SampleFormState()
    var item: SampleItemSwift

    init(
        item: SampleItemSwift,
        bridge: any SampleRepositoryBridge = AppDependencies.shared.bridge,
        nowMillis: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }
    ) {
        self.item = item
        self.bridge = bridge
        self.nowMillis = nowMillis
    }

    func startEditing() {
        formState.id = item.id
        formState.title = item.title
        formState.description = item.description
        formState.category = item.category
        isEditing = true
    }

    func cancelEditing() {
        isEditing = false
        formState.titleError = false
        formState.descriptionError = false
    }

    func saveChanges() async {
        guard formState.validate() else { return }
        formState.isSaving = true

        do {
            let updated = SampleItemSwift(
                id: item.id,
                title: formState.title,
                description: formState.description,
                category: formState.category,
                occurredAtMillis: item.occurredAtMillis,
                updatedAtMillis: nowMillis()
            )
            let saved = try await bridge.updateItem(updated)
            item = saved
            isEditing = false
            formState.isSaving = false
        } catch {
            formState.isSaving = false
        }
    }
}
