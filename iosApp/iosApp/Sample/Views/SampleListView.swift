import SwiftUI

struct SampleListView: View {
    @State private var viewModel = SampleListViewModel()
    @Environment(\.dreamColors) private var colors

    var body: some View {
        NavigationStack {
            Group {
                switch viewModel.contentState {
                case .loading:
                    DreamCircularProgressIndicator(size: .large)

                case .empty:
                    DreamEmptyStateView(
                        title: "sample_empty_title",
                        message: "sample_empty_body"
                    )

                case .content(let items):
                    listContent(items)

                case .error(let message):
                    DreamErrorStateView(
                        title: "sample_error_title",
                        message: message,
                        retryLabel: "sample_retry",
                        onRetry: { Task { await viewModel.load(force: true) } }
                    )
                }
            }
            .navigationTitle("sample_title")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button("sample_add", systemImage: "plus") {
                        viewModel.startCreate()
                    }
                }
            }
            .sheet(isPresented: $viewModel.showForm) {
                SampleFormView(
                    formState: viewModel.formState,
                    onSave: { await viewModel.saveItem() },
                    onCancel: { viewModel.showForm = false }
                )
            }
            .navigationDestination(item: $viewModel.selectedItem) { item in
                SampleDetailView(item: item)
            }
            .task { await viewModel.load() }
            .refreshable { await viewModel.load(force: true) }
        }
        .dreamTheme()
    }

    private func listContent(_ items: [SampleItemSwift]) -> some View {
        List {
            Section {
                Text("sample_ios_host_description")
                    .font(.dreamBodyLarge)
                    .foregroundStyle(colors.onSurface)
                Text("sample_ios_instruction")
                    .font(.dreamBodyMedium)
                    .foregroundStyle(colors.primary)
                    .fontWeight(.semibold)
            }

            Section {
                ForEach(items) { item in
                    Button {
                        viewModel.selectItem(item)
                    } label: {
                        VStack(alignment: .leading, spacing: DreamSpacing.xs) {
                            Text(item.displayTitle)
                                .font(.dreamTitleMedium)
                                .foregroundStyle(colors.onSurface)
                            Text(item.displayDescription)
                                .font(.dreamBodyMedium)
                                .foregroundStyle(colors.onSurfaceVariant)
                                .lineLimit(2)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

#Preview {
    SampleListView()
}
