import Foundation

@Observable
final class RecurringFormViewModel {
    enum ContentState {
        case loading
        case ready
        case error(String)
    }

    private let bridge: any RecurringRepositoryBridge

    var contentState: ContentState = .ready
    var formState = RecurringFormState()
    var showDeleteConfirmation = false
    var didSaveSuccessfully = false
    var toastMessage: String?
    var isEditMode: Bool { formState.isEditMode }

    init(
        bridge: any RecurringRepositoryBridge = AppDependencies.shared.recurringBridge
    ) {
        self.bridge = bridge
    }

    func loadTemplate(id: String) async {
        contentState = .loading
        do {
            guard let template = try await bridge.loadTemplateById(id: id) else {
                contentState = .error("Template not found")
                return
            }
            formState.populate(from: template)
            contentState = .ready
        } catch {
            contentState = .error(error.localizedDescription)
        }
    }

    func save() async {
        guard formState.validate() else { return }
        formState.isSaving = true

        do {
            let amount = Double(formState.amountText) ?? 0

            if let editId = formState.templateId {
                _ = try await bridge.updateTemplate(
                    id: editId,
                    amount: amount,
                    type: formState.selectedType,
                    category: formState.selectedCategory,
                    note: formState.noteText,
                    frequency: formState.selectedFrequency,
                    startDateMillis: formState.startDateMillis,
                    endDateMillis: formState.endDateMillis
                )
                toastMessage = String(localized: "recurring_updated_snackbar")
            } else {
                _ = try await bridge.createTemplate(
                    amount: amount,
                    type: formState.selectedType,
                    category: formState.selectedCategory,
                    note: formState.noteText,
                    frequency: formState.selectedFrequency,
                    startDateMillis: formState.startDateMillis,
                    endDateMillis: formState.endDateMillis
                )
                toastMessage = String(localized: "recurring_created_snackbar")
            }

            formState.isSaving = false
            didSaveSuccessfully = true
        } catch {
            formState.isSaving = false
            toastMessage = error.localizedDescription
        }
    }

    func delete() async {
        guard let id = formState.templateId else { return }
        showDeleteConfirmation = false

        do {
            try await bridge.deleteTemplate(id: id)
            toastMessage = String(localized: "recurring_deleted_snackbar")
            didSaveSuccessfully = true
        } catch {
            toastMessage = error.localizedDescription
        }
    }
}
