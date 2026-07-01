import SwiftUI

struct TransactionRowView: View {
    let transaction: ExpenseItem
    let onDelete: () -> Void
    @Environment(\.dreamColors) private var colors

    var body: some View {
        HStack(spacing: DreamSpacing.sm) {
            Circle()
                .fill(transaction.type == .income
                    ? Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255).opacity(0.15)
                    : Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255).opacity(0.15))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: transaction.type == .income ? "arrow.up" : "arrow.down")
                        .foregroundColor(transaction.type == .income
                            ? Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255)
                            : Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255))
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

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .font(.system(size: 15))
                    .foregroundStyle(colors.onSurfaceVariant)
                    .padding(10)
                    .background(.regularMaterial, in: Circle())
            }
            .accessibilityLabel(String(localized: "expense_ios_delete_accessibility"))
        }
        .padding(.vertical, DreamSpacing.xs)
    }
}
