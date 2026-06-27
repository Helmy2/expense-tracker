import XCTest
@testable import iosApp

final class SampleListViewModelTests: XCTestCase {
    func testLoadSetsContentStateWhenBridgeReturnsItems() async {
        let bridge = MockSampleRepositoryBridge()
        bridge.itemsToReturn = [makeSampleItem()]
        let viewModel = SampleListViewModel(bridge: bridge, nowMillis: { 500 })

        await viewModel.load(force: true)

        switch viewModel.contentState {
        case .content(let items):
            XCTAssertEqual(items.count, 1)
            XCTAssertEqual(items.first?.title, "Sample title")
        default:
            XCTFail("Expected content state")
        }
    }

    func testLoadSetsErrorStateWhenBridgeThrows() async {
        let bridge = MockSampleRepositoryBridge()
        bridge.loadItemsError = MockBridgeError(errorDescription: "Bridge failed")
        let viewModel = SampleListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        switch viewModel.contentState {
        case .error(let message):
            XCTAssertEqual(message, "Bridge failed")
        default:
            XCTFail("Expected error state")
        }
    }

    func testStartCreateResetsFormAndShowsSheet() {
        let bridge = MockSampleRepositoryBridge()
        let viewModel = SampleListViewModel(bridge: bridge)
        viewModel.formState.title = "Existing"
        viewModel.formState.description = "Existing description"

        viewModel.startCreate()

        XCTAssertTrue(viewModel.showForm)
        XCTAssertEqual(viewModel.formState.title, "")
        XCTAssertEqual(viewModel.formState.description, "")
    }

    func testSaveItemCreatesNewItemAndClosesSheet() async {
        let bridge = MockSampleRepositoryBridge()
        bridge.createdItem = makeSampleItem(id: "created-id", title: "Created", description: "Created description")
        let viewModel = SampleListViewModel(bridge: bridge, nowMillis: { 500 })
        viewModel.startCreate()
        viewModel.formState.title = "Created"
        viewModel.formState.description = "Created description"

        await viewModel.saveItem()

        XCTAssertEqual(bridge.createCallCount, 1)
        XCTAssertFalse(viewModel.showForm)
    }
}
