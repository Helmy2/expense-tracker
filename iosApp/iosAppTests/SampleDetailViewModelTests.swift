import XCTest
@testable import iosApp

final class SampleDetailViewModelTests: XCTestCase {
    func testStartEditingCopiesItemIntoFormState() {
        let bridge = MockSampleRepositoryBridge()
        let item = makeSampleItem(title: "Bridge", description: "Copied description", category: .architecture)
        let viewModel = SampleDetailViewModel(item: item, bridge: bridge)

        viewModel.startEditing()

        XCTAssertTrue(viewModel.isEditing)
        XCTAssertEqual(viewModel.formState.id, item.id)
        XCTAssertEqual(viewModel.formState.title, item.title)
        XCTAssertEqual(viewModel.formState.description, item.description)
        XCTAssertEqual(viewModel.formState.category, item.category)
    }

    func testCancelEditingClearsValidationFlags() {
        let bridge = MockSampleRepositoryBridge()
        let item = makeSampleItem()
        let viewModel = SampleDetailViewModel(item: item, bridge: bridge)
        viewModel.isEditing = true
        viewModel.formState.titleError = true
        viewModel.formState.descriptionError = true

        viewModel.cancelEditing()

        XCTAssertFalse(viewModel.isEditing)
        XCTAssertFalse(viewModel.formState.titleError)
        XCTAssertFalse(viewModel.formState.descriptionError)
    }

    func testSaveChangesUpdatesItemAndEndsEditing() async {
        let bridge = MockSampleRepositoryBridge()
        let item = makeSampleItem(updatedAtMillis: 100)
        let viewModel = SampleDetailViewModel(item: item, bridge: bridge, nowMillis: { 999 })
        viewModel.startEditing()
        viewModel.formState.title = "Updated title"
        viewModel.formState.description = "Updated description"
        viewModel.formState.category = .preview

        await viewModel.saveChanges()

        XCTAssertEqual(bridge.updateCallCount, 1)
        XCTAssertEqual(viewModel.item.title, "Updated title")
        XCTAssertEqual(viewModel.item.description, "Updated description")
        XCTAssertEqual(viewModel.item.category, .preview)
        XCTAssertEqual(viewModel.item.updatedAtMillis, 999)
        XCTAssertFalse(viewModel.isEditing)
        XCTAssertFalse(viewModel.formState.isSaving)
    }
}
