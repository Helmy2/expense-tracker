import SwiftUI

struct DreamSegmentedPicker<SelectionValue: Hashable, Content: View>: View {
    @Binding var selection: SelectionValue
    @ViewBuilder let content: () -> Content

    @Environment(\.dreamColors) private var colors

    var body: some View {
        Picker(selection: $selection) {
            content()
        } label: {
            EmptyView()
        }
            .pickerStyle(.segmented)
            .tint(colors.primary)
    }
}
