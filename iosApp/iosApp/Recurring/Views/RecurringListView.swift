import SwiftUI

struct RecurringListView: View {
    @State private var viewModel = RecurringListViewModel()
    @Environment(\.dreamColors) private var colors

    var body: some View {
        NavigationStack {
            Group {
                switch viewModel.contentState {
                case .loading:
                    DreamCircularProgressIndicator(size: .large)

                case .empty:
                    emptyContent

                case .content(let items):
                    listContent(items)

                case .error(let message):
                    DreamErrorStateView(
                        title: "recurring_error_title",
                        message: message,
                        retryLabel: "recurring_retry",
                        onRetry: { Task { await viewModel.load(force: true) } }
                    )
                }
            }
            .navigationTitle("recurring_title")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.startCreate()
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel(String(localized: "recurring_ios_add_template"))
                }
            }
            .navigationDestination(isPresented: $viewModel.showFormView) {
                RecurringFormView(
                    templateId: viewModel.editingTemplateId,
                    onDismiss: {
                        Task { await viewModel.load(force: true) }
                    }
                )
            }
            .alert("recurring_delete_title", isPresented: $viewModel.showingDeleteConfirmation) {
                Button("recurring_delete_dismiss", role: .cancel) { }
                Button("recurring_delete_confirm", role: .destructive) {
                    Task { await viewModel.confirmDelete() }
                }
            } message: {
                Text("recurring_delete_body")
            }
            .task { await viewModel.load() }
            .refreshable { await viewModel.load(force: true) }
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
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                withAnimation {
                                    viewModel.toastMessage = nil
                                }
                            }
                        }
                }
            }
        }
        .dreamTheme()
    }

    private var emptyContent: some View {
        VStack(spacing: DreamSpacing.md) {
            DreamCard(variant: .filled) {
                VStack(alignment: .leading, spacing: DreamSpacing.sm) {
                    Text("recurring_empty_title")
                        .font(.dreamTitleMedium)
                        .foregroundStyle(colors.onSurface)
                    Text("recurring_empty_body")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)

                    DreamButton("recurring_ios_create_first", variant: .primary) {
                        viewModel.startCreate()
                    }
                    .padding(.top, DreamSpacing.sm)
                }
            }
            .padding(.horizontal, DreamSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func listContent(_ items: [RecurringTemplateItem]) -> some View {
        List {
            ForEach(items) { template in
                RecurringRowView(template: template)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        viewModel.startEdit(templateId: template.id)
                    }
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            viewModel.pendingDeleteId = template.id
                            viewModel.showingDeleteConfirmation = true
                        } label: {
                            Label("recurring_delete_confirm", systemImage: "trash")
                        }
                    }
            }
        }
        .listStyle(.plain)
    }
}

// MARK: - Recurring Row

struct RecurringRowView: View {
    let template: RecurringTemplateItem
    @Environment(\.dreamColors) private var colors
    @State private var isPaused: Bool

    init(template: RecurringTemplateItem) {
        self.template = template
        self._isPaused = State(initialValue: template.isPaused)
    }

    var body: some View {
        HStack(spacing: DreamSpacing.sm) {
            // Icon circle
            Circle()
                .fill(iconColor.opacity(0.15))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: template.type == .income ? "arrow.up" : "arrow.down")
                        .foregroundColor(iconColor)
                )

            // Info
            VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                Text(template.formattedAmount)
                    .font(.dreamTitleMedium)
                    .foregroundStyle(colors.onSurface)

                Text("\(template.categoryDisplayName) \u{00B7} \(template.displayFrequency)")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)

                if !template.isPaused {
                    Text("\(String(localized: "recurring_next_due")): \(template.displayNextDueDate)")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                }
            }

            Spacer()

            // Pause toggle
            VStack(alignment: .trailing, spacing: DreamSpacing.xs) {
                Toggle(isOn: Binding(
                    get: { !template.isPaused },
                    set: { newValue in
                        // Toggle pause via shared view model - handled at list level
                    }
                )) {
                    EmptyView()
                }
                .labelsHidden()
                .toggleStyle(.switch)
                .tint(colors.primary)
                .scaleEffect(0.85)
                .disabled(true) // Handle through list-level action

                if template.isPaused {
                    Text("recurring_paused_label")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                }
            }
        }
        .padding(.vertical, DreamSpacing.xs)
        .opacity(template.isPaused ? 0.5 : 1.0)
    }

    private var iconColor: Color {
        template.type == .income
            ? Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255)
            : Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255)
    }
}

#Preview {
    RecurringListView()
}
