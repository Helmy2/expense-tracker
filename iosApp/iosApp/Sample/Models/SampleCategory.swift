import Foundation

enum SampleCategory: String, CaseIterable, Identifiable {
    case contract
    case architecture
    case preview

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .contract: String(localized: "sample_category_contract")
        case .architecture: String(localized: "sample_category_architecture")
        case .preview: String(localized: "sample_category_preview")
        }
    }
}
