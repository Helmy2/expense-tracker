import SwiftUI

struct SampleDetailView: View {
    @State private var viewModel: SampleDetailViewModel
    @Environment(\.dreamColors) private var colors

    init(item: SampleItemSwift) {
        _viewModel = State(initialValue: SampleDetailViewModel(item: item))
    }

    var body: some View {
        List {
            if viewModel.isEditing {
                editingSection
            } else {
                displaySection
            }
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                if viewModel.isEditing {
                    Button("sample_cancel") { viewModel.cancelEditing() }
                } else {
                    Button("sample_edit") { viewModel.startEditing() }
                }
            }
        }
        .navigationTitle("sample_detail_title")
        .dreamTheme()
    }

    private var displaySection: some View {
        Section {
            Text(viewModel.item.displayTitle)
                .font(.dreamTitleLarge)
                .foregroundStyle(colors.onSurface)

            Text(viewModel.item.displayDescription)
                .font(.dreamBodyLarge)
                .foregroundStyle(colors.onSurface)

            HStack {
                Text("sample_category_label")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
                Spacer()
                Text(viewModel.item.category.displayName)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurface)
            }

            HStack {
                Text("sample_occurred_label")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
                Spacer()
                Text(viewModel.item.displayDate)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurface)
            }
        }
    }

    private var editingSection: some View {
        Section {
            DreamTextField(
                "sample_title_label",
                text: $viewModel.formState.title,
                isError: viewModel.formState.titleError,
                errorMessage: "sample_validation_required"
            )

            DreamTextField(
                "sample_description_label",
                text: $viewModel.formState.description,
                isError: viewModel.formState.descriptionError,
                errorMessage: "sample_validation_required",
                axis: .vertical,
                lineLimit: 2...6
            )

            DreamSegmentedPicker(selection: $viewModel.formState.category) {
                ForEach(SampleCategory.allCases) { category in
                    Text(category.displayName).tag(category)
                }
            }

            DreamButton(
                "sample_save",
                variant: .primary,
                isEnabled: viewModel.formState.isValid && !viewModel.formState.isSaving
            ) {
                Task { await viewModel.saveChanges() }
            }
        }
    }
}

#Preview {
    NavigationStack {
        SampleDetailView(
            item: SampleItemSwift(
                id: "1",
                title: "Contract-first features",
                description: "Generate features from compact contracts stored in .features/.",
                category: .contract,
                occurredAtMillis: 1_720_000_000_000,
                updatedAtMillis: 1_720_000_000_000
            )
        )
    }
}
