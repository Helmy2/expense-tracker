import SwiftUI

struct DreamErrorStateView: View {
    let title: LocalizedStringKey
    let message: String
    let retryLabel: LocalizedStringKey
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: DreamSpacing.md) {
            ContentUnavailableView(
                title,
                systemImage: "exclamationmark.triangle",
                description: Text(message)
            )

            DreamButton(retryLabel, variant: .outlined, action: onRetry)
                .fixedSize()
        }
    }
}
