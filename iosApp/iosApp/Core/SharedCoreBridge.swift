import Foundation
import SharedCore

protocol SampleRepositoryBridge {
    func loadItems() async throws -> [SampleItemSwift]
    func loadItem(id: String) async throws -> SampleItemSwift?
    func createItem(title: String, description: String, category: SampleCategory) async throws -> SampleItemSwift
    func updateItem(_ item: SampleItemSwift) async throws -> SampleItemSwift
}

enum BridgeError: LocalizedError {
    case unknown
    case message(String)

    var errorDescription: String? {
        switch self {
        case .unknown: "Something went wrong."
        case .message(let text): text
        }
    }
}

final class SharedCoreBridge: SampleRepositoryBridge {
    let repository: SampleRepository

    init() {
        repository = iosSampleRepository()
    }

    func loadItems() async throws -> [SampleItemSwift] {
        let kotlinItems = try await repository.loadItemsOrThrow()
        return kotlinItems.map { mapFromKotlin($0) }
    }

    func loadItem(id: String) async throws -> SampleItemSwift? {
        guard let kotlinItem = try await repository.loadItemOrThrow(id: id) else { return nil }
        return mapFromKotlin(kotlinItem)
    }

    func createItem(title: String, description: String, category: SampleCategory) async throws -> SampleItemSwift {
        let kotlinItem = try await repository.createItemOrThrow(
            title: title,
            description: description,
            category: category.toKotlinCategory()
        )
        return mapFromKotlin(kotlinItem)
    }

    func updateItem(_ item: SampleItemSwift) async throws -> SampleItemSwift {
        let kotlinItem = item.toKotlinItem()
        let updated = try await repository.updateItemOrThrow(item: kotlinItem)
        return mapFromKotlin(updated)
    }

    private func mapFromKotlin(_ item: SharedCore.SampleItem) -> SampleItemSwift {
        SampleItemSwift(
            id: item.id,
            title: item.title,
            description: item.description_,
            category: item.category.asSwiftCategory,
            occurredAtMillis: item.occurredAtMillis,
            updatedAtMillis: item.updatedAtMillis
        )
    }
}

extension SampleItemSwift {
    func toKotlinItem() -> SharedCore.SampleItem {
        SharedCore.SampleItem(
            id: id,
            title: title,
            description: description,
            category: category.toKotlinCategory(),
            occurredAtMillis: occurredAtMillis,
            updatedAtMillis: updatedAtMillis
        )
    }
}

extension SampleCategory {
    func toKotlinCategory() -> SharedCore.SampleCategory {
        switch self {
        case .contract: return SharedCore.SampleCategory.contract
        case .architecture: return SharedCore.SampleCategory.architecture
        case .preview: return SharedCore.SampleCategory.preview
        }
    }
}

extension SharedCore.SampleCategory {
    var asSwiftCategory: SampleCategory {
        switch self {
        case .contract: return .contract
        case .architecture: return .architecture
        case .preview: return .preview
        @unknown default: return .contract
        }
    }
}
