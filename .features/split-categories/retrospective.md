# Retrospective: split-categories

## Context

- Project: Dream expense tracker
- Template base: dream-kmp-base
- Work reviewed: Split single TransactionCategory enum into IncomeCategory + ExpenseCategory across all layers
- Date: 2026-07-01

## What Worked Well

- Contract-first approach prevented scope drift — the contract clearly defined the two new enums, their values, and which features use which type
- Phase 1 (domain/data) completed cleanly in one iteration with all tests passing
- Parallel Phase 2 (Android + iOS UI) worked well — both subagents completed independently without conflicts
- The existing String-based Room storage meant no schema migration was needed
- Inspector verified all 10 architecture rules and contract parity in a single pass

## What Did Not Work Well

- The test class name `TransactionCategoryTest.kt` is now stale — should have been renamed during the refactor
- The iOS project path in verification instructions references `iosApp-swiftui/` but the actual path is `iosApp/`

## What Was Easy

- Domain layer refactor was straightforward — enums are value objects with no behavior
- Data layer mappers needed minimal changes since category was already stored as String
- Budget feature was simple to update — just change the enum type

## What Was Hard

- Ensuring category pickers in expense and recurring forms correctly adapt to type required careful coordination between State, ViewModel, and Content layers
- iOS bridge mapping needed updates across three bridge files (Transaction, Budget, Recurring)

## Specific Things Learned

### Reliable Smoke Patterns

- Existing Maestro flows for expense, budget, and recurring features already exercise category selection paths — no dedicated split-categories flows needed
- `./gradlew allTests` + `xcodebuild test` provides comprehensive coverage for this type of refactor

### Architecture Lessons

- Keeping `Transaction.category` as `String` (matching Room storage) was the right call — it avoided schema changes and let the presentation layer handle type resolution
- `Budget.category` as `ExpenseCategory` (strong type) prevents invalid income budgets
- Helper functions (`resolveIncomeCategory()`, `resolveExpenseCategory()`) in the domain layer provide type-safe resolution without polluting the data layer

### Token Optimization Lessons

- Passing the contract content directly to subagents (rather than pointing to files) reduced redundant reads
- Fresh subagent invocations for each phase kept context clean

## Recommended Template Improvements

### High Priority

- Add a checklist item: "Rename test classes when the class under test is renamed"
- Fix iOS verification path in generated instructions: `iosApp/iosApp.xcodeproj` not `iosApp-swiftui/iosApp.xcodeproj`

### Medium Priority

- Consider a domain helper pattern for type-union fields (String stored, resolved by sibling field) as a reusable template pattern
- Add guidance for when to use String vs strong enum for cross-type fields

### Low Priority

- Document the pattern of "enum split across type discriminator" as a common refactor scenario

## Token Optimization Recommendations

### Agent Efficiency

- Continue passing contract content in task prompts rather than file pointers
- Phase 2 parallel execution (Android + iOS) saves significant tokens vs sequential

### Verification Efficiency

- `./gradlew allTests` + `xcodebuild test` is sufficient for enum-refactor verification
- Existing Maestro flows provide adequate smoke coverage for category path changes

### Output Structure

- The verification report template with status buckets worked well for tracking

## Application Status

- Applied: 2026-07-01
- What was updated: split-categories feature completed, all verification passed

## Bottom Line

Splitting a shared enum into type-specific enums is a clean refactor when the underlying storage is String-based. The key insight is keeping the domain model field as String and resolving to the correct enum in the presentation layer, which avoids schema changes and keeps the data layer thin. Contract-first planning and parallel UI implementation made this a smooth multi-platform feature update.
