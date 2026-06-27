# Test Plan — Expense Tracker

**Feature:** expense
**Date:** 2026-06-27

## Domain Layer Tests

| Test | File | What It Verifies |
|------|------|------------------|
| Transaction entity creation | `TransactionTest.kt` | Field preservation, empty note default, type variants |
| Dashboard summary computation | `DashboardSummaryTest.kt` | Empty list, income-only, expenses-only, mixed totals |
| Category enum completeness | `TransactionCategoryTest.kt` | All 10 categories exist |

## Data Layer Tests

| Test | File | What It Verifies |
|------|------|------------------|
| Entity-to-domain mapper | `TransactionMapperTest.kt` | Roundtrip mapping preserves all fields |
| Domain-to-entity mapper | `TransactionMapperTest.kt` | Type/category enum name serialization |
| Repository with fake DAO | `RoomTransactionRepositoryTest.kt` | Load, add, delete, failure propagation |

## Presentation Layer Tests

| Test | File | What It Verifies |
|------|------|------------------|
| Initial loading state | `ExpenseViewModelTest.kt` | ViewModel starts in Loading state |
| Empty state transition | `ExpenseViewModelTest.kt` | Load with empty list → Empty content state |
| Content state transition | `ExpenseViewModelTest.kt` | Load with transactions → Content state |
| Dashboard recomputation | `ExpenseViewModelTest.kt` | Dashboard totals update after add/delete |
| Save with valid input | `ExpenseViewModelTest.kt` | Amount > 0, category selected → persist |
| Save with invalid amount | `ExpenseViewModelTest.kt` | Amount ≤ 0 → no persist, error |
| Save with non-numeric amount | `ExpenseViewModelTest.kt` | Non-numeric string → no persist |
| Delete with confirmation | `ExpenseViewModelTest.kt` | Delete action → remove transaction |
| Delete cancelled | `ExpenseViewModelTest.kt` | Cancel delete → transaction retained |
| Type selection | `ExpenseViewModelTest.kt` | Segmented button updates state |
| Category selection | `ExpenseViewModelTest.kt` | Dropdown selection updates state, closes menu |
| Amount text update | `ExpenseViewModelTest.kt` | Text input updates state |
| Note text update | `ExpenseViewModelTest.kt` | Text input updates state |
| Error state on failure | `ExpenseViewModelTest.kt` | Repository error → Error content state |
| Save event emission | `ExpenseViewModelTest.kt` | Successful save → TransactionSaved event |

## iOS Tests

| Test | File | What It Verifies |
|------|------|------------------|
| Load content state | `ExpenseListViewModelTests.swift` | Bridge returns items → content state |
| Load empty state | `ExpenseListViewModelTests.swift` | Bridge returns empty → empty state |
| Load error state | `ExpenseListViewModelTests.swift` | Bridge throws → error state |
| Save success | `ExpenseListViewModelTests.swift` | Save adds and reloads |
| Save error | `ExpenseListViewModelTests.swift` | Save failure → error state |
| Delete confirmation | `ExpenseListViewModelTests.swift` | Delete removes transaction |
| Dashboard computation | `ExpenseListViewModelTests.swift` | Totals computed correctly |

## Total Coverage

- Domain: 3 tests
- Data: 2 tests
- Presentation (Android): 15 tests
- Presentation (iOS): 7 tests
- **Total: 27 tests**
