# Recurring Transactions — Retrospective

## Date: 2026-06-30

## What Went Well

1. **Contract-first workflow**: Producing the contract before implementation
   gave the implementation subagents a clear source of truth, and the
   contract caught ambiguity in the original request (e.g., what to do
   for paused templates when computing next due date, what fields the
   upcoming-dashboard model needs).
2. **Parallel implementation**: Phase 1 (domain/data), the UI spec, and
   the Android/iOS UIs ran concurrently. Both UI implementations built
   against the same contract and produced complementary code that
   integrated with the existing expense and budget features.
3. **Architectural consistency**: The new feature followed the
   existing `domain/data/api/impl` split and reused
   `TransactionCategory`/`TransactionType` value objects from
   `feature/expense/domain` without breaking the [dep-*] rules.
4. **Composability with existing patterns**: The form/list/ViewModel
   shapes mirror the budget feature closely, which made the
   implementation faster and the code more reviewable.

## What Was Hard

1. **Cross-feature dashboard integration** required careful
   orchestration. The first implementation had
   `feature/expense/impl` import UI types from
   `feature/recurring-transactions/impl` — a [dep-7] violation that the
   inspector caught. Refactoring `UpcomingRecurringUi` to be defined
   locally in `expense/impl` and adding a private mapper resolved it.
2. **iOS processDueRecurring integration**: the iOS native app uses a
   bridge pattern, not the Compose host. The `processDueRecurring()`
   call had to be added in *both* the Compose path (DreamApp) and the
   native SwiftUI path (ExpenseListView .task). The iOS bridge also
   needed a new `processDueRecurring()` method and a
   `processDueRecurringOrThrow()` Kotlin extension, plus a
   corresponding `Int32` cast for the SKIE `KotlinInt` return type.
3. **The "See All" string** ended up as a hardcoded literal in
   `ExpenseContent.kt` initially because the dashboard section was
   added in a hurry. A dedicated string resource
   (`recurring_see_all`) had to be added.

## Defects Encountered

| # | Severity | Description | Resolution |
|---|----------|-------------|------------|
| 1 | CRITICAL | expense/impl → recurring/impl (violates [dep-7]) | Local `UpcomingRecurringUi`, private mapper |
| 2 | MAJOR | `processDueRecurring()` never called at app start | LaunchedEffect in DreamApp + .task in ExpenseListView |
| 3 | MINOR | Hardcoded "See All" in dashboard section | Added `recurring_see_all` resource |
| 4 | MINOR | Dead `ClearStartDate` action in form | Removed unused action and handler |
| 5 | MINOR | Misleading `recurring_budgets_button` name | Renamed to `recurring_nav_button` |

All five were addressed in the second iteration before final review.

## Process Improvements

1. **Dependency-rule check before phase 2**: Run a quick
   `grep "feature.recurringTransactions.impl"` over the
   `feature/expense/impl` module to confirm no cross-impl imports
   before delegating UI work. This is the kind of rule that is easy
   to miss when an aggregator exception is documented in the
   contract — both implementers (Android and iOS) may reach for the
   same convenient wrong path.
2. **Startup integration checklist**: Every feature that defines a
   "called at app startup" action in its contract should be wired
   into the *root composable* (DreamApp) and any *native entry point*
   (iOSApp / ContentView) before review. A `next time` item: add a
   pre-deliverable check that explicitly greps for the call.
3. **String-resource-first**: When adding a new screen, list all the
   user-visible strings in `strings.xml` *before* writing the
   composable. A `string-resource-first` rule would have caught the
   "See All" defect.

## Files That Could Be Reused for Future Features

- `feature/recurring-transactions/api` demonstrates a clean
  `NavKey` route module (single file, two routes).
- `feature/recurring-transactions/impl` demonstrates the
  `Screen + Content + State + Action + Event + ViewModel` split
  with MVI and the `viewModel<T>()` Koin pattern.
- The `UpcomingRecurringUi` mapping pattern in `ExpenseViewModel`
  shows how a dashboard aggregator can consume another feature's
  domain types without depending on its `impl`.

## Notes for Future

- The `TransactionDao` cross-feature reuse documented in
  `decisions.yaml` is a deliberate MVP exception. If a third
  feature needs to write transactions, we should extract the
  transaction-write path into a shared `shared/core/data/...`
  helper or move the auto-generation responsibility out of the
  recurring repository into a dedicated scheduler.
- The `processDueRecurring()` call is best-effort — if the call
  fails, the app continues normally. A future iteration should add
  a per-template lock or a queued retry if multi-process
  coordination is ever needed.
