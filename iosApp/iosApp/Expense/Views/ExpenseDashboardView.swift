import SwiftUI

struct ExpenseDashboardView: View {
    let dashboard: DashboardData
    @Environment(\.dreamColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: DreamSpacing.sm) {
            VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                Text("expense_balance_label")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)

                Text(formatCurrency(dashboard.totalBalance))
                    .font(.dreamHeadlineLarge)
                    .fontWeight(.bold)
                    .foregroundStyle(colors.onSurface)
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
            }

            HStack(spacing: DreamSpacing.md) {
                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("expense_income_label")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                    Text(formatCurrency(dashboard.totalIncome))
                        .font(.dreamTitleLarge)
                        .foregroundStyle(Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255))
                        .minimumScaleFactor(0.6)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                    Text("expense_expenses_label")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                    Text(formatCurrency(dashboard.totalExpenses))
                        .font(.dreamTitleLarge)
                        .foregroundStyle(Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255))
                        .minimumScaleFactor(0.6)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(.horizontal, DreamSpacing.md)
        .padding(.vertical, DreamSpacing.sm)
    }

    private func formatCurrency(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        return formatter.string(from: NSNumber(value: value)) ?? "$0.00"
    }
}
