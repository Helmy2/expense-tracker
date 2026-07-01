import XCTest
@testable import iosApp

final class RecurringListViewModelTests: XCTestCase {
    func testLoadSetsContentStateWhenTemplatesExist() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.templatesToReturn = [
            makeRecurringTemplateItem(id: "1", amount: 1500, type: .expense, category: ExpenseCategory.rent.rawValue, frequency: .monthly),
            makeRecurringTemplateItem(id: "2", amount: 45, type: .expense, category: ExpenseCategory.entertainment.rawValue, frequency: .monthly),
        ]
        let viewModel = RecurringListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        if case .content(let items) = viewModel.contentState {
            XCTAssertEqual(items.count, 2)
        } else {
            XCTFail("Expected content state")
        }
    }

    func testLoadSetsEmptyStateWhenNoTemplates() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.templatesToReturn = []
        let viewModel = RecurringListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        if case .empty = viewModel.contentState {
            // expected
        } else {
            XCTFail("Expected empty state")
        }
    }

    func testLoadSetsErrorStateWhenBridgeThrows() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.loadTemplatesError = MockBridgeError(errorDescription: "Database unavailable")
        let viewModel = RecurringListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        if case .error(let message) = viewModel.contentState {
            XCTAssertEqual(message, "Database unavailable")
        } else {
            XCTFail("Expected error state")
        }
    }

    func testTogglePauseCallsBridge() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.templatesToReturn = [
            makeRecurringTemplateItem(id: "1", amount: 100, isPaused: false)
        ]
        let viewModel = RecurringListViewModel(bridge: bridge)

        await viewModel.load(force: true)
        await viewModel.togglePause(id: "1")

        XCTAssertEqual(bridge.togglePauseCallCount, 1)
        XCTAssertEqual(bridge.lastToggledId, "1")
    }

    func testDeleteConfirmationRemovesTemplate() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.templatesToReturn = [
            makeRecurringTemplateItem(id: "to-delete", amount: 100)
        ]
        let viewModel = RecurringListViewModel(bridge: bridge)

        await viewModel.load(force: true)

        viewModel.pendingDeleteId = "to-delete"
        viewModel.showingDeleteConfirmation = true
        await viewModel.confirmDelete()

        XCTAssertEqual(bridge.deleteCallCount, 1)
        XCTAssertEqual(bridge.lastDeletedId, "to-delete")
        XCTAssertFalse(viewModel.showingDeleteConfirmation)
    }

    func testStartCreateResetsFormState() async {
        let bridge = MockRecurringRepositoryBridge()
        let viewModel = RecurringListViewModel(bridge: bridge)

        viewModel.editingTemplateId = "some-id"
        viewModel.startCreate()

        XCTAssertNil(viewModel.editingTemplateId)
        XCTAssertTrue(viewModel.showFormView)
    }

    func testStartEditSetsEditingTemplateId() async {
        let bridge = MockRecurringRepositoryBridge()
        let viewModel = RecurringListViewModel(bridge: bridge)

        viewModel.startEdit(templateId: "edit-1")

        XCTAssertEqual(viewModel.editingTemplateId, "edit-1")
        XCTAssertTrue(viewModel.showFormView)
    }

    func testLoadSkipsWhenAlreadyLoadedWithoutForce() async {
        let bridge = MockRecurringRepositoryBridge()
        bridge.templatesToReturn = [
            makeRecurringTemplateItem(id: "1", amount: 100)
        ]
        let viewModel = RecurringListViewModel(bridge: bridge)

        // First load
        await viewModel.load(force: true)

        // Set up error for second load (should not be called since force=false)
        bridge.loadTemplatesError = MockBridgeError(errorDescription: "Should not be called")

        // Second load without force
        await viewModel.load(force: false)

        // Should still have content
        if case .content(let items) = viewModel.contentState {
            XCTAssertEqual(items.count, 1)
        } else {
            XCTFail("Expected content state")
        }
    }
}
