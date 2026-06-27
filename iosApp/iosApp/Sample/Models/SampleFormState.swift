import Foundation

@Observable
final class SampleFormState {
    var id: String?
    var title: String = ""
    var description: String = ""
    var category: SampleCategory = .contract
    var titleError: Bool = false
    var descriptionError: Bool = false
    var isSaving: Bool = false

    var isValid: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty &&
        !description.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var isEditing: Bool { id != nil }

    func reset() {
        id = nil
        title = ""
        description = ""
        category = .contract
        titleError = false
        descriptionError = false
        isSaving = false
    }

    func validate() -> Bool {
        titleError = title.trimmingCharacters(in: .whitespaces).isEmpty
        descriptionError = description.trimmingCharacters(in: .whitespaces).isEmpty
        return !titleError && !descriptionError
    }
}
