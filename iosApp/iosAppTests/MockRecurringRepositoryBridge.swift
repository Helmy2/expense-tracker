import Foundation
@testable import iosApp

final class MockRecurringRepositoryBridge: RecurringRepositoryBridge {
    var templatesToReturn: [RecurringTemplateItem] = []
    var templateToReturn: RecurringTemplateItem?
    var upcomingToReturn: [UpcomingRecurringData] = []
    var loadTemplatesError: Error?
    var loadTemplateByIdError: Error?
    var createTemplateError: Error?
    var updateTemplateError: Error?
    var deleteTemplateError: Error?
    var togglePauseError: Error?
    var loadUpcomingError: Error?
    var createCallCount = 0
    var updateCallCount = 0
    var deleteCallCount = 0
    var togglePauseCallCount = 0
    var lastDeletedId: String?
    var lastToggledId: String?
    var lastCreated: (amount: Double, type: ExpenseType, category: String, note: String, frequency: RecurringFrequencySwift)?
    var lastUpdated: (id: String, amount: Double, type: ExpenseType, category: String, note: String, frequency: RecurringFrequencySwift)?

    func loadTemplates() async throws -> [RecurringTemplateItem] {
        if let error = loadTemplatesError { throw error }
        return templatesToReturn
    }

    func loadTemplateById(id: String) async throws -> RecurringTemplateItem? {
        if let error = loadTemplateByIdError { throw error }
        return templateToReturn
    }

    func createTemplate(
        amount: Double,
        type: ExpenseType,
        category: String,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem {
        createCallCount += 1
        if let error = createTemplateError { throw error }
        lastCreated = (amount, type, category, note, frequency)
        if let template = templateToReturn { return template }
        return makeRecurringTemplateItem(
            id: "new-template-id",
            amount: amount,
            type: type,
            category: category,
            frequency: frequency
        )
    }

    func updateTemplate(
        id: String,
        amount: Double,
        type: ExpenseType,
        category: String,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem {
        updateCallCount += 1
        if let error = updateTemplateError { throw error }
        lastUpdated = (id, amount, type, category, note, frequency)
        return makeRecurringTemplateItem(
            id: id,
            amount: amount,
            type: type,
            category: category,
            frequency: frequency
        )
    }

    func deleteTemplate(id: String) async throws {
        deleteCallCount += 1
        lastDeletedId = id
        if let error = deleteTemplateError { throw error }
    }

    func togglePause(id: String) async throws -> RecurringTemplateItem {
        togglePauseCallCount += 1
        lastToggledId = id
        if let error = togglePauseError { throw error }
        let item = templatesToReturn.first { $0.id == id } ?? templateToReturn
        if let item {
            return makeRecurringTemplateItem(
                id: item.id,
                amount: item.amount,
                type: item.type,
                category: item.category,
                frequency: item.frequency,
                isPaused: !item.isPaused
            )
        }
        return makeRecurringTemplateItem(id: id)
    }

    func loadUpcoming(count: Int32) async throws -> [UpcomingRecurringData] {
        if let error = loadUpcomingError { throw error }
        return upcomingToReturn
    }

    func processDueRecurring() async throws -> Int32 {
        return 0
    }
}

func makeRecurringTemplateItem(
    id: String = "recurring-1",
    amount: Double = 100.0,
    type: ExpenseType = .expense,
    category: String = ExpenseCategory.food.rawValue,
    note: String = "Template note",
    frequency: RecurringFrequencySwift = .monthly,
    startDateMillis: Int64 = 1_720_000_000_000,
    endDateMillis: Int64? = nil,
    isPaused: Bool = false,
    lastGeneratedDateMillis: Int64? = nil,
    createdAtMillis: Int64 = 1_720_000_000_000,
    updatedAtMillis: Int64 = 1_720_000_000_000
) -> RecurringTemplateItem {
    RecurringTemplateItem(
        id: id,
        amount: amount,
        type: type,
        category: category,
        note: note,
        frequency: frequency,
        startDateMillis: startDateMillis,
        endDateMillis: endDateMillis,
        isPaused: isPaused,
        lastGeneratedDateMillis: lastGeneratedDateMillis,
        createdAtMillis: createdAtMillis,
        updatedAtMillis: updatedAtMillis
    )
}

func makeUpcomingRecurringData(
    templateId: String = "upcoming-1",
    amount: Double = 50.0,
    type: ExpenseType = .expense,
    category: String = ExpenseCategory.entertainment.rawValue,
    note: String = "Netflix",
    frequency: RecurringFrequencySwift = .monthly,
    nextDueDateMillis: Int64 = 1_725_000_000_000
) -> UpcomingRecurringData {
    UpcomingRecurringData(
        templateId: templateId,
        amount: amount,
        type: type,
        category: category,
        note: note,
        frequency: frequency,
        nextDueDateMillis: nextDueDateMillis
    )
}
