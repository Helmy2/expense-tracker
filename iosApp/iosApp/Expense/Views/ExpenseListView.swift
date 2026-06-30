import SwiftUI

struct ExpenseListView: View {
    @State private var viewModel = ExpenseListViewModel()
    @State private var upcomingItems: [UpcomingRecurringData] = []
    @State private var showRecurringList = false
    @State private var selectedRecurringTemplateId: String?
    @Environment(\.dreamColors) private var colors

    var body: some View {
        NavigationStack {
            Group {
                switch viewModel.contentState {
                case .loading:
                    DreamCircularProgressIndicator(size: .large)

                case .empty:
                    VStack(spacing: DreamSpacing.md) {
                        DreamEmptyStateView(
                            title: "expense_empty_title",
                            message: "expense_empty_body"
                        )
                    }

                case .content(let items):
                    listContent(items)

                case .error(let message):
                    DreamErrorStateView(
                        title: "expense_error_title",
                        message: message,
                        retryLabel: "expense_retry",
                        onRetry: { Task { await viewModel.load(force: true) } }
                    )
                }
            }
            .navigationTitle("expense_title")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    HStack(spacing: DreamSpacing.sm) {
                        NavigationLink {
                            BudgetListView()
                        } label: {
                            Text("budget_list_title")
                        }

                        NavigationLink {
                            RecurringListView()
                        } label: {
                            Text("recurring_nav_button")
                        }
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.showFormSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel(String(localized: "expense_ios_add_transaction"))
                }
            }
            .sheet(isPresented: $viewModel.showFormSheet) {
                NavigationStack {
                    ExpenseFormView(viewModel: viewModel)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarTrailing) {
                                Button("expense_cancel") {
                                    viewModel.showFormSheet = false
                                }
                            }
                        }
                }
                .presentationDetents([.medium])
            }
            .alert("expense_delete_title", isPresented: $viewModel.showingDeleteConfirmation) {
                Button("expense_cancel", role: .cancel) { }
                Button("expense_delete", role: .destructive) {
                    Task { await viewModel.confirmDelete() }
                }
            } message: {
                Text("expense_delete_message")
            }
            .task {
                // Process due recurring transactions on app launch
                try? await AppDependencies.shared.recurringBridge.processDueRecurring()
                await viewModel.load()
            }
            .refreshable { await viewModel.load(force: true) }
            .navigationDestination(isPresented: $showRecurringList) {
                RecurringListView()
            }
            .navigationDestination(item: $selectedRecurringTemplateId) { templateId in
                RecurringFormView(
                    templateId: templateId,
                    onDismiss: { Task { await loadUpcoming() } }
                )
            }
        }
        .dreamTheme()
        .task { await loadUpcoming() }
    }

    private func listContent(_ items: [ExpenseItem]) -> some View {
        List {
            ExpenseDashboardView(dashboard: viewModel.dashboard)
                .listRowSeparator(.hidden)

            // Upcoming Recurring Section
            if !upcomingItems.isEmpty {
                UpcomingRecurringSection(
                    upcomingItems: upcomingItems,
                    onSeeAll: { showRecurringList = true },
                    onItemTap: { templateId in
                        selectedRecurringTemplateId = templateId
                    }
                )
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)
            }

            Section("expense_transactions") {
                ForEach(items) { transaction in
                    TransactionRowView(
                        transaction: transaction,
                        onDelete: {
                            viewModel.pendingDeleteId = transaction.id
                            viewModel.showingDeleteConfirmation = true
                        }
                    )
                }
            }
        }
        .listStyle(.plain)
    }

    private func loadUpcoming() async {
        do {
            upcomingItems = try await AppDependencies.shared.recurringBridge.loadUpcoming(count: 5)
        } catch {
            upcomingItems = []
        }
    }
}

#Preview {
    ExpenseListView()
}
