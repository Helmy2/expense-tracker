import SwiftUI

struct ExpenseFormView: View {
    @Bindable var viewModel: ExpenseListViewModel
    @Environment(\.dreamColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: DreamSpacing.sm) {
            Text("expense_new_transaction")
                .font(.dreamTitleMedium)
                .foregroundStyle(colors.onSurface)

            DreamTextField(
                "expense_ios_amount_placeholder",
                text: $viewModel.formState.amountText,
                isError: viewModel.formState.amountError,
                errorMessage: "expense_ios_amount_error"
            )
            .keyboardType(.decimalPad)

            Text("expense_ios_type_label")
                .font(.dreamLabelLarge)
                .foregroundStyle(colors.onSurfaceVariant)

            DreamSegmentedPicker(selection: $viewModel.formState.selectedType) {
                ForEach(ExpenseType.allCases) { type in
                    Text(type.displayName).tag(type)
                }
            }

            Text("expense_ios_category_label")
                .font(.dreamLabelLarge)
                .foregroundStyle(colors.onSurfaceVariant)

            Menu {
                ForEach(ExpenseCategory.allCases) { category in
                    Button(category.displayName) {
                        viewModel.formState.selectedCategory = category
                    }
                }
            } label: {
                HStack {
                    Text(viewModel.formState.selectedCategory.displayName)
                        .foregroundStyle(colors.onSurface)
                    Spacer()
                    Image(systemName: "chevron.down")
                        .foregroundStyle(colors.onSurfaceVariant)
                }
                .padding(DreamSpacing.sm)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(colors.outline, lineWidth: 1)
                )
            }

            DreamTextField(
                "expense_ios_note_placeholder",
                text: $viewModel.formState.noteText
            )

            DreamButton(
                "expense_save",
                variant: .primary,
                size: .large,
                isEnabled: viewModel.formState.isValid && !viewModel.formState.isSaving
            ) {
                Task { await viewModel.saveTransaction() }
            }
        }
        .padding(.horizontal, DreamSpacing.md)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(.regularMaterial)
    }
}
