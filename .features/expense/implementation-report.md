# Implementation Report — Expense UI Update

## Feature

`expense` — Dashboard layout restructure and input form moved to bottom sheet

## Changes Made

### Presentation Layer (Android — Compose Multiplatform)

**State (`ExpenseState.kt`)**
- Added `showBottomSheet: Boolean = false` field

**Actions (`ExpenseAction.kt`)**
- Added `ToggleFormSheet` — toggles `showBottomSheet`
- Added `DismissFormSheet` — sets `showBottomSheet = false`

**ViewModel (`ExpenseViewModel.kt`)**
- Handles `ToggleFormSheet` (toggle) and `DismissFormSheet` (set false)
- On successful `saveTransaction()`, sets `showBottomSheet = false`

**Screen (`ExpenseScreen.kt`)**
- Added `IconButton` with `Icons.Filled.Add` in `TopAppBar` `actions` slot
- Click dispatches `ExpenseAction.ToggleFormSheet`

**Content (`ExpenseContent.kt`)**
- Removed `ExpenseFormSection` from main content column
- Restructured `DashboardSection`:
  - Total Balance label + value in prominent layout (headlineLarge, bold)
  - Row with "Total Income" (green) and "Total Expenses" (red) below, using `weight(1f)`
  - Removed Card wrapper
- Added `Sheet` (Dream design system) containing `FormSheetContent`:
  - Title "New Transaction", Amount field, Type segmented button, Category dropdown, Note field, Save button

**Tests (`ExpenseViewModelTest.kt`)**
- Added 4 new tests: `toggleFormSheetSetsShowBottomSheetToTrue`, `dismissFormSheetSetsShowBottomSheetToFalse`, `toggleFormSheetTwiceToggles`, `saveTransactionClosesBottomSheet`

### Presentation Layer (iOS — SwiftUI)

**Dashboard View (`ExpenseDashboardView.swift`)**
- Rewrote layout: Balance label + value in large bold text; Income/Expenses in HStack below

**List View (`ExpenseListView.swift`)**
- Removed inline `ExpenseFormView` from empty and content states
- Added `.toolbar` with plus `Image(systemName: "plus")` button
- Added `.sheet(isPresented: $viewModel.showFormSheet)` with `ExpenseFormView`

**ViewModel (`ExpenseListViewModel.swift`)**
- Added `showFormSheet = false` property
- On successful `saveTransaction()`, sets `showFormSheet = false`

**Localization (`Localizable.xcstrings`)**
- Added `expense_balance_label` ("Balance"), `expense_income_label` ("Income"), `expense_expenses_label` ("Expenses"), `expense_ios_add_transaction` ("Add transaction")

**Tests (`ExpenseListViewModelTests.swift`)**
- Added 2 new tests: `testToggleFormSheet`, `testSaveTransactionClosesFormSheet`

### Cleanup

- Removed leftover sample Maestro flows (`maestro/features/sample/`)
- Removed leftover sample Room schema exports (`shared/core/data/schemas/com.expense.tracker.shared.core.data.local.SampleDatabase/` and `com.dream.template...` variant)

## Not Changed

- Domain layer (`feature/expense/domain/`) — no changes
- Data layer (`feature/expense/data/`) — no changes
- API layer (`feature/expense/api/`) — no changes
- Navigation (`shared/navigation/`) — no changes
- DI wiring (`shared/umbrella-core/`, `shared/umbrella-ui/`) — no changes
- Shared strings (`shared/core/strings/`) — no changes
- Maestro flows (`maestro/features/expense/`) — updated for new interaction pattern

## Verification

See `.features/verification-report.md`.
