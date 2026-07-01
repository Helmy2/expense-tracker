import SwiftUI

struct BudgetDetailView: View {
    let budgetId: String
    @State private var viewModel = BudgetDetailViewModel()
    @Environment(\.dreamColors) private var colors
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        Group {
            switch viewModel.contentState {
            case .loading:
                DreamCircularProgressIndicator(size: .large)

            case .content(let budgetWithSpending, let transactions):
                detailContent(budgetWithSpending, transactions: transactions)

            case .error(let message):
                DreamErrorStateView(
                    title: "budget_error_title",
                    message: message,
                    retryLabel: "budget_retry",
                    onRetry: { Task { await viewModel.load(budgetId: budgetId) } }
                )
            }
        }
        .navigationTitle(budgetCategoryName)
        .navigationBarTitleDisplayMode(.inline)
        .task { await viewModel.load(budgetId: budgetId) }
    }

    private var budgetCategoryName: String {
        if case .content(let bws, _) = viewModel.contentState {
            return bws.budget.category.displayName
        }
        return String(localized: "budget_list_title")
    }

    private func detailContent(_ budgetWithSpending: BudgetWithSpendingData, transactions: [ExpenseItem]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DreamSpacing.md) {
                // Summary Card
                summaryCard(budgetWithSpending)

                // Transactions Section
                transactionsSection(transactions, category: budgetWithSpending.budget.category)
            }
            .padding(.horizontal, DreamSpacing.md)
            .padding(.vertical, DreamSpacing.sm)
        }
    }

    private func summaryCard(_ data: BudgetWithSpendingData) -> some View {
        DreamCard(variant: .elevated) {
            VStack(alignment: .leading, spacing: DreamSpacing.sm) {
                Text(data.budget.category.displayName)
                    .font(.dreamTitleMedium)
                    .foregroundStyle(colors.onSurface)

                HStack(spacing: DreamSpacing.lg) {
                    VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                        Text("budget_spent_label")
                            .font(.dreamBodyMedium)
                            .foregroundStyle(colors.onSurfaceVariant)
                        Text(data.formattedSpent)
                            .font(.dreamTitleLarge)
                            .foregroundStyle(colors.error)
                            .minimumScaleFactor(0.6)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                        Text("budget_remaining_label")
                            .font(.dreamBodyMedium)
                            .foregroundStyle(colors.onSurfaceVariant)
                        Text(data.formattedRemaining)
                            .font(.dreamTitleLarge)
                            .foregroundStyle(data.remainingAmount < 0 ? colors.error : Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255))
                            .minimumScaleFactor(0.6)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }

                BudgetProgressBar(
                    percentage: data.clampedPercentage,
                    barColor: data.status.barColor
                )

                if data.isOverBudget {
                    HStack(spacing: DreamSpacing.xs) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(colors.error)
                        Text("budget_over_budget_warning")
                            .font(.dreamBodyMedium)
                            .foregroundStyle(colors.error)
                    }
                }
            }
        }
    }

    private func transactionsSection(_ transactions: [ExpenseItem], category: ExpenseCategory) -> some View {
        VStack(alignment: .leading, spacing: DreamSpacing.sm) {
            Text("budget_transactions_title")
                .font(.dreamTitleMedium)
                .foregroundStyle(colors.onSurface)

            Divider()

            if transactions.isEmpty {
                DreamCard(variant: .filled) {
                    VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                        Text("budget_no_transactions_title")
                            .font(.dreamTitleMedium)
                            .foregroundStyle(colors.onSurface)
                        Text("budget_no_transactions_body")
                            .font(.dreamBodyMedium)
                            .foregroundStyle(colors.onSurfaceVariant)
                    }
                }
            } else {
                ForEach(transactions) { transaction in
                    transactionRow(transaction)
                    if transaction.id != transactions.last?.id {
                        Divider()
                            .padding(.leading, DreamSpacing.md)
                    }
                }
            }
        }
    }

    private func transactionRow(_ transaction: ExpenseItem) -> some View {
        HStack(spacing: DreamSpacing.sm) {
            Circle()
                .fill(Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255).opacity(0.15))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: "arrow.down")
                        .foregroundStyle(Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255))
                )

            VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                Text(transaction.formattedAmount)
                    .font(.dreamTitleMedium)
                    .foregroundStyle(colors.onSurface)
                Text("\(transaction.categoryDisplayName) \u{00B7} \(transaction.displayDate)")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
            }

            Spacer()
        }
        .padding(.vertical, DreamSpacing.xs)
    }
}

#Preview {
    NavigationStack {
        BudgetDetailView(budgetId: "preview")
    }
    .dreamTheme()
}
