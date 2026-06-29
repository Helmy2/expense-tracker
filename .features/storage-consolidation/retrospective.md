# Template Improvement Retrospective

## Context

- Project: expense-tracker
- Template base: dream-kmp-base (modular-hybrid)
- Work reviewed: Storage consolidation — replaced two per-feature Room databases with a single shared `AppDatabase` in `shared/core/data`, removed the cross-feature `TransactionRepository` call from the budget data layer, hardened the iOS bridge exception-wrapping contract, and updated the iOS bridge surface to expose the new spending-aware methods.
- Date: 2026-06-29

## What Worked Well

- **Contract-first delegation pattern scaled to a non-trivial refactor.** The `dream-implementer` consumed `.features/storage-consolidation/contract.yaml` as the single source of truth, including target module layout, schema, DAO method, repository surface, iOS bridge changes, and required test coverage. The plan was detailed enough that the implementer did not have to invent architecture decisions during execution.
- **`.features/verification-report.md` as a single iteration-state file.** Each subagent read the previous round's context from this file (failure count, past fix attempts, what to try this round) instead of relying on conversation history. This made fresh subagent invocations cheap and reliable — the context survived across 5 repair rounds.
- **Per-check status buckets (`passed` / `blocked by environment` / `not run` / `not applicable` / `pre-existing defect: not in scope`).** The pre-existing `maestro/features/budget/android.yaml` failure was correctly triaged as `pre-existing defect: not in scope` after a `git stash` + replay on `main` reproduced the same failure. The operating guide's discipline about not blending `not run` into `passed` caught this.
- **Koin compiler plugin DSL + single composition root.** Collapsing `expenseDataModule` + `budgetDataModule` into a single `appDataModule(databaseFactory, appContext)` was straightforward because the Koin compiler DSL doesn't need annotation-based `@KoinViewModel` / `@Module` / `@ComponentScan`. The Koin graph test confirmed all registrations resolve without eager instantiation.
- **iOS bridge extension pattern.** The `*OrThrow` extensions in `shared/umbrella-core/.../iosMain/.../di/` are a thin, well-typed adapter between Kotlin `Result<T>` and Swift `async throws`. Once the pattern was in place, adding `loadBudgetsWithSpendingOrThrow` and `loadBudgetDetailOrThrow` took minutes.
- **`TimeProvider.yearMonthRangeMillis(YearMonth): LongRange` as a default method.** Centralizes the half-open monthly window math. Both `loadBudgetsWithSpending` and `loadBudgetDetail` use the same helper; tests cover edge cases (year boundary, month boundary). No more `Instant`/`toLocalDateTime` arithmetic in feature code.

## What Did Not Work Well

- **The mappers were put in `shared/core/data/mapper/` in round 1** because it felt "natural" to put the entity↔domain mapping next to the entity. This broke `10-data-layer-practices.md` §10.5 ("data layer: DTO <-> entity <-> domain mapping stays in data" — the feature's data layer). The compile error was caught by `compileAndroidMain` (`Unresolved reference 'Transaction'`), not by a code review. The fix was simple (move the mappers back) but it cost a full repair round. **The template should make this rule more visible to subagents that lack conversation history.**
- **Round 3's iOS build-error diagnosis was a false positive.** The `dream-tester` reported `BudgetDetailData: Equatable` synthesis failure on `BudgetWithSpendingData` / `ExpenseItem` / `ExpenseType` / `ExpenseCategory`. The synthesis actually works because `Hashable` refines `Equatable` and `String`-backed enums auto-conform. The tester had a single round of budget to read the model files, and the cross-references were easy to miss. The user surfaced the real crash (a stale on-device database) which led to a re-diagnosis. **A more conservative tester (one that reads related files before flagging a defect) would have avoided a wasted repair round.**
- **The destructive-fallback migration could not reconcile the new schema with the old on-device data.** The old app wrote two files (`expense_tracker.db` and `budget_tracker.db`); the new app writes one file (`expense_tracker.db` with two tables). The fallback dropped the `transactions` table in the old `expense_tracker.db` and re-created it with the new schema, but it did not know about the `budgets` table (which lived in a separate `budget_tracker.db` file). The result: the new `AppDatabase` opened the old `expense_tracker.db` file with the two-table schema, found a `budgets` table that didn't match the expected hash, and aborted. **For the refactor, the workaround is "uninstall the app". For the next release, a real Room migration is mandatory.**
- **Round-4 crash from a stale on-device database was not anticipated by any subagent.** The `dream-tester` (round 3) reported a SIGABRT but did not consider "stale on-device database from before the consolidation" as a hypothesis. The user had to surface it. **The template should add "uninstall the app before installing the new build" as an explicit step in iOS verification.**
- **Cross-round communication relied entirely on `.features/verification-report.md`.** Subagents had to write to and read from this file to coordinate. The protocol works but it requires careful per-round updating. **A standardized round-summary template would help.**

## What Was Easy

- **Moving `TransactionEntity` / `TransactionDao` / `BudgetEntity` / `BudgetDao` from feature data into `shared/core/data`.** Standard `mv` + package rename + import update across the affected modules. The KSP code generation picked up the new locations automatically.
- **Adding a new `@Query` method to an existing DAO.** `sumExpenseForCategory(category, startMillis, endMillis): Double` was 5 lines of Kotlin and a corresponding in-memory filter in `FakeTransactionDao` for the test.
- **Renaming the Koin `initKoin` signature.** Going from two optional factory arguments to a single required factory argument was straightforward because the call sites are only in the platform hosts (`MainActivity.kt` and `MainViewController.kt`).
- **Adding the iOS `BudgetDetailData` Swift model.** 6 lines of code; the conformance chain worked automatically through `Hashable` and `String` raw value.

## What Was Hard

- **Diagnosing the runtime crash.** The thread 1 stack pointed at `processUnhandledException` from `kotlinx.coroutines`, but the root cause was an on-device database that the new schema couldn't open. The stack itself didn't contain enough information to localize the cause to a specific code path. The user had to manually uninstall the app to confirm.
- **Deciding whether to harden the iOS bridge exception wrapping.** SKIE documentation is sparse on which `Throwable` types it converts to Swift `Error` and which it doesn't. The defensive `safeOrThrow` helper is a safe default but it's a workaround for a SKIE behavior we couldn't pin down. **A note in `36-ios-kmp-bridge-practices.md` about SKIE's exception propagation guarantees would help future agents.**
- **Coordinating 5 subagent rounds in one feature** (2 implementer + 1 implementer-ios + 1 implementer + 1 tester + 1 tester + 1 inspector). The `.features/verification-report.md` pattern is the only thing that made this manageable.

## Specific Things Learned

### Reliable Smoke Patterns

- **Maestro iOS `maestro test maestro/features/budget/ios.yaml` (18/18 pass).** The end-to-end budget flow exercises the unified `AppDatabase` through SKIE: create a budget → navigate to detail → verify `Spent US$0.00 / Remaining US$500.00` from the SQL aggregate. This is the strongest evidence the consolidation works.
- **`xcrun simctl uninstall` before `xcrun simctl install` for iOS verification.** Guarantees a clean on-device baseline. Without this step, schema changes from previous installs can pollute the new build's behavior.
- **Defensive `try/catch` on the iOS bridge extension layer.** Wrap every `*OrThrow` in a `safeOrThrow(operation, block)` helper that rethrows `CancellationException` and converts every other `Throwable` into a `RuntimeException(message, cause)`. Gives SKIE a stable exception type to convert to a Swift `Error`. Tested with 6 dedicated cases in `shared/umbrella-core/.../iosTest/`.

### Brittle Smoke Patterns

- **`xcodebuild test` on a simulator that has a stale install from a previous run.** Can SIGABRT at runtime for reasons unrelated to the current build's code. Always `xcrun simctl uninstall` first.
- **`maestro test .../budget/android.yaml` with `(50%, 35%)` tap coordinates** for opening the category dropdown. The form is rendered as a bottom sheet on the lower half of the screen; the (50%, 35%) tap lands on the scrim and dismisses the sheet. This was a pre-existing failure on `main` (verified by `git stash` + replay). Not fixed in this refactor.

### Architecture Lessons

- **Mappers belong in feature data, not shared data.** Per `10-data-layer-practices.md` §10.5. The round-1 mistake of putting `TransactionMapper.kt` and `BudgetMapper.kt` in `shared/core/data/mapper/` cost a repair round. The fix was mechanical but the round was wasted.
- **A second local mapper is preferable to cross-feature data coupling.** When `feature/budget/data` needed to map `TransactionEntity → Transaction` for `BudgetDetail.transactions`, the cleanest fix was a 5-line second `TransactionMapper.kt` in `feature/budget/data/.../mapper/`, not a cross-feature dependency on `feature/expense/data`. Per `06-dependency-rules-to-keep.md` §5.3.
- **One shared Room database is the default, not the exception.** Per `11-offline-first-storage-practices.md` §11.2. Per-feature databases are an exception that must be documented in `.features/decisions.yaml`. The Dream default — one `AppDatabase` in `shared/core/data` with all entities, schema export, and platform factories — is the right starting point for every generated app.
- **The iOS bridge's `*OrThrow` pattern is the right adapter between Kotlin `Result<T>` and Swift `async throws`.** It keeps the Swift side idiomatic and gives a single, testable place to add cross-cutting concerns (logging, telemetry, exception wrapping).
- **Migrations must be planned before user data exists.** Destructive fallback is fine for the first iteration. Once the app ships to users, every schema change needs a real Room migration that preserves existing data.

## Recommended Template Improvements

### High Priority

- **Add a "Bridge exception wrapping" note to `36-ios-kmp-bridge-practices.md`.** SKIE's exception propagation guarantees are not well-documented. The template should explicitly recommend wrapping every `*OrThrow` extension in a defensive `try/catch` that converts `Throwable` to `RuntimeException` with `cause`. This avoids runtime SIGABRTs in production.
- **Add a "Mappers belong in feature data" rule to `10-data-layer-practices.md` §10.5.** The current rule says "data layer: DTO <-> entity <-> domain mapping stays in data" but doesn't make clear that "the data layer" means the feature's data layer, not the shared data layer. Add an explicit anti-example: "Mappers that map `Entity → FeatureDomainModel` belong in `feature/<name>/data/.../mapper/`, not in `shared/core/data/mapper/`. The shared data module owns entities and DAOs, not domain-bound mappers."
- **Add "uninstall before install" to iOS verification steps.** The `dream-agent-operating-guide.md` verification rules should explicitly call out `xcrun simctl uninstall <bundle-id> && xcrun simctl install ...` as the iOS install sequence to avoid stale on-device data.
- **Add a "Schema migration" section to `11-offline-first-storage-practices.md`.** The current file says "use destructive migration fallback for first-pass MVP" but doesn't explain what happens when the schema changes (e.g. from two databases to one). Add a note: "Destructive fallback cannot reconcile a schema split across multiple database files. If the previous app version wrote per-feature databases and the new version writes a shared database, the migration must include a one-time copy of data from the old files into the new one, OR users must uninstall before installing the new version."

### Medium Priority

- **Add a standardized round-summary template to the operating guide.** The current `.features/verification-report.md` is open-ended; a per-round "what was tried, what worked, what didn't, what's next" template would make cross-round coordination more reliable.
- **Add a `git status` check to the pre-implementer step.** The implementer should always run `git status` and `git log --oneline -5` before starting work, to confirm they're on the intended branch and that no unrelated worktree changes are present.
- **Add a note about iOS bundle identifier to the iOS verification commands.** The bundle id is `com.expense.tracker.ios` (not the project name). Document this in the operating guide so the tester doesn't have to grep `Info.plist` each time.

### Low Priority

- **Consider adding `.pbxproj.bak` to `.gitignore`.** The `scripts/sync-pbxproj.rb` script writes a backup of `project.pbxproj` next to the real file. This shows up in `git status` after every sync and is not source. Either add it to `.gitignore` or have the script write the backup to a temp directory.
- **Consider a `dream-improve` step at the end of every substantial refactor.** The current `dream-improve` command scans unapplied retros and applies them to the template. After a refactor this large, the recommendations above are exactly what `dream-improve` would apply. Run `dream-improve` as the final step before commit.

## Suggested Template Defaults To Add

- A reference implementation of `safeOrThrow(operation, block)` for the iOS bridge extensions, in `.opencode/guidance/templates/`.
- A reference `AppDatabase` setup (entities, DAOs, factories, schema export) in `.opencode/guidance/templates/`, so the next generated app can copy it as the starting point.
- A reference `BudgetRepository.loadBudgetsWithSpending()` SQL-aggregate pattern in `.opencode/knowledge-graph/best-practices/10-data-layer-practices.md` or a new `32b-cross-feature-data-reads.md` file.

## Application Status

- Applied: `not yet` (the recommendations above are pending; `dream-improve` is the natural place to apply them).
- What was updated: *(nothing yet — this retro is unapplied)*

## Bottom Line

The contract-first, multi-agent pattern scales to non-trivial cross-feature refactors. The biggest friction was the round-1 mapper placement mistake (cost a repair round) and the round-3 false-positive Equatable diagnosis (cost a wasted iOS-fix round). Both are preventable with sharper template rules. The on-device database incompatibility is a one-time issue for this refactor but a real migration is required before the next release. The iOS bridge hardening (`safeOrThrow`) should be a template default for every generated app.
