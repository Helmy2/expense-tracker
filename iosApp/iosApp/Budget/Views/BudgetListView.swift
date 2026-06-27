import SwiftUI

struct BudgetListView: View {
    @State private var viewModel = BudgetListViewModel()
    @State private var selectedBudgetId: String?
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
                        title: "budget_error_title",
                        message: message,
                        retryLabel: "budget_retry",
                        onRetry: { Task { await viewModel.load(force: true) } }
                    )
                }
            }
            .navigationTitle("budget_list_title")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.startCreate()
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel(String(localized: "budget_ios_add_budget"))
                }
            }
            .sheet(isPresented: $viewModel.showFormSheet) {
                NavigationStack {
                    BudgetFormView(viewModel: viewModel)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarTrailing) {
                                Button("expense_cancel") {
                                    viewModel.showFormSheet = false
                                    viewModel.editingBudgetId = nil
                                }
                            }
                        }
                }
                .presentationDetents([.medium])
            }
            .alert("budget_delete_title", isPresented: $viewModel.showingDeleteConfirmation) {
                Button("budget_delete_dismiss", role: .cancel) { }
                Button("budget_delete_confirm", role: .destructive) {
                    Task { await viewModel.confirmDelete() }
                }
            } message: {
                Text("budget_delete_body")
            }
            .task { await viewModel.load() }
            .refreshable { await viewModel.load(force: true) }
            .navigationDestination(item: $selectedBudgetId) { budgetId in
                BudgetDetailView(budgetId: budgetId)
            }
        }
        .dreamTheme()
    }

    private var emptyContent: some View {
        VStack(spacing: DreamSpacing.md) {
            DreamCard(variant: .filled) {
                VStack(alignment: .leading, spacing: DreamSpacing.sm) {
                    Text("budget_empty_title")
                        .font(.dreamTitleMedium)
                        .foregroundStyle(colors.onSurface)
                    Text("budget_empty_body")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                }
            }
            .padding(.horizontal, DreamSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func listContent(_ items: [BudgetWithSpendingData]) -> some View {
        ScrollView {
            LazyVStack(spacing: DreamSpacing.sm) {
                ForEach(items) { item in
                        BudgetCardView(
                            item: item,
                            onTap: { selectedBudgetId = item.id },
                            onEdit: { viewModel.startEdit(budget: item) }
                        )
                }
            }
            .padding(.horizontal, DreamSpacing.md)
            .padding(.vertical, DreamSpacing.sm)
        }
    }
}

#Preview {
    BudgetListView()
}
