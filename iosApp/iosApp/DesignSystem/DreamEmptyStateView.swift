import SwiftUI

struct DreamEmptyStateView: View {
    let title: LocalizedStringKey
    let message: LocalizedStringKey

    var body: some View {
        ContentUnavailableView(
            title,
            systemImage: "tray",
            description: Text(message)
        )
    }
}
