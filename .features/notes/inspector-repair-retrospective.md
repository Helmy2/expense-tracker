# Repair Retrospective — Inspector Findings Resolution

## Context

- Project: expense-tracker
- Work: Resolving 11 inspector findings from `dream-inspector` review
- Date: 2026-06-30

## What Worked Well

- **Parallel implementer execution**: Running `dream-implementer` (shared core) and `dream-implementer-android` (UI layer) in parallel saved significant time — shared changes (AppError extension, formatAmount utility) were ready when UI changes were applied
- **Precise UI model design**: Creating `DashboardSummaryUi` with pre-formatted strings eliminated the need for mapper calls in composables entirely — clean separation of formatting (in ViewModel) from presentation (in composable)
- **Incremental test updates**: Tests adapted smoothly because the test helpers (FakeRepository, FakeTimeProvider) remained unchanged
- **Koin injection pattern**: Registering mappers as `factory` in Koin modules and injecting into ViewModels followed the existing pattern cleanly

## What Did Not Work Well

- **UI model enums**: `TransactionCategory` and `BudgetStatus` enums remain in UI models because `asLabel()` is a `@Composable` function using `stringResource` — can't call it from a pure Kotlin mapper class. This is acceptable since enums are value types with no business logic.
- **Mapper consolidation deferred**: Moving `TransactionMapper` to `shared/core/data/mapper/` requires module dependency changes that violate `Presentation -> Domain <- Data`. The duplication remains an acceptable cost of modularity.
- **Maestro budget Android flow**: The coordinate-based tapping approach is fragile across devices. The flow's `Tap on point (50%,35%)` doesn't reliably open the Compose dropdown menu across different screen sizes.

## What Was Easy

- Removing dead navigation code (`navigation<BudgetFormRoute>` block) — single deletion, no behavioral impact
- Extracting `AppError.asMessageText()` to shared domain — pure function, no dependencies
- Removing `SystemTimeProvider` from composables — all mappers already had Koin factory registration
- Removing placeholder file — single deletion
- Removing dead `ConfirmDelete`/`CancelDelete` actions — pattern was already unused

## What Was Hard

- **UI model refactoring**: The largest change — creating UI models, adding mapping methods, updating 6+ files per feature. Required careful tracing of every domain type usage through ViewModels → composables.
- **Composable type propagation**: Changing `TransactionItem(transaction: Transaction, mapper: ...)` to `TransactionItem(transaction: ExpenseTransactionUi)` required updating 3 layers (screen → content → list → item) per feature.

## Specific Things Learned

### Reliable Patterns

- UI models with pre-formatted strings eliminate mapper dependencies from composables entirely
- Keeping enums (`TransactionCategory`, `BudgetStatus`) in UI models is acceptable — they're value types with no business logic
- Mapping in ViewModel's `load()` method keeps the domain→UI boundary explicit

### Architecture Lessons

- The `Presentation -> Domain <- Data` rule constrains where mappers can live — `shared/core/data` can't depend on feature domain types
- `TransactionCategory.asLabel()` being `@Composable` (uses `stringResource`) creates a natural boundary: enums pass through UI models, but labels are resolved in composables

## Recommended Improvements

### High Priority
- Make Maestro Android flows use text-based selectors instead of coordinate-based tapping for Compose dropdown menus

### Low Priority
- Consider moving `Transaction` domain model to `shared/core/domain/` if another feature needs it, which would then allow consolidated mappers in `shared/core/data/mapper/`

## Bottom Line

The inspector-driven repair workflow (discover → plan → parallel implement → verify) worked well. The most impactful fix was the UI model refactoring, which eliminated domain types from UI state entirely. The two cancelled items have clear architectural reasons and are safe to defer.
