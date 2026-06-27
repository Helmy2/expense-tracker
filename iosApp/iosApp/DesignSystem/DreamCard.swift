import SwiftUI

enum DreamCardVariant {
    case elevated
    case outlined
    case filled
}

struct DreamCard<Content: View>: View {
    let variant: DreamCardVariant
    let action: (() -> Void)?
    @ViewBuilder let content: () -> Content

    @Environment(\.dreamColors) private var colors

    init(
        variant: DreamCardVariant = .elevated,
        action: (() -> Void)? = nil,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.variant = variant
        self.action = action
        self.content = content
    }

    var body: some View {
        Group {
            if let action {
                Button(action: action) {
                    cardContent
                }
                .buttonStyle(.plain)
            } else {
                cardContent
            }
        }
    }

    private var cardContent: some View {
        content()
            .padding(DreamSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(backgroundColor)
            .clipShape(RoundedRectangle(cornerRadius: DreamSpacing.lg))
            .overlay(
                RoundedRectangle(cornerRadius: DreamSpacing.lg)
                    .stroke(variant == .outlined ? colors.outline : .clear, lineWidth: 1)
            )
            .shadow(color: variant == .elevated ? .black.opacity(0.08) : .clear, radius: 4, y: 2)
    }

    private var backgroundColor: Color {
        switch variant {
        case .elevated, .outlined: colors.surface
        case .filled: colors.surfaceVariant
        }
    }
}
