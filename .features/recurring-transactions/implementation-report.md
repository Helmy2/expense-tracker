# Recurring Transactions — Implementation Report

## Feature Summary

The Recurring Transactions feature adds the ability to:
- Create scheduled transaction templates that repeat automatically (daily, weekly, monthly, or yearly)
- Auto-create transactions for due templates on app launch
- List, pause/resume, edit, and delete recurring templates
- Show upcoming recurring transactions on the main expense dashboard

## Architecture Compliance

| Rule | Status |
|------|--------|
| [arch-1] Package by feature | PASSED |
| [arch-2] Split into domain, data, api, impl | PASSED |
| [arch-3] Presentation -> Domain <- Data | PASSED |
| [arch-4] Domain pure Kotlin | PASSED |
| [dep-7] Dashboard depends on domain only | PASSED (after fix) |
| [dom-5] TimeProvider for all time | PASSED |
| [dat-1] Repositories map DTOs to domain | PASSED |
| [nav-2] Routes as typed NavKey | PASSED |
| [di-3] viewModel<T>() syntax | PASSED |
| [res-1] All strings from strings.xml | PASSED (after fix) |
| [test-1] TimeProvider injected in tests | PASSED |

## Files Created

### Shared Core Data
- `shared/core/data/src/commonMain/.../entity/RecurringTemplateEntity.kt`
- `shared/core/data/src/commonMain/.../dao/RecurringTemplateDao.kt`

### Domain (feature/recurring-transactions/domain)
- `RecurringFrequency.kt`
- `RecurringTemplate.kt`
- `UpcomingRecurring.kt`
- `repository/RecurringTemplateRepository.kt`
- `src/commonTest/.../model/RecurringFrequencyTest.kt`
- `src/commonTest/.../model/RecurringTemplateTest.kt`
- `src/commonTest/.../model/UpcomingRecurringTest.kt`
- `src/commonTest/.../repository/RecurringTemplateRepositoryTest.kt`

### Data (feature/recurring-transactions/data)
- `mapper/RecurringTemplateMapper.kt`
- `repository/RoomRecurringTemplateRepository.kt`
- `src/commonTest/.../mapper/RecurringTemplateMapperTest.kt`
- `src/commonTest/.../repository/RoomRecurringTemplateRepositoryTest.kt`

### API (feature/recurring-transactions/api)
- `navigation/RecurringRoute.kt`

### Impl (feature/recurring-transactions/impl)
- `RecurringUiModels.kt`
- `RecurringPresentationMapper.kt`
- `RecurringListState.kt`, `RecurringListAction.kt`, `RecurringListEvent.kt`
- `RecurringListViewModel.kt`
- `RecurringListContent.kt`, `RecurringListScreen.kt`
- `RecurringFormState.kt`, `RecurringFormAction.kt`, `RecurringFormEvent.kt`
- `RecurringFormViewModel.kt`
- `RecurringFormContent.kt`, `RecurringFormScreen.kt`
- `di/RecurringModule.kt`
- Tests: `RecurringPresentationMapperTest.kt`, `RecurringListViewModelTest.kt`, `RecurringFormViewModelTest.kt`

### iOS Bridge (umbrella-core/iosMain)
- `RecurringRepositoryFactory.kt`
- `RecurringRepositoryExtensions.kt`

### iOS App
- `iosApp/iosApp/Core/RecurringRepositoryBridge.swift`
- `iosApp/iosApp/Recurring/Models/RecurringTemplateItem.swift`
- `iosApp/iosApp/Recurring/Models/RecurringFrequencySwift.swift`
- `iosApp/iosApp/Recurring/Models/UpcomingRecurringData.swift`
- `iosApp/iosApp/Recurring/Models/RecurringFormState.swift`
- `iosApp/iosApp/Recurring/ViewModels/RecurringListViewModel.swift`
- `iosApp/iosApp/Recurring/ViewModels/RecurringFormViewModel.swift`
- `iosApp/iosApp/Recurring/Views/RecurringListView.swift`
- `iosApp/iosApp/Recurring/Views/RecurringFormView.swift`
- `iosApp/iosApp/Recurring/Views/UpcomingRecurringSection.swift`
- `iosApp/iosAppTests/MockRecurringRepositoryBridge.swift`
- `iosApp/iosAppTests/RecurringListViewModelTests.swift`
- `iosApp/iosAppTests/RecurringFormStateTests.swift`

## Files Modified

### Shared
- `shared/core/data/.../database/AppDatabase.kt` (added RecurringTemplateEntity, bumped to version 2)
- `shared/umbrella-core/build.gradle.kts` (added recurring deps)
- `shared/umbrella-core/.../di/CoreModule.kt` (registered RecurringTemplateDao and repository)
- `shared/umbrella-ui/build.gradle.kts` (added recurring deps)
- `shared/umbrella-ui/.../di/UiModule.kt` (added recurringUiModule)
- `shared/umbrella-ui/.../app/DreamApp.kt` (added processDueRecurring on launch)
- `shared/core/strings/.../strings.xml` (added 34 recurring string resources)

### Expense
- `feature/expense/impl/build.gradle.kts` (added recurring domain/api deps)
- `feature/expense/impl/.../ExpenseState.kt` (added upcomingRecurring)
- `feature/expense/impl/.../ExpenseAction.kt` (added NavigateToRecurringList)
- `feature/expense/impl/.../ExpenseUiModels.kt` (added local UpcomingRecurringUi)
- `feature/expense/impl/.../ExpenseScreen.kt` (added Recurring nav button)
- `feature/expense/impl/.../ExpenseContent.kt` (added DashboardUpcomingSection)
- `feature/expense/impl/.../ExpenseViewModel.kt` (loads upcoming, maps locally)
- `feature/expense/impl/.../di/ExpenseModule.kt` (wired recurring nav)
- `feature/expense/impl/.../ExpenseViewModelTest.kt` (updated test helper)

### iOS
- `iosApp/iosApp/Core/AppDependencies.swift` (added recurringBridge)
- `iosApp/iosApp/Expense/Views/ExpenseListView.swift` (added upcoming section, recurring nav, startup process)
- `iosApp/iosApp/Localizable.xcstrings` (added 34 recurring string keys)

### Project Config
- `settings.gradle.kts` (added 4 new modules)
- `app-contract.yaml` (added recurring-transactions feature)
- `decisions.yaml` (added recurring_templates table, cross-feature reuse)

## Test Results

| Layer | Tests | Status |
|-------|-------|--------|
| Domain | RecurringFrequency, RecurringTemplate, UpcomingRecurring, Repository contract | PASSED |
| Data | Mapper, RoomRecurringTemplateRepository (CRUD, processDue, loadUpcoming) | PASSED |
| Impl | List VM (4 states, toggle, delete), Form VM (validation, save, edit), Presentation Mapper | PASSED |
| iOS | 7 ListViewModel, 15 FormState | PASSED |

## Issues Found and Fixed

1. CRITICAL: [dep-7] violation (expense/impl depended on recurring/impl) — Fixed by defining UpcomingRecurringUi locally.
2. MAJOR: processDueRecurring never called at startup — Fixed via DreamApp LaunchedEffect and iOS .task.
3. MINOR: Hardcoded "See All" — Fixed via string resource.
4. MINOR: Dead ClearStartDate action — Removed.
5. MINOR: Misleading string name `recurring_budgets_button` — Renamed to `recurring_nav_button`.

## Build Status
- `./gradlew allTests`: PASSED
- `./gradlew :androidApp:assembleDebug`: PASSED
- `xcodebuild build`: PASSED
- `xcodebuild test`: PASSED

## Out of Scope (not implemented in this pass)
- Maestro smoke flows for recurring (deferred — no device/emulator time was spent on these in this iteration)
- iOS .task integration: processDueRecurring is called from the .task in ExpenseListView (in the existing view), but is also wired in DreamApp for the Compose host.
