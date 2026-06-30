import XCTest
@testable import iosApp

final class RecurringFormStateTests: XCTestCase {
    func testIsValidReturnsFalseForEmptyAmount() {
        let formState = RecurringFormState()
        formState.amountText = ""
        XCTAssertFalse(formState.isValid)
    }

    func testIsValidReturnsFalseForZeroAmount() {
        let formState = RecurringFormState()
        formState.amountText = "0"
        XCTAssertFalse(formState.isValid)
    }

    func testIsValidReturnsFalseForNegativeAmount() {
        let formState = RecurringFormState()
        formState.amountText = "-100"
        XCTAssertFalse(formState.isValid)
    }

    func testIsValidReturnsFalseForNonNumericAmount() {
        let formState = RecurringFormState()
        formState.amountText = "abc"
        XCTAssertFalse(formState.isValid)
    }

    func testIsValidReturnsTrueForValidAmount() {
        let formState = RecurringFormState()
        formState.amountText = "500"
        XCTAssertTrue(formState.isValid)
    }

    func testIsEditModeReturnsFalseByDefault() {
        let formState = RecurringFormState()
        XCTAssertFalse(formState.isEditMode)
    }

    func testIsEditModeReturnsTrueWhenTemplateIdSet() {
        let formState = RecurringFormState()
        formState.templateId = "some-id"
        XCTAssertTrue(formState.isEditMode)
    }

    func testResetClearsAllFields() {
        let formState = RecurringFormState()
        formState.templateId = "test-id"
        formState.amountText = "500"
        formState.selectedType = .income
        formState.selectedCategory = .rent
        formState.selectedFrequency = .weekly
        formState.noteText = "Test note"
        formState.isSaving = true
        formState.amountError = true
        formState.hasEndDate = true
        formState.endDate = Date()

        formState.reset()

        XCTAssertNil(formState.templateId)
        XCTAssertEqual(formState.amountText, "")
        XCTAssertEqual(formState.selectedType, .expense)
        XCTAssertEqual(formState.selectedCategory, .other)
        XCTAssertEqual(formState.selectedFrequency, .monthly)
        XCTAssertEqual(formState.noteText, "")
        XCTAssertFalse(formState.isSaving)
        XCTAssertFalse(formState.amountError)
        XCTAssertFalse(formState.hasEndDate)
        XCTAssertNil(formState.endDate)
    }

    func testPopulateFromTemplateItem() {
        let formState = RecurringFormState()
        let template = makeRecurringTemplateItem(
            id: "test-template",
            amount: 250.50,
            type: .income,
            category: .salary,
            frequency: .weekly,
            startDateMillis: 1_720_000_000_000,
            endDateMillis: 1_760_000_000_000
        )

        formState.populate(from: template)

        XCTAssertEqual(formState.templateId, "test-template")
        XCTAssertEqual(formState.selectedType, .income)
        XCTAssertEqual(formState.selectedCategory, .salary)
        XCTAssertEqual(formState.selectedFrequency, .weekly)
        XCTAssertTrue(formState.hasEndDate)
        XCTAssertNotNil(formState.endDate)
    }

    func testValidateSetsAmountErrorForInvalidInput() {
        let formState = RecurringFormState()
        formState.amountText = "abc"

        let result = formState.validate()

        XCTAssertFalse(result)
        XCTAssertTrue(formState.amountError)
    }

    func testValidateClearsAmountErrorForValidInput() {
        let formState = RecurringFormState()
        formState.amountText = "500"
        formState.amountError = true

        let result = formState.validate()

        XCTAssertTrue(result)
        XCTAssertFalse(formState.amountError)
    }

    func testStartDateMillisConvertsCorrectly() {
        let formState = RecurringFormState()
        let date = Date(timeIntervalSince1970: 1_720_000_000)
        formState.startDate = date

        XCTAssertEqual(formState.startDateMillis, 1_720_000_000_000)
    }

    func testEndDateMillisReturnsNilWhenNoEndDate() {
        let formState = RecurringFormState()
        formState.hasEndDate = false
        formState.endDate = nil

        XCTAssertNil(formState.endDateMillis)
    }

    func testEndDateMillisReturnsNilWhenHasEndDateFalse() {
        let formState = RecurringFormState()
        formState.hasEndDate = false
        formState.endDate = Date()

        XCTAssertNil(formState.endDateMillis)
    }

    func testEndDateMillisReturnsValueWhenHasEndDateTrue() {
        let formState = RecurringFormState()
        formState.hasEndDate = true
        let date = Date(timeIntervalSince1970: 1_760_000_000)
        formState.endDate = date

        XCTAssertEqual(formState.endDateMillis, 1_760_000_000_000)
    }
}
