import Foundation
import SharedCore

enum RecurringFrequencySwift: String, CaseIterable, Identifiable {
    case daily = "DAILY"
    case weekly = "WEEKLY"
    case monthly = "MONTHLY"
    case yearly = "YEARLY"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .daily: String(localized: "recurring_frequency_daily")
        case .weekly: String(localized: "recurring_frequency_weekly")
        case .monthly: String(localized: "recurring_frequency_monthly")
        case .yearly: String(localized: "recurring_frequency_yearly")
        }
    }

    static func fromKotlin(_ frequency: SharedCore.RecurringFrequency) -> RecurringFrequencySwift {
        switch frequency {
        case .daily: return .daily
        case .weekly: return .weekly
        case .monthly: return .monthly
        case .yearly: return .yearly
        @unknown default: return .monthly
        }
    }

    func toKotlinFrequency() -> SharedCore.RecurringFrequency {
        switch self {
        case .daily: return .daily
        case .weekly: return .weekly
        case .monthly: return .monthly
        case .yearly: return .yearly
        }
    }
}
