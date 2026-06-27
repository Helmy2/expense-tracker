import Foundation
@testable import iosApp

final class MockSampleRepositoryBridge: SampleRepositoryBridge {
    var itemsToReturn: [SampleItemSwift] = []
    var itemToReturn: SampleItemSwift?
    var createdItem: SampleItemSwift?
    var updatedItem: SampleItemSwift?
    var loadItemsError: Error?
    var loadItemError: Error?
    var createItemError: Error?
    var updateItemError: Error?
    var createCallCount = 0
    var updateCallCount = 0

    func loadItems() async throws -> [SampleItemSwift] {
        if let loadItemsError {
            throw loadItemsError
        }
        return itemsToReturn
    }

    func loadItem(id: String) async throws -> SampleItemSwift? {
        if let loadItemError {
            throw loadItemError
        }
        return itemToReturn
    }

    func createItem(title: String, description: String, category: SampleCategory) async throws -> SampleItemSwift {
        if let createItemError {
            throw createItemError
        }
        createCallCount += 1
        let item = SampleItemSwift(
            id: createdItem?.id ?? "created-id",
            title: title,
            description: description,
            category: category,
            occurredAtMillis: createdItem?.occurredAtMillis ?? 100,
            updatedAtMillis: createdItem?.updatedAtMillis ?? 100
        )
        createdItem = item
        return item
    }

    func updateItem(_ item: SampleItemSwift) async throws -> SampleItemSwift {
        if let updateItemError {
            throw updateItemError
        }
        updateCallCount += 1
        updatedItem = item
        return item
    }
}

struct MockBridgeError: LocalizedError {
    let errorDescription: String?
}

func makeSampleItem(
    id: String = "item-1",
    title: String = "Sample title",
    description: String = "Sample description",
    category: SampleCategory = .contract,
    occurredAtMillis: Int64 = 100,
    updatedAtMillis: Int64 = 100
) -> SampleItemSwift {
    SampleItemSwift(
        id: id,
        title: title,
        description: description,
        category: category,
        occurredAtMillis: occurredAtMillis,
        updatedAtMillis: updatedAtMillis
    )
}
