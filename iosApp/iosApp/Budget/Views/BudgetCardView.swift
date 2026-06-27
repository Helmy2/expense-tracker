import SwiftUI

struct BudgetCardView: View {
    let item: BudgetWithSpendingData
    let onTap: () -> Void
    let onEdit: () -> Void
    @Environment(\.dreamColors) private var colors

    var body: some View {
        DreamCard(variant: .elevated, action: onTap) {
            VStack(alignment: .leading, spacing: DreamSpacing.sm) {
                // Row 1 — Header
                HStack {
                    Circle()
                        .fill(item.status.barColor.opacity(0.15))
                        .frame(width: 40, height: 40)
                        .overlay(
                            Image(systemName: "chart.pie")
                                .foregroundStyle(item.status.barColor)
                        )

                    Text(item.budget.category.displayName)
                        .font(.dreamTitleMedium)
                        .foregroundStyle(colors.onSurface)

                    Spacer()

                    Button(action: onEdit) {
                        Image(systemName: "pencil")
                            .font(.system(size: 15))
                            .foregroundStyle(colors.onSurfaceVariant)
                            .padding(10)
                            .background(.regularMaterial, in: Circle())
                    }
                    .accessibilityLabel(String(localized: "budget_ios_edit_budget"))
                }

                // Row 2 — Amounts
                Text("\(item.formattedSpent) of \(item.budget.formattedLimit)")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)

                // Row 3 — Progress bar
                BudgetProgressBar(
                    percentage: item.clampedPercentage,
                    barColor: item.status.barColor
                )

                // Row 4 — Over-budget warning
                if item.isOverBudget {
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
}

struct BudgetProgressBar: View {
    let percentage: Double
    let barColor: Color
    @Environment(\.dreamColors) private var colors

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(colors.surfaceVariant)
                    .frame(height: 8)

                RoundedRectangle(cornerRadius: 4)
                    .fill(barColor)
                    .frame(width: geometry.size.width * percentage, height: 8)
            }
        }
        .frame(height: 8)
    }
}

#Preview {
    VStack(spacing: 16) {
        BudgetCardView(
            item: BudgetWithSpendingData.compute(
                budget: BudgetItem(
                    id: "1",
                    category: .food,
                    monthlyLimit: 500,
                    createdAtMillis: 0,
                    updatedAtMillis: 0
                ),
                spentAmount: 150
            ),
            onTap: {},
            onEdit: {}
        )

        BudgetCardView(
            item: BudgetWithSpendingData.compute(
                budget: BudgetItem(
                    id: "2",
                    category: .entertainment,
                    monthlyLimit: 200,
                    createdAtMillis: 0,
                    updatedAtMillis: 0
                ),
                spentAmount: 180
            ),
            onTap: {},
            onEdit: {}
        )
    }
    .padding()
    .dreamTheme()
}
