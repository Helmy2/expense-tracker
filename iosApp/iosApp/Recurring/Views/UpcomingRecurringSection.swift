import SwiftUI

struct UpcomingRecurringSection: View {
    let upcomingItems: [UpcomingRecurringData]
    let onSeeAll: () -> Void
    let onItemTap: (String) -> Void
    @Environment(\.dreamColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: DreamSpacing.sm) {
            // Header
            HStack {
                Text("recurring_upcoming_title")
                    .font(.dreamTitleMedium)
                    .foregroundStyle(colors.onSurface)

                Spacer()

                Button(action: onSeeAll) {
                    Text("recurring_ios_see_all")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.primary)
                }
            }

            // Items
            DreamCard(variant: .outlined) {
                VStack(alignment: .leading, spacing: DreamSpacing.sm) {
                    ForEach(upcomingItems.prefix(3)) { item in
                        Button {
                            onItemTap(item.templateId)
                        } label: {
                            UpcomingRowView(item: item)
                        }
                        .buttonStyle(.plain)

                        if item.id != upcomingItems.prefix(3).last?.id {
                            Divider()
                        }
                    }
                }
            }
        }
        .padding(.horizontal, DreamSpacing.md)
        .padding(.vertical, DreamSpacing.sm)
    }
}

// MARK: - Upcoming Row

struct UpcomingRowView: View {
    let item: UpcomingRecurringData
    @Environment(\.dreamColors) private var colors

    var body: some View {
        HStack(spacing: DreamSpacing.sm) {
            // Icon
            Circle()
                .fill(item.type == .income
                    ? Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255).opacity(0.15)
                    : Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255).opacity(0.15))
                .frame(width: 36, height: 36)
                .overlay(
                    Image(systemName: item.type == .income ? "arrow.up" : "arrow.down")
                        .font(.system(size: 14))
                        .foregroundColor(item.type == .income
                            ? Color(red: 0x4C / 255, green: 0xAF / 255, blue: 0x50 / 255)
                            : Color(red: 0xEF / 255, green: 0x53 / 255, blue: 0x50 / 255))
                )

            VStack(alignment: .leading, spacing: 2) {
                Text(item.formattedAmount)
                    .font(.dreamTitleMedium)
                    .foregroundStyle(colors.onSurface)

                Text(item.displayCategoryAndFrequency)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurfaceVariant)
            }

            Spacer()

            // Due date
            VStack(alignment: .trailing, spacing: 2) {
                    Text("recurring_next_due")
                        .font(.dreamBodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                Text(item.displayDueDate)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.onSurface)
            }
        }
        .padding(.vertical, DreamSpacing.xs)
    }
}

#Preview {
    UpcomingRecurringSection(
        upcomingItems: [],
        onSeeAll: {},
        onItemTap: { _ in }
    )
}
