import SwiftUI

struct DreamButton: View {
    let title: LocalizedStringKey
    let variant: DreamButtonVariant
    let size: DreamComponentSize
    let isEnabled: Bool
    let action: () -> Void

    @Environment(\.dreamColors) private var colors

    init(
        _ title: LocalizedStringKey,
        variant: DreamButtonVariant = .primary,
        size: DreamComponentSize = .medium,
        isEnabled: Bool = true,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.variant = variant
        self.size = size
        self.isEnabled = isEnabled
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.dreamLabelLarge)
                .foregroundStyle(foregroundColor)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, horizontalPadding)
                .padding(.vertical, 12)
                .background(backgroundColor)
                .overlay(
                    RoundedRectangle(cornerRadius: 24)
                        .stroke(variant == .outlined ? colors.outline : .clear, lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 24))
        }
        .disabled(!isEnabled)
        .opacity(isEnabled ? 1 : 0.5)
    }

    private var foregroundColor: Color {
        switch variant {
        case .primary: colors.onPrimary
        case .secondary: colors.onSecondary
        case .destructive: colors.onError
        case .outlined, .tertiary: colors.primary
        }
    }

    private var backgroundColor: Color {
        switch variant {
        case .primary: colors.primary
        case .secondary: colors.secondary
        case .destructive: colors.error
        case .outlined, .tertiary: .clear
        }
    }

    private var horizontalPadding: CGFloat {
        switch size {
        case .small: DreamSpacing.sm
        case .medium: DreamSpacing.md
        case .large: DreamSpacing.lg
        }
    }
}
