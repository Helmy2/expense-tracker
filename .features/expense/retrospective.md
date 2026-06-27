# Template Improvement Retrospective

## Context

- Project: expense-tracker
- Template base: dream-kmp-base (modular-hybrid)
- Work reviewed: Expense feature UI update — dashboard layout restructure and inline form moved to bottom sheet
- Date: 2026-06-27

## What Worked Well

- **Clean delegation pattern**: dream-designer generated a detailed UI spec from the contract, then dream-implementer-android and dream-implementer-ios worked independently in parallel without conflicts
- **Contract-first approach**: The feature contract and UI spec served as the single source of truth, keeping both platform implementations aligned
- **iOS verification**: dream-implementer-ios ran xcodebuild and xcodebuild test independently, catching build errors early
- **Incremental changes**: No domain/data/api changes were needed — clean separation of concerns allowed targeted UI-only edits
- **SwiftUI sheet integration**: The `.sheet(isPresented:)` modifier integrated cleanly with existing `@Observable` ViewModel pattern

## What Did Not Work Well

- **Leftover sample artifacts**: The sample Maestro flows and Room schema exports were not removed when the sample feature was replaced by the expense feature, requiring cleanup during this feature update
- **SystemTimeProvider hardcoded in composable**: A pre-existing issue where `ExpensePresentationMapper(SystemTimeProvider)` is created directly in `ExpenseContent.kt` instead of injecting `TimeProvider` via DI — not caught during initial implementation

## What Was Easy

- Adding `showBottomSheet` state field and toggle actions — straightforward MVI extension
- Restructuring the `DashboardSection` composable — single-file change
- iOS form sheet integration — `@Bindable` and `.sheet()` worked naturally with existing code

## What Was Hard

- Verifying Android Maestro flows without a booted emulator — environment dependency is a friction point for local development verification
- Ensuring both platforms (Compose Multiplatform and native SwiftUI) produced the same visual outcome from a single UI spec

## Specific Things Learned

### Reliable Smoke Patterns

- iOS Maestro flow with updated interaction pattern (tap plus icon → sheet appears → fill form → save → sheet closes → list updates) worked reliably

### Brittle Smoke Patterns

- Android Maestro verification depends on a booted emulator, which isn't always available in a local dev environment

### Architecture Lessons

- A UI-only feature update that respects `Presentation -> Domain <- Data` means domain and data tests don't need re-running — they're UP-TO-DATE and irrelevant
- The `showBottomSheet` state flag pattern is clean: ViewModel owns the toggle/dismiss, and the Sheet's onDismissRequest dispatches a dedicated action rather than directly mutating state

## Recommended Template Improvements

### High Priority

- Ensure sample artifacts (Maestro flows, Room schemas, sample module) are fully removed when the first real feature replaces the sample

### Medium Priority

- Add a note in the implementation checklist to verify sample artifact removal after feature generation

### Low Priority

- *(none)*

## Suggested Template Defaults To Add

- *(none)*

## Application Status

- Applied: `2026-06-27`
- What was updated:
  - `.opencode/guidance/new-feature-checklist.md` — Added explicit verification steps in Sample Removal section to confirm cleanup of Maestro sample flows (`maestro/features/sample/`), Room schema export directories (`shared/core/data/schemas/`), and settings/build file references.
  - `.opencode/knowledge-graph/best-practices/26-anti-patterns-to-avoid-when-reusing-this-architecture.md` — Broadened anti-pattern #17 to explicitly cover leftover sample artifacts (Room schema exports, Maestro flow directories, sample string keys, stray imports) with a reference to `./scripts/remove-sample.sh`.

## Bottom Line

The contract-first, multi-agent pattern for UI updates works well when the presentation layer is cleanly separated from domain/data. The main friction point was pre-existing sample cleanup debt, not the feature work itself.
