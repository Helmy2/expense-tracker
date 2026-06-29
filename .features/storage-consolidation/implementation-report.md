# Implementation Report: storage-consolidation

## Summary

Consolidate the two per-feature Room databases (`TransactionDatabase` on `expense_tracker.db` and `BudgetDatabase` on `budget_tracker.db`) into a single shared `AppDatabase` in `shared/core/data`, following the Dream default per `11-offline-first-storage-practices.md` §11.2. After the change, the `feature/budget/impl` layer no longer depends on `TransactionRepository`, and the cross-feature read becomes a SQL `SUM(...)` aggregate inside `RoomBudgetRepository`.

## Modules Changed

- `shared/core/data` — gained `AppDatabase`, `AppDatabaseFactory`, `AppDatabaseConstructor`, `AndroidAppDatabaseFactory`, `IosAppDatabaseFactory`, `entity/{TransactionEntity,BudgetEntity}`, `dao/{TransactionDao,BudgetDao}`. Schema export directory is now `shared/core/data/schemas/`.
- `feature/expense/data` — slimmed: lost the local Room database and platform factories. Kept the repository, mapper, and tests. New package imports for the moved `TransactionDao` / `TransactionEntity` from `shared/core/data`.
- `feature/expense/data` mapper — restored: `TransactionMapper.kt` lives here, not in `shared/core/data` (per `10-data-layer-practices.md` §10.5: mappers belong in feature data).
- `feature/budget/data` — slimmed: lost the local Room database, platform factories, and schema export. Kept the repository, mapper, and tests. New `TransactionMapper.kt` added (budget data needs to map `TransactionEntity → Transaction` for `BudgetDetail.transactions`; cannot depend on `feature/expense/data`).
- `feature/budget/data` repository — `RoomBudgetRepository` now takes `BudgetDao + TransactionDao + TimeProvider` (no `TransactionRepository` dep). New methods: `loadBudgetsWithSpending()`, `loadBudgetDetail(id)`. Existing CRUD methods preserved.
- `feature/budget/domain` — `BudgetRepository` interface gained `loadBudgetsWithSpending()` and `loadBudgetDetail(id)` methods.
- `feature/budget/impl` — `BudgetViewModel` and `BudgetDetailViewModel` (in `BudgetDetailScreen.kt`) dropped the `TransactionRepository` parameter. Both consume only `BudgetRepository` now. `feature/budget/impl` still depends on `feature/expense/domain` for `TransactionCategory` enum (tracked in `decisions.yaml`).
- `feature/expense/impl` — no code changes (it was already using `TransactionRepository` correctly).
- `shared/core/domain` — `TimeProvider` gained `yearMonthRangeMillis(YearMonth): LongRange` default method for the half-open monthly window the SQL aggregate needs.
- `shared/core/testing` — `FakeTimeProvider` automatically inherits the new default method (no change required).
- `shared/umbrella-core` — `CoreModule.kt` collapsed two DI modules (`expenseDataModule` + `budgetDataModule`) into a single `appDataModule(databaseFactory, appContext)`. iOS `BudgetRepositoryFactory.kt` and `TransactionRepositoryFactory.kt` use the unified `AppDatabase`. `BudgetRepositoryExtensions.kt` gained `loadBudgetsWithSpendingOrThrow()` and `loadBudgetDetailOrThrow(id:)`.
- `shared/umbrella-core/iosMain` — new `BridgeUtils.kt` with `safeOrThrow(operation, block)` helper; all 11 `*OrThrow` extensions (8 on `BudgetRepository` + 3 on `TransactionRepository`) are wrapped in it. New `iosTest` source set with 6 regression tests pinning the contract.
- `shared/umbrella-ui` — `initKoin(...)` signature collapsed to a single `databaseFactory: AppDatabaseFactory` parameter. `MainViewController.kt` (iOS) passes a single `IosAppDatabaseFactory()`.
- `androidApp` — `MainActivity.kt` passes a single `AndroidAppDatabaseFactory(applicationContext)`.
- `iosApp/Core` — `BudgetRepositoryBridge.swift` gained `loadBudgetsWithSpending()` and `loadBudgetDetail(id:)` protocol methods + `mapBudgetWithSpendingFromKotlin` / `mapTransactionFromKotlin` helpers. `TransactionRepositoryBridge.swift` unchanged.
- `iosApp/Budget/ViewModels` — `BudgetListViewModel` and `BudgetDetailViewModel` dropped the `transactionBridge` parameter. Compute-spending logic was removed (now done in Kotlin via SQL aggregate).
- `iosApp/Budget/Models` — new `BudgetDetailData.swift` mirroring Kotlin `BudgetDetail`. `BudgetWithSpendingData.swift` gained `Equatable` (was already `Identifiable`). The `Equatable` synthesis chain works because `BudgetItem: Hashable`, `ExpenseItem: Hashable`, `ExpenseType: String` (auto-`Equatable`), `ExpenseCategory: String` (auto-`Equatable`), and `BudgetStatus: Equatable`.
- `iosApp/iosApp.xcodeproj/project.pbxproj` — `BudgetDetailData.swift` registered via `scripts/sync-pbxproj.rb`. Single-file diff.
- `iosApp/iosAppTests` — `MockBudgetRepositoryBridge.swift` gained `budgetsWithSpendingToReturn` + `budgetDetailToReturn` fields and the two new mock methods. `BudgetListViewModelTests.swift` and `BudgetDetailViewModelTests.swift` dropped the `MockTransactionRepositoryBridge` parameter.
- `iosApp/Localizable.xcstrings` — Xcode auto-extracted `budget_category_label` and a `%@ of %@` placeholder during the build/test run.

## Contract Mapping

| item | type | status | code location | test coverage | notes |
| --- | --- | --- | --- | --- | --- |
| Single `AppDatabase` with `transactions` + `budgets` tables | entity | implemented | `shared/core/data/.../database/AppDatabase.kt` | `AppDatabase_Impl` schema export in `shared/core/data/schemas/` | `@Database(entities = [TransactionEntity::class, BudgetEntity::class], version = 1, exportSchema = true)` |
| `transactions` table schema unchanged | entity-field | implemented | KSP-generated `AppDatabase_Impl.kt` createSQL | schema JSON `1.json` in `shared/core/data/schemas/` | Identical columns to old `TransactionDatabase.1.json` |
| `budgets` table schema unchanged | entity-field | implemented | KSP-generated `AppDatabase_Impl.kt` createSQL | schema JSON `1.json` in `shared/core/data/schemas/` | Identical columns to old `BudgetDatabase.1.json` |
| `TransactionDao.sumExpenseForCategory(category, startMillis, endMillis)` | entity | implemented | `shared/core/data/.../dao/TransactionDao.kt` | `RoomBudgetRepositoryTest` via `FakeTransactionDao` (matches SQL behavior in-memory) | `COALESCE(SUM(amount), 0.0)` over `type = 'EXPENSE'` |
| `RoomBudgetRepository.loadBudgetsWithSpending()` | action | implemented | `feature/budget/data/.../RoomBudgetRepository.kt` | `loadBudgetsWithSpending*` cases in `RoomBudgetRepositoryTest` (4 cases) | Uses `sumExpenseForCategory` per budget |
| `RoomBudgetRepository.loadBudgetDetail(id)` | action | implemented | `feature/budget/data/.../RoomBudgetRepository.kt` | `loadBudgetDetail*` cases in `RoomBudgetRepositoryTest` (2 cases) | Private `buildBudgetDetail` helper |
| `BudgetRepository.loadBudgetsWithSpending()` interface | action | implemented | `feature/budget/domain/.../BudgetRepository.kt` | `BudgetViewModelTest` via `FakeBudgetRepository` | Returns `Result<List<BudgetWithSpending>>` |
| `BudgetRepository.loadBudgetDetail(id)` interface | action | implemented | `feature/budget/domain/.../BudgetRepository.kt` | `BudgetDetailViewModelTest` via `FakeBudgetRepository` | Returns `Result<BudgetDetail?>` |
| `TimeProvider.yearMonthRangeMillis(YearMonth)` | helper | implemented | `shared/core/domain/.../TimeProvider.kt` | `TimeProviderTest` (7 new cases) | Returns `LongRange` half-open `[startMillis, endMillisExclusive)` |
| `BudgetViewModel` no longer takes `TransactionRepository` | refactor | implemented | `feature/budget/impl/.../BudgetViewModel.kt` | `BudgetViewModelTest` (19 cases, `FakeTransactionRepository` removed) | Consumes only `BudgetRepository` |
| `BudgetDetailViewModel` no longer takes `TransactionRepository` | refactor | implemented | `feature/budget/impl/.../BudgetDetailScreen.kt` | `BudgetDetailViewModelTest` (5 cases, `FakeTransactionRepository` removed) | Uses `loadBudgetDetail(id)` |
| `feature/budget/data` no longer depends on `feature/expense/domain` | refactor | implemented | `feature/budget/data/build.gradle.kts` | grep check: no `feature.expense.domain` imports in `feature/budget/data/src/commonMain` | `feature/budget/impl` still depends on it for `TransactionCategory` enum |
| iOS `BudgetRepositoryBridge.loadBudgetsWithSpending()` | bridge | implemented | `iosApp/.../Core/BudgetRepositoryBridge.swift` | `BudgetListViewModelTests` (10 cases) | Maps `BudgetWithSpending` to `BudgetWithSpendingData` |
| iOS `BudgetRepositoryBridge.loadBudgetDetail(id:)` | bridge | implemented | `iosApp/.../Core/BudgetRepositoryBridge.swift` | `BudgetDetailViewModelTests` (5 cases) | Maps `BudgetDetail` to `BudgetDetailData` |
| iOS `BudgetDetailData` Swift model | model | implemented | `iosApp/.../Budget/Models/BudgetDetailData.swift` | used by `BudgetDetailViewModel` and tests | Conforms to `Equatable` via `Hashable` propagation through `BudgetWithSpendingData` / `ExpenseItem` |
| iOS `BudgetListViewModel` drops `transactionBridge` | refactor | implemented | `iosApp/.../Budget/ViewModels/BudgetListViewModel.swift` | `BudgetListViewModelTests` | Sorts by `category.displayName` |
| iOS `BudgetDetailViewModel` drops `transactionBridge` | refactor | implemented | `iosApp/.../Budget/ViewModels/BudgetDetailViewModel.swift` | `BudgetDetailViewModelTests` | Treats `nil` as `error("Budget not found")` |
| `safeOrThrow(operation, block)` bridge helper | helper | implemented | `shared/umbrella-core/.../iosMain/.../di/BridgeUtils.kt` | `BudgetRepositoryExtensionsTest` (6 cases in `iosTest`) | Re-throws `CancellationException`; wraps other Throwables in `RuntimeException` with `cause` |
| All 11 `*OrThrow` extensions wrapped in `safeOrThrow` | refactor | implemented | `BudgetRepositoryExtensions.kt` + `TransactionRepositoryExtensions.kt` | same 6 tests + integration with `loadBudgetsWithSpendingOrThrow` / `loadBudgetDetailOrThrow` | Same Kotlin signatures, same SKIE-exported Swift surface |
| Single Koin composition root: `appDataModule(databaseFactory, appContext)` | refactor | implemented | `shared/umbrella-core/.../di/CoreModule.kt` | `umbrellaCore` unit tests + Android integration | Replaces `expenseDataModule` + `budgetDataModule` |
| `initKoin(databaseFactory, appContext, appDeclaration)` signature | refactor | implemented | `shared/umbrella-ui/.../di/UiModule.kt` + `MainViewController.kt` (iOS) + `MainActivity.kt` (Android) | Android emulator + iOS simulator launch | Single database factory argument |
| Android `MainActivity` passes `AndroidAppDatabaseFactory` | refactor | implemented | `androidApp/.../MainActivity.kt` | `maestro test expense/android.yaml` 14/14 pass | Replaces two factory arguments |
| iOS `MainViewController` passes `IosAppDatabaseFactory` | refactor | implemented | `shared/umbrella-ui/.../iosMain/.../MainViewController.kt` | iOS app launches without crash | Replaces `IosTransactionDatabaseFactory` |
| `project.pbxproj` registers `BudgetDetailData.swift` | refactor | implemented | `iosApp/iosApp.xcodeproj/project.pbxproj` (single-file diff) | `xcodebuild build` + `xcodebuild test` succeed | Diff is exactly the new file entry |
| Old per-feature `schemas/` and `local/` directories deleted | cleanup | implemented | `feature/expense/data/{schemas,local,...}` and `feature/budget/data/{schemas,local,...}` deleted | grep check: no `@Entity` in feature data modules | `feature/expense/data/build.gradle.kts` and `feature/budget/data/build.gradle.kts` dropped the Room schema / KSP config |
| `appDataModule` registers `AppDatabase`, `TransactionDao`, `BudgetDao`, both repositories, DataStore, SessionStorage, HttpClientFactory | DI | implemented | `shared/umbrella-core/.../di/CoreModule.kt` | Koin DI graph + integration with platform hosts | One factory, one module |
| Per-feature Room schema exports deleted (1.json files) | cleanup | implemented | `feature/expense/data/schemas/...` and `feature/budget/data/schemas/...` deleted | n/a | Replaced by single `shared/core/data/schemas/` |
| KSP-generated `AppDatabase_Impl` + DAO impls | build | implemented | `shared/core/data/build/generated/ksp/...` | `xcodebuild build` succeeds; iOS Swift header inspection confirms `__sumExpenseForCategory(...)` is exported via SKIE | Standard Room/KSP code generation |
| iOS bridge exception-wrapping contract: non-RuntimeException → RuntimeException with cause | contract | implemented | `shared/umbrella-core/.../BridgeUtils.kt` | `loadBudgetsWithSpendingOrThrow_wrapsNonRuntimeExceptionInRuntimeException`, `_wrapsNullPointerException`, `_wrappedExceptionHasOriginalAsCauseForDiagnostics` (3 of the 6 tests) | Uses `throwable::class.simpleName ?: throwable::class.qualifiedName` (KMP-portable, not `javaClass.simpleName`) |
| iOS bridge preserves `CancellationException` unchanged | contract | implemented | `shared/umbrella-core/.../BridgeUtils.kt` | `loadBudgetsWithSpendingOrThrow_rethrowsCancellationExceptionUnchanged` | structured-concurrency guarantee |
| iOS bridge converts `Result.Failure` to `RuntimeException` | contract | implemented | `BudgetRepositoryExtensions.kt` + `TransactionRepositoryExtensions.kt` | `loadBudgetsWithSpendingOrThrow_convertsResultFailureToRuntimeException` | Same behavior as before; `safeOrThrow` is an outer wrap |
| iOS bridge returns `Result.Success` value unchanged | contract | implemented | `BudgetRepositoryExtensions.kt` + `TransactionRepositoryExtensions.kt` | `loadBudgetsWithSpendingOrThrow_returnsValueOnSuccess` | Happy path |
| Decision recorded: shared Room database is the default | decision | implemented | `.features/decisions.yaml` (`storage.strategy: shared-room-database`) | reviewed by `dream-inspector` | Per `11-offline-first-storage-practices.md` §11.2 |
| Decision recorded: `TransactionCategory` enum reuse acceptable | decision | implemented | `.features/decisions.yaml` (`crossFeatureReuse` entry) | reviewed by `dream-inspector` | Per `32-feature-boundary-and-reuse-practices.md` §32.2 |

## Partial Items

- **Destructive-fallback migration in production.** `RoomDatabaseBuilder.kt` uses `fallbackToDestructiveMigration(dropAllTables = true)`. This is acceptable for the current iteration (the app hasn't shipped to users yet), but a real Room migration is required before the next release. Recorded in `.features/verification-report.md` as a follow-up.
- **iOS-side `BudgetWithSpendingData` is `Equatable` but not `Hashable`.** Synthesis chose `Equatable` only because that's what the contract asked for. If a future feature needs `Hashable` (e.g. for `Set<BudgetWithSpendingData>`), add it. Not blocking.
- **Per-detail `loadBudgetDetail` filters contributing transactions in Kotlin** (not in SQL). The contract allowed this; a SQL `WHERE IN` would be a follow-up if the per-detail list grows.

## Blocked Items

- **(none in scope) Pre-existing `maestro/features/budget/android.yaml` failure** was already failing on `main` before this refactor (verified by `git stash` + replay on commit `d6670d7`). Recorded as `pre-existing defect: not in scope` in `.features/verification-report.md`. Not a blocker for this refactor.
- **(none in scope) Pre-existing `maestro/features/expense/ios.yaml` hardcoded date** (`Food.*27 Jun 2026`) fails today (29 Jun 2026). Pre-existing date-staleness; the form, SQL insert, and list render all work. Recorded as `pre-existing defect: not in scope`.

## Notes

- **`data` is a Kotlin soft keyword.** The KSP-generated `AppDatabase_Impl.kt` is in package `com.expense.tracker.shared.core.\`data\`.database` (backticks), while the hand-written source files are in `com.expense.tracker.shared.core.data.database` (no backticks). Kotlin treats them as the same identifier at the bytecode level. The SKIE Swift header was inspected and all expected symbols (`SharedCoreTransactionDao`, `SharedCoreBudgetDao`, `SharedCoreAppDatabase`, `SharedCoreBudgetRepositoryExtensionsKt`, `SharedCoreTransactionRepositoryExtensionsKt`, the new `__sumExpenseForCategory(...)` method, etc.) are present. A package rename to `coredata` or `database` is a follow-up for source readability, but is not required.
- **`scripts/sync-pbxproj.rb` writes a `project.pbxproj.bak` file** alongside the real pbxproj. The `.bak` file is a build artifact, not source, and is not staged for commit. Recommend adding it to `.gitignore` as a follow-up.
- **Mappers were moved twice.** Round 1 put `TransactionMapper.kt` and `BudgetMapper.kt` in `shared/core/data/mapper/`, but this broke `10-data-layer-practices.md` §10.5 (mappers belong in feature data). Round 2 moved them back to `feature/expense/data/.../mapper/` and `feature/budget/data/.../mapper/`. The round-1 mistake was caught by a `compileAndroidMain` failure, not a code review. The lesson: when consolidating storage, mappers must move with their feature data layer.
- **`feature/budget/data` got a second `TransactionMapper.kt`.** Budget data cannot depend on `feature/expense/data`, but it still needs to map `TransactionEntity → Transaction` for `BudgetDetail.transactions`. A second local mapper (5 lines) is the cleanest fix per §10.5. A future refactor could extract a shared `shared/core/data/mapper/TransactionMapper.kt` only if a third feature needs the same mapping.
- **Pre-existing on-device data was incompatible with the new schema.** The old app wrote two files (`expense_tracker.db` + `budget_tracker.db`); the new app writes one file (`expense_tracker.db` with two tables). The destructive-fallback migration could not reconcile the on-disk state cleanly, causing a SIGABRT on the iOS test bundle. Uninstalling the app and reinstalling fixed it. This is a one-time per-device issue and is not a production bug for new installs.

## Verification

See `.features/verification-report.md`. Round 5 result: `validated`.
