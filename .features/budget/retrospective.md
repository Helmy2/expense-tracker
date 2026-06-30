# Template Improvement Retrospective — Budgets Feature

## Context

- Project: expense-tracker
- Template base: dream-kmp-base
- Work reviewed: Budgets feature generation (domain, data, api, impl, iOS SwiftUI, wiring)
- Date: 2026-06-27

## What Worked Well

- The parallel Phase 2 delegation (Android Compose + iOS SwiftUI) worked efficiently — both platforms implemented independently from the shared domain/data layer
- The feature contract and UI spec provided clear guidance for all implementers
- Dream design-system components were reused consistently across both platforms
- The Koin compiler plugin DSL for navigation entries pattern worked correctly once the viewModel syntax was fixed
- Room database pattern with expect/actual factory worked for both platforms
- The `sync-pbxproj.rb` script (after path fix) successfully registered all new Swift files

## What Did Not Work Well

- Koin compiler plugin DSL `viewModel { }` lambda syntax is not supported — must use `viewModel<T>()` syntax exclusively
- The `sync-pbxproj.rb` script hardcoded `iosApp-swiftui/` paths instead of `iosApp/` — needed manual fix
- iOS test files placed in subdirectory (`iosAppTests/Budget/`) caused pbxproj registration at wrong path — had to move files to root
- Multiple compilation errors in initial generation: missing string resource imports, wrong icon references, missing kotlinx-datetime dependency
- The `FakeBudgetRepository` and `FakeTransactionRepository` classes created conflicts between test files when both were `private` in different files

## What Was Easy

- Creating the feature contract from the natural-language request
- Generating the UI spec from the contract
- Domain entity and repository interface creation
- Room entity, DAO, and database creation following the expense pattern
- Adding string resources to strings.xml
- Wiring into settings.gradle.kts and umbrella modules

## What Was Hard

- Debugging Koin compiler plugin DSL errors — the error messages were cryptic (referencing MatchGroupCollection)
- Fixing iOS test file path registration in pbxproj
- Resolving test class visibility conflicts between BudgetViewModelTest and BudgetDetailViewModelTest
- The `BudgetDetailViewModel` taking `budgetId` as a constructor parameter didn't work with Koin's `viewModel<T>()` — had to refactor to pass via action

## Specific Things Learned

### Reliable Smoke Patterns

- Build-only verification (Gradle tests + Android APK build + iOS xcodebuild build + iOS xcodebuild test) is a reliable pre-commit gate
- The `sync-pbxproj.rb` script is essential after adding/removing Swift files

### Brittle Smoke Patterns

- Koin compiler plugin DSL error messages are not helpful for debugging — always check the exact function signature
- iOS pbxproj registration doesn't handle subdirectories in test files well

### Architecture Lessons

- `BudgetDetailViewModel` should not take route parameters in the constructor — use action-based parameter passing instead (matches the expense feature pattern)
- Cross-feature dependency (budget depends on expense domain for TransactionCategory) is acceptable but should be documented and eventually extracted to shared domain
- The `FeatureFormRoute` Koin entry should render the same screen as `FeatureRoute` when the form is a BottomSheet overlay, not a separate screen

## Recommended Template Improvements

### High Priority

- Add a note in the Dream operating guide that Koin compiler plugin DSL requires `viewModel<T>()` syntax, not `viewModel { }` lambda syntax
- Fix `sync-pbxproj.rb` to use `iosApp/` paths (or make paths configurable)

### Medium Priority

- Add `kotlinx-datetime` as a default dependency for feature data modules that use time-based filtering
- Document that test helper classes should be `internal` (not `private`) when shared across test files in the same module

### Low Priority

- Extract `TransactionCategory.asLabel()` to a shared composable utility to avoid duplication across feature impl files
- Extract `AppError.asMessageText()` to a shared utility

## Suggested Template Defaults To Add

- A note in the feature generation checklist: "Verify Koin viewModel registration uses `viewModel<T>()` syntax, not lambda"
- A note: "Place test helper classes as `internal` when they may be shared across test files"

## Application Status

- Applied: 2026-06-30
- What was updated: Added `viewModel<T>()` syntax rule to `dream-agent-operating-guide.md` and `new-feature-checklist.md`; added `internal` test helper visibility note to `23-testing-oriented-practices-to-reuse.md`; added 3 anti-patterns (mappers in shared data, viewModel lambda syntax, hardcoded SystemTimeProvider in composables) to `26-anti-patterns-to-avoid-when-reusing-this-architecture.md`. Skipped: `sync-pbxproj.rb` path fix (already uses `iosApp/`), project-specific utility extractions (TransactionCategory.asLabel, AppError.asMessageText).

## Bottom Line

The Budgets feature generation followed the Dream workflow effectively with parallel platform implementation. The main friction points were Koin DSL syntax limitations, iOS pbxproj path handling, and test class visibility. These are template-level improvements that would benefit future feature generations.
