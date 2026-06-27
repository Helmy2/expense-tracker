import Foundation

struct SampleItemSwift: Identifiable, Hashable {
    let id: String
    let title: String
    let description: String
    let category: SampleCategory
    let occurredAtMillis: Int64
    let updatedAtMillis: Int64

    var displayTitle: String {
        title
    }

    var displayDescription: String {
        description
    }

    var displayDate: String {
        let date = Date(timeIntervalSince1970: TimeInterval(occurredAtMillis) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }

    init(
        id: String,
        title: String,
        description: String,
        category: SampleCategory,
        occurredAtMillis: Int64,
        updatedAtMillis: Int64
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.category = category
        self.occurredAtMillis = occurredAtMillis
        self.updatedAtMillis = updatedAtMillis
    }
}
