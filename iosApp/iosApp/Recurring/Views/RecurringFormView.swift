import SwiftUI

struct RecurringFormView: View {
    let templateId: String?
    let onDismiss: () -> Void

    @State private var viewModel = RecurringFormViewModel()
    @Environment(\.dreamColors) private var colors
    @Environment(\.dismiss) private var dismiss

    init(templateId: String?, onDismiss: @escaping () -> Void = {}) {
        self.templateId = templateId
        self.onDismiss = onDismiss
    }

    var body: some View {
        Group {
            switch viewModel.contentState {
            case .loading:
                DreamCircularProgressIndicator(size: .large)

            case .ready:
                formContent

            case .error(let message):
                DreamErrorStateView(
                    title: "recurring_error_title",
                    message: message,
                    retryLabel: "recurring_retry",
                    onRetry: {
                        if let id = templateId {
                            Task { await viewModel.loadTemplate(id: id) }
                        }
                    }
                )
            }
        }
        .navigationTitle(viewModel.isEditMode ? "recurring_form_edit_title" : "recurring_form_create_title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if viewModel.isEditMode {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.showDeleteConfirmation = true
                    } label: {
                        Image(systemName: "trash")
                            .foregroundStyle(colors.error)
                    }
                    .accessibilityLabel(String(localized: "recurring_ios_delete_template"))
                }
            }
        }
        .alert("recurring_delete_title", isPresented: $viewModel.showDeleteConfirmation) {
            Button("recurring_delete_dismiss", role: .cancel) { }
            Button("recurring_delete_confirm", role: .destructive) {
                Task { await viewModel.delete() }
            }
        } message: {
            Text("recurring_delete_body")
        }
        .task {
            if let id = templateId {
                await viewModel.loadTemplate(id: id)
            }
        }
        .onChange(of: viewModel.didSaveSuccessfully) { _, saved in
            if saved {
                onDismiss()
                dismiss()
            }
        }
        .overlay(alignment: .bottom) {
            if let message = viewModel.toastMessage {
                Text(message)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurface)
                    .padding(.horizontal, DreamSpacing.md)
                    .padding(.vertical, DreamSpacing.sm)
                    .background(colors.surfaceVariant)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .padding(.bottom, DreamSpacing.lg)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                            withAnimation {
                                viewModel.toastMessage = nil
                            }
                        }
                    }
            }
        }
    }

    private var formContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DreamSpacing.md) {
                // Amount
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_amount_label")
                        .font(.dreamLabelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DreamTextField(
                        "recurring_ios_amount_placeholder",
                        text: $viewModel.formState.amountText,
                        isError: viewModel.formState.amountError,
                        errorMessage: "recurring_amount_validation"
                    )
                    .keyboardType(.decimalPad)
                }

                // Type
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_type_label")
                        .font(.dreamLabelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DreamSegmentedPicker(selection: $viewModel.formState.selectedType) {
                        ForEach(ExpenseType.allCases) { type in
                            Text(type.displayName).tag(type)
                        }
                    }
                }

                // Category
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_category_label")
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
                                .foregroundStyle(viewModel.isEditMode ? colors.onSurfaceVariant : colors.onSurface)
                            Spacer()
                            if !viewModel.isEditMode {
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
                    .disabled(viewModel.isEditMode)
                }

                // Note
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_note_label")
                        .font(.dreamLabelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DreamTextField(
                        "recurring_ios_note_placeholder",
                        text: $viewModel.formState.noteText
                    )
                }

                // Frequency
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_frequency_label")
                        .font(.dreamLabelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DreamSegmentedPicker(selection: $viewModel.formState.selectedFrequency) {
                        ForEach(RecurringFrequencySwift.allCases) { freq in
                            Text(freq.displayName).tag(freq)
                        }
                    }
                }

                // Start Date
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("recurring_start_date_label")
                        .font(.dreamLabelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DatePicker(
                        "recurring_start_date_label",
                        selection: $viewModel.formState.startDate,
                        displayedComponents: .date
                    )
                    .datePickerStyle(.compact)
                    .labelsHidden()
                    .padding(DreamSpacing.sm)
                    .background(colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(colors.outline, lineWidth: 1)
                    )
                }

                // End Date (optional)
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    HStack {
                        Text("recurring_end_date_label")
                            .font(.dreamLabelLarge)
                            .foregroundStyle(colors.onSurfaceVariant)

                        Spacer()

                        if viewModel.formState.hasEndDate {
                            Button("recurring_end_date_clear") {
                                viewModel.formState.hasEndDate = false
                                viewModel.formState.endDate = nil
                            }
                            .font(.dreamBodyMedium)
                            .foregroundStyle(colors.primary)
                        }
                    }

                    if viewModel.formState.hasEndDate {
                        DatePicker(
                            "recurring_end_date_label",
                            selection: Binding(
                                get: { viewModel.formState.endDate ?? Date() },
                                set: { viewModel.formState.endDate = $0 }
                            ),
                            displayedComponents: .date
                        )
                        .datePickerStyle(.compact)
                        .labelsHidden()
                        .padding(DreamSpacing.sm)
                        .background(colors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(colors.outline, lineWidth: 1)
                        )
                    } else {
                        Button("recurring_ios_add_end_date") {
                            viewModel.formState.hasEndDate = true
                            viewModel.formState.endDate = Calendar.current.date(byAdding: .year, value: 1, to: Date())
                        }
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.primary)
                    }
                }

                // Save Button
                DreamButton(
                    "recurring_save",
                    variant: .primary,
                    size: .large,
                    isEnabled: viewModel.formState.isValid && !viewModel.formState.isSaving
                ) {
                    Task { await viewModel.save() }
                }
                .padding(.top, DreamSpacing.sm)
            }
            .padding(.horizontal, DreamSpacing.md)
            .padding(.vertical, DreamSpacing.sm)
        }
        .scrollDismissesKeyboard(.interactively)
    }
}

#Preview {
    NavigationStack {
        RecurringFormView(templateId: nil)
    }
    .dreamTheme()
}
