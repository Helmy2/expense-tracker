import XCTest
@testable import iosApp

final class BudgetFormStateTests: XCTestCase {
    func testIsFormValidReturnsFalseForEmptyLimit() {
        let formState = BudgetFormState()
        formState.limitText = ""
        XCTAssertFalse(formState.isFormValid)
    }

    func testIsFormValidReturnsFalseForZeroLimit() {
        let formState = BudgetFormState()
        formState.limitText = "0"
        XCTAssertFalse(formState.isFormValid)
    }

    func testIsFormValidReturnsFalseForNegativeLimit() {
        let formState = BudgetFormState()
        formState.limitText = "-100"
        XCTAssertFalse(formState.isFormValid)
    }

    func testIsFormValidReturnsFalseForNonNumericLimit() {
        let formState = BudgetFormState()
        formState.limitText = "abc"
        XCTAssertFalse(formState.isFormValid)
    }

    func testIsFormValidReturnsTrueForValidLimit() {
        let formState = BudgetFormState()
        formState.limitText = "500"
        XCTAssertTrue(formState.isFormValid)
    }

    func testResetClearsAllFields() {
        let formState = BudgetFormState()
        formState.limitText = "500"
        formState.selectedCategory = .food
        formState.isSaving = true
        formState.limitError = true
        formState.categoryMenuExpanded = true

        formState.reset()

        XCTAssertEqual(formState.limitText, "")
        XCTAssertEqual(formState.selectedCategory, .other)
        XCTAssertFalse(formState.isSaving)
        XCTAssertFalse(formState.limitError)
        XCTAssertFalse(formState.categoryMenuExpanded)
    }

    func testPopulateFromBudgetItem() {
        let formState = BudgetFormState()
        let budget = makeBudgetItem(id: "test", category: .entertainment, monthlyLimit: 250)

        formState.populate(from: budget)

        XCTAssertEqual(formState.selectedCategory, .entertainment)
        XCTAssertEqual(formState.limitText, "250.0")
        XCTAssertFalse(formState.isSaving)
        XCTAssertFalse(formState.limitError)
    }

    func testValidateSetsLimitErrorForInvalidInput() {
        let formState = BudgetFormState()
        formState.limitText = "abc"

        let result = formState.validate()

        XCTAssertFalse(result)
        XCTAssertTrue(formState.limitError)
    }

    func testValidateClearsLimitErrorForValidInput() {
        let formState = BudgetFormState()
        formState.limitText = "500"
        formState.limitError = true

        let result = formState.validate()

        XCTAssertTrue(result)
        XCTAssertFalse(formState.limitError)
    }
}
