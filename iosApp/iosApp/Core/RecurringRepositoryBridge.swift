import Foundation
import SharedCore

// MARK: - Protocol

protocol RecurringRepositoryBridge {
    func loadTemplates() async throws -> [RecurringTemplateItem]
    func loadTemplateById(id: String) async throws -> RecurringTemplateItem?
    func createTemplate(
        amount: Double,
        type: ExpenseType,
        category: ExpenseCategory,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem

    func updateTemplate(
        id: String,
        amount: Double,
        type: ExpenseType,
        category: ExpenseCategory,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem

    func deleteTemplate(id: String) async throws
    func togglePause(id: String) async throws -> RecurringTemplateItem
    func loadUpcoming(count: Int32) async throws -> [UpcomingRecurringData]
    func processDueRecurring() async throws -> Int32
}

// MARK: - Shared Implementation

final class SharedRecurringRepositoryBridge: RecurringRepositoryBridge {
    let repository: RecurringTemplateRepository

    init() {
        repository = iosRecurringRepository()
    }

    func loadTemplates() async throws -> [RecurringTemplateItem] {
        let kotlinList = try await repository.loadTemplatesOrThrow()
        return kotlinList.map { mapFromKotlin($0) }
    }

    func loadTemplateById(id: String) async throws -> RecurringTemplateItem? {
        guard let kotlinItem = try await repository.loadTemplateByIdOrThrow(id: id) else {
            return nil
        }
        return mapFromKotlin(kotlinItem)
    }

    func createTemplate(
        amount: Double,
        type: ExpenseType,
        category: ExpenseCategory,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem {
        let kotlinEndDate = endDateMillis.map { KotlinLong(value: $0) }
        let kotlinItem = try await repository.createTemplateOrThrow(
            amount: amount,
            type: type.toKotlinType(),
            category: category.toKotlinCategory(),
            note: note,
            frequency: frequency.toKotlinFrequency(),
            startDateMillis: startDateMillis,
            endDateMillis: kotlinEndDate
        )
        return mapFromKotlin(kotlinItem)
    }

    func updateTemplate(
        id: String,
        amount: Double,
        type: ExpenseType,
        category: ExpenseCategory,
        note: String,
        frequency: RecurringFrequencySwift,
        startDateMillis: Int64,
        endDateMillis: Int64?
    ) async throws -> RecurringTemplateItem {
        let kotlinEndDate = endDateMillis.map { KotlinLong(value: $0) }
        let kotlinItem = try await repository.updateTemplateOrThrow(
            id: id,
            amount: amount,
            type: type.toKotlinType(),
            category: category.toKotlinCategory(),
            note: note,
            frequency: frequency.toKotlinFrequency(),
            startDateMillis: startDateMillis,
            endDateMillis: kotlinEndDate
        )
        return mapFromKotlin(kotlinItem)
    }

    func deleteTemplate(id: String) async throws {
        try await repository.deleteTemplateOrThrow(id: id)
    }

    func togglePause(id: String) async throws -> RecurringTemplateItem {
        let kotlinItem = try await repository.togglePauseOrThrow(id: id)
        return mapFromKotlin(kotlinItem)
    }

    func loadUpcoming(count: Int32) async throws -> [UpcomingRecurringData] {
        let kotlinList = try await repository.loadUpcomingOrThrow(count: count)
        return kotlinList.map { mapUpcomingFromKotlin($0) }
    }

    func processDueRecurring() async throws -> Int32 {
        let result = try await repository.processDueRecurringOrThrow()
        return result as! Int32
    }

    // MARK: Mapping

    private func mapFromKotlin(_ template: SharedCore.RecurringTemplate) -> RecurringTemplateItem {
        RecurringTemplateItem(
            id: template.id,
            amount: Double(template.amount),
            type: template.type.asSwiftType,
            category: template.category.asSwiftCategory,
            note: template.note,
            frequency: RecurringFrequencySwift.fromKotlin(template.frequency),
            startDateMillis: template.startDateMillis,
            endDateMillis: template.endDateMillis?.int64Value,
            isPaused: template.isPaused,
            lastGeneratedDateMillis: template.lastGeneratedDateMillis?.int64Value,
            createdAtMillis: template.createdAtMillis,
            updatedAtMillis: template.updatedAtMillis
        )
    }

    private func mapUpcomingFromKotlin(_ upcoming: SharedCore.UpcomingRecurring) -> UpcomingRecurringData {
        UpcomingRecurringData(
            templateId: upcoming.templateId,
            amount: Double(upcoming.amount),
            type: upcoming.type.asSwiftType,
            category: upcoming.category.asSwiftCategory,
            note: upcoming.note,
            frequency: RecurringFrequencySwift.fromKotlin(upcoming.frequency),
            nextDueDateMillis: upcoming.nextDueDateMillis
        )
    }
}
