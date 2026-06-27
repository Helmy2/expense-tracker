import SwiftUI

struct BudgetFormView: View {
    @Bindable var viewModel: BudgetListViewModel
    @Environment(\.dreamColors) private var colors

    private var isEditMode: Bool {
        viewModel.editingBudgetId != nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: DreamSpacing.sm) {
            Text(isEditMode ? "budget_form_edit_title" : "budget_form_create_title")
                .font(.dreamTitleMedium)
                .foregroundStyle(colors.onSurface)

            // Category Picker
            Text("budget_category_label")
                .font(.dreamLabelLarge)
                .foregroundStyle(colors.onSurfaceVariant)

            Menu {
                ForEach(viewModel.formState.availableCategories) { category in
                    Button(category.displayName) {
                        viewModel.formState.selectedCategory = category
                    }
                }
            } label: {
                HStack {
                    Text(viewModel.formState.selectedCategory.displayName)
                        .foregroundStyle(isEditMode ? colors.onSurfaceVariant : colors.onSurface)
                    Spacer()
                    if !isEditMode {
                        Image(systemName: "chevron.down")
                            .foregroundStyle(colors.onSurfaceVariant)
                    }
                }
                .padding(DreamSpacing.sm)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(colors.outline, lineWidth: 1)
                )
            }
            .disabled(isEditMode)

            // Monthly Limit
            DreamTextField(
                "budget_limit_placeholder",
                text: $viewModel.formState.limitText,
                isError: viewModel.formState.limitError,
                errorMessage: "budget_limit_validation"
            )
            .keyboardType(.decimalPad)

            // Save Button
            DreamButton(
                "budget_save",
                variant: .primary,
                size: .large,
                isEnabled: viewModel.formState.isFormValid && !viewModel.formState.isSaving
            ) {
                Task { await viewModel.saveBudget() }
            }
        }
        .padding(.horizontal, DreamSpacing.md)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(.regularMaterial)
    }
}

#Preview {
    NavigationStack {
        BudgetFormView(viewModel: BudgetListViewModel())
    }
    .dreamTheme()
}
