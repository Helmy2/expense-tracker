import SwiftUI

struct SampleFormView: View {
    @Bindable var formState: SampleFormState
    let onSave: () async -> Void
    let onCancel: () -> Void
    @Environment(\.dreamColors) private var colors

    var body: some View {
        NavigationStack {
            Form {
                Section("sample_details_header") {
                    DreamTextField(
                        "sample_title_label",
                        text: $formState.title,
                        isError: formState.titleError,
                        errorMessage: "sample_validation_required"
                    )

                    DreamTextField(
                        "sample_description_label",
                        text: $formState.description,
                        isError: formState.descriptionError,
                        errorMessage: "sample_validation_required",
                        axis: .vertical,
                        lineLimit: 2...6
                    )
                }

                Section("sample_category_label") {
                    DreamSegmentedPicker(selection: $formState.category) {
                        ForEach(SampleCategory.allCases) { category in
                            Text(category.displayName).tag(category)
                        }
                    }
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("sample_cancel", action: onCancel)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        Task { await onSave() }
                    } label: {
                        Text(formState.isEditing ? "sample_save" : "sample_add")
                    }
                    .disabled(!formState.isValid || formState.isSaving)
                }
            }
        }
        .dreamTheme()
    }
}

#Preview {
    SampleFormView(
        formState: SampleFormState(),
        onSave: {},
        onCancel: {}
    )
}
