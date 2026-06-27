import SwiftUI

struct DreamTextField: View {
    let placeholder: LocalizedStringKey
    @Binding var text: String
    let isError: Bool
    let errorMessage: LocalizedStringKey?
    let axis: Axis
    let lineLimit: ClosedRange<Int>
    let accessibilityIdentifier: String?

    @Environment(\.dreamColors) private var colors

    init(
        _ placeholder: LocalizedStringKey,
        text: Binding<String>,
        isError: Bool = false,
        errorMessage: LocalizedStringKey? = nil,
        axis: Axis = .horizontal,
        lineLimit: ClosedRange<Int> = 1...1,
        accessibilityIdentifier: String? = nil
    ) {
        self.placeholder = placeholder
        _text = text
        self.isError = isError
        self.errorMessage = errorMessage
        self.axis = axis
        self.lineLimit = lineLimit
        self.accessibilityIdentifier = accessibilityIdentifier
    }

    var body: some View {
        VStack(alignment: .leading, spacing: DreamSpacing.xs) {
            TextField(placeholder, text: $text, axis: axis)
                .textInputAutocapitalization(.sentences)
                .lineLimit(lineLimit)
                .accessibilityIdentifier(accessibilityIdentifier ?? "")
                .padding(DreamSpacing.sm)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(isError ? colors.error : colors.outline, lineWidth: 1)
                )

            if isError, let errorMessage {
                Text(errorMessage)
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.error)
            }
        }
    }
}
