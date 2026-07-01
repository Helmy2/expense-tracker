# Retrospective: Inspector Repair Round

## Date: 2026-07-01

## Findings Addressed

### CRITICAL: Expense `ConfirmDelete`/`CancelDelete` contract mismatch
- **Root cause**: Expense feature used local composable state (`mutableStateOf(false)`) for delete confirmation instead of ViewModel actions as required by contract.
- **Fix**: Added `ConfirmDelete` and `CancelDelete` to `ExpenseAction`, added `deleteTargetId` to `ExpenseState`, updated ViewModel to stage-then-confirm pattern (matching budget), updated UI to use ViewModel state, added tests.
- **Files changed**: `ExpenseAction.kt`, `ExpenseState.kt`, `ExpenseViewModel.kt`, `ExpenseContent.kt`, `ExpenseViewModelTest.kt`
- **Tests added**: `cancelDeleteDoesNotCallRepository`

### MAJOR: Empty `feature/categories/` scaffolding
- **Root cause**: Orphaned directory from initial project setup, never populated with source files.
- **Fix**: Removed `feature/categories/` directory and `.features/categories/` metadata.
- **Verification**: `settings.gradle.kts` had no references; build unaffected.

### MAJOR: Missing Maestro flows for `recurring-transactions`
- **Root cause**: Flows were never created for this feature.
- **Fix**: Created `maestro/features/recurring-transactions/android.yaml` and `ios.yaml` with create-template smoke flows.

## Verification
- `./gradlew allTests`: BUILD SUCCESSFUL (474 tasks)
- `./gradlew :androidApp:assembleDebug`: pending
- Maestro flows: created, not yet run on device

## Lessons
- Always implement delete confirmation as ViewModel actions (not local composable state) for testability and contract compliance.
- The budget feature is the reference implementation for this pattern.
- Orphaned scaffolding should be cleaned up immediately after project instantiation.
