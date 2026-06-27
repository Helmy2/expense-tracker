import SwiftUI

struct DreamCircularProgressIndicator: View {
    let size: DreamComponentSize

    @Environment(\.dreamColors) private var colors

    init(size: DreamComponentSize = .medium) {
        self.size = size
    }

    var body: some View {
        ProgressView()
            .progressViewStyle(CircularProgressViewStyle(tint: colors.primary))
            .scaleEffect(scaleFactor)
    }

    private var scaleFactor: CGFloat {
        switch size {
        case .small: 0.8
        case .medium: 1.0
        case .large: 1.3
        }
    }
}
