# Bottom Navigation — UI/UX Specification

## Overview

This spec defines the persistent bottom navigation bar for the expense-tracker app, enabling quick switching between three top-level destinations: **Expenses**, **Budgets**, and **Recurring**. Each tab maintains its own back stack, and the bottom bar hides when detail/form screens are pushed on top.

---

## 1. Screen: AppShellWithBottomNav

### Purpose

The app shell wraps all navigation content and provides the persistent bottom navigation bar. It replaces the current `AppShell` which only renders `AppNavigation()` in a bare scaffold.

### View States

| State | Trigger | User Sees |
|-------|---------|-----------|
| **TabRoot** | User is on any tab's root screen (ExpenseScreen, BudgetScreen, RecurringListScreen) | Tab content with bottom bar visible |
| **DetailPushed** | User navigates to a detail or form screen (BudgetDetailScreen, BudgetFormScreen, RecurringFormScreen) | Full-screen detail/form with bottom bar hidden |

### Layout Structure

```
AppTheme
 └─ Surface (fillMaxSize)
     └─ Box (fillMaxSize + background)
         ├─ Column (weight=1f)
         │   └─ NavDisplay (current tab's back stack)
         └─ AnimatedVisibility(visible = !isDetailVisible, enter = slideInVertically, exit = slideOutVertically)
             └─ NavigationBar (design-system component)
                 ├─ Tab: Expenses (icon: receipt_long)
                 ├─ Tab: Budgets (icon: account_balance_wallet)
                 └─ Tab: Recurring (icon: schedule)
```

### Component Hierarchy

| Component | Source | Purpose |
|-----------|--------|---------|
| `AppTheme` | `shared/designsystem` | Dream theme wrapper |
| `Surface` | Material3 (via AppTheme) | Surface color background |
| `NavigationBar` | `shared/designsystem/components/Navigation.kt` | Persistent bottom nav bar |
| `NavDisplay` | Navigation3 | Renders current tab's back stack |

### Design Tokens

| Token | Value | Role |
|-------|-------|------|
| `MaterialTheme.colorScheme.surface` | `DreamSurfaceLight` / `DreamSurfaceDark` | Bottom bar background |
| `MaterialTheme.colorScheme.onSurface` | `DreamOnSurfaceLight` / `DreamOnSurfaceDark` | Unselected tab text/icon |
| `MaterialTheme.colorScheme.primary` | `DreamPrimaryLight` / `DreamPrimaryDark` | Selected tab icon/text |
| `MaterialTheme.colorScheme.primaryContainer` | — | Selected tab indicator |
| `MaterialTheme.colorScheme.onSurfaceVariant` | `DreamOnSurfaceVariantLight` / `DreamOnSurfaceVariantDark` | Unselected tab icon/text |
| `MaterialTheme.colorScheme.background` | `DreamBackgroundLight` / `DreamBackgroundDark` | Screen background |

### User Interactions

| Interaction | Behavior |
|-------------|----------|
| **Tap tab** | Switches to that tab's root screen; resets to root if already on that tab |
| **System back (Android)** | Pops current tab's back stack; if at root, does nothing |
| **Swipe gesture** | Not implemented (tabs only, no swipe pager) |

### Scroll Behavior

- Bottom bar is fixed (not scroll-aware)
- Content scrolls independently within each tab's NavDisplay
- Bottom bar does not collapse on scroll

---

## 2. Tab Bar Layout

### Tabs

| Index | Label | Icon | Route | Screen |
|-------|-------|------|-------|--------|
| 0 | Expenses | `receipt_long` | `ExpenseRoute` | `ExpenseScreen` |
| 1 | Budgets | `account_balance_wallet` | `BudgetRoute` | `BudgetScreen` |
| 2 | Recurring | `schedule` | `RecurringListRoute` | `RecurringListScreen` |

### Icon Behavior

- **Unselected**: Outlined/filled variant in `onSurfaceVariant` color
- **Selected**: Filled variant in `primary` color with `primaryContainer` indicator

### Component Spec

Use existing `NavigationBar` from `shared/designsystem/components/Navigation.kt`:

```kotlin
NavigationBar(
    destinations = listOf(
        NavigationDestination(
            label = stringResource(Res.string.nav_expenses),
            icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) },
            selectedIcon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) }
        ),
        NavigationDestination(
            label = stringResource(Res.string.nav_budgets),
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            selectedIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) }
        ),
        NavigationDestination(
            label = stringResource(Res.string.nav_recurring),
            icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
            selectedIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
        )
    ),
    selectedDestination = state.selectedTabIndex,
    onDestinationSelected = { index -> viewModel.onAction(BottomNavAction.SelectTab(index)) }
)
```

### Animation

- Bottom bar slides down when `isDetailVisible` becomes `true`
- Bottom bar slides up when returning from detail/form
- Use `AnimatedVisibility` with `slideInVertically` / `slideOutVertically`
- Duration: 200ms (standard Material3 motion)

---

## 3. Detail/Form Push Behavior

### Screens That Hide Bottom Bar

| Screen | Route | Tab |
|--------|-------|-----|
| `BudgetDetailScreen` | `BudgetDetailRoute(budgetId)` | Budgets |
| `BudgetFormScreen` | `BudgetFormRoute(budgetId?)` | Budgets |
| `RecurringFormScreen` | `RecurringFormRoute(templateId?)` | Recurring |

### Screens That Keep Bottom Bar Visible

| Screen | Route | Tab |
|--------|-------|-----|
| `ExpenseScreen` | `ExpenseRoute` | Expenses (root) |
| `BudgetScreen` | `BudgetRoute` | Budgets (root) |
| `RecurringListScreen` | `RecurringListRoute` | Recurring (root) |

### Push/Pop Behavior

1. **Push**: When a detail/form screen is pushed onto the current tab's back stack, `isDetailVisible` becomes `true` → bottom bar hides
2. **Pop**: When returning to the tab root, `isDetailVisible` becomes `false` → bottom bar shows
3. **Tab switch while detail visible**: If user switches tabs while a detail is visible, the new tab shows its root (bottom bar visible) since each tab has its own stack

### Navigation3 Integration

The `BottomNavNavigator` manages per-tab back stacks:

```
BottomNavNavigator
 ├─ backStacks: Map<String, SnapshotStateList<NavKey>>
 │   ├─ "expenses" → [ExpenseRoute]
 │   ├─ "budgets" → [BudgetRoute, BudgetDetailRoute("123")]
 │   └─ "recurring" → [RecurringListRoute, RecurringFormRoute(null)]
 ├─ selectedTabIndex: Int (0, 1, or 2)
 ├─ isDetailVisible: Boolean (derived from current stack depth > 1)
 └─ currentBackStack: SnapshotStateList<NavKey> (active tab's stack)
```

The `NavDisplay` in `AppNavigation` uses `navigator.currentBackStack` instead of a single flat back stack.

---

## 4. Android Implementation

### BottomNavNavigator

**Location**: `shared/navigation/src/commonMain/kotlin/com/expense/tracker/shared/navigation/BottomNavNavigator.kt`

```kotlin
// Conceptual structure — not code, just the contract
class BottomNavNavigator(
    private val tabs: List<TabDefinition>
) {
    val selectedTabIndex: MutableState<Int>
    val isDetailVisible: MutableState<Boolean>
    val currentBackStack: SnapshotStateList<NavKey>

    fun selectTab(index: Int)        // Switch tab, reset to root
    fun goTo(destination: NavKey)     // Push onto current tab's stack
    fun goBack()                      // Pop current tab's stack
}
```

**State derivation**:
- `isDetailVisible` is derived: `currentBackStack.size > 1`
- When `selectTab` is called, `currentBackStack` switches to the target tab's stack
- `goTo` adds to the current tab's stack and triggers `isDetailVisible = true`
- `goBack` removes from the current tab's stack; if stack returns to size 1, `isDetailVisible = false`

### Navigation3 Integration

**AppNavigation.kt** changes:
- Replace `koinInject<Navigator>()` with `koinInject<BottomNavNavigator>()`
- `NavDisplay` uses `navigator.currentBackStack` as its back stack
- `onBack` calls `navigator.goBack()`

**Koin wiring** (in `UiModule.kt`):
- Replace `single { Navigator(startDestination = ExpenseRoute) }` with `single { BottomNavNavigator(tabs = listOf(...)) }`
- Remove `startDestination` from navigator module (tabs manage their own roots)

### ExpenseScreen Changes

**Remove** from `ExpenseScreen` top bar actions:
- `Button(text = stringResource(Res.string.recurring_nav_button), ...)` — the "Recurring" action button
- `Button(text = stringResource(Res.string.expense_budgets_button), ...)` — the "Budgets" action button

**Keep**:
- `IconButton(onClick = { viewModel.onAction(ExpenseAction.ToggleFormSheet) })` — the add FAB/icon
- `TopAppBar` title: `stringResource(Res.string.expense_title)`

**Remove** from `ExpenseScreen` parameters:
- `onNavigateToBudgets: () -> Unit` — no longer needed (tab handles it)
- `onNavigateToRecurring: () -> Unit` — no longer needed (tab handles it)

**Remove** from `ExpenseContent`:
- `onNavigateToRecurringList` parameter — no longer needed
- Any call to `onNavigateToRecurring` from within ExpenseContent

### AppShell Changes

**Before** (current):
```kotlin
@Composable
fun AppShell() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            ) {
                AppNavigation()
            }
        }
    }
}
```

**After** (new):
```kotlin
@Composable
fun AppShell() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Content area — weight=1f fills remaining space
                    Box(modifier = Modifier.weight(1f).safeDrawingPadding()) {
                        AppNavigation()
                    }
                    // Bottom bar — rendered by AppNavigation or AppShellWithBottomNav
                }
            }
        }
    }
}
```

**Key change**: `safeDrawingPadding()` moves from the outer Box to the content Box only, so the bottom bar renders edge-to-edge (respecting navigation bar insets via the NavigationBar component itself).

### Bottom Bar Visibility Animation

Use `AnimatedVisibility` in `AppShell` or `AppNavigation`:

```kotlin
AnimatedVisibility(
    visible = !navigator.isDetailVisible,
    enter = slideInVertically(initialOffsetY = { it }),
    exit = slideOutVertically(targetOffsetY = { it })
) {
    // NavigationBar component
}
```

---

## 5. iOS Implementation

### SwiftUI TabView

**Location**: `iosApp/iosApp/ContentView.swift`

**Before** (current):
```swift
struct ContentView: View {
    var body: some View {
        ExpenseListView()
    }
}
```

**After** (new):
```swift
struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            ExpenseListView()
                .tabItem {
                    Label("Expenses", systemImage: "receipt.long")
                }
                .tag(0)

            BudgetListView()
                .tabItem {
                    Label("Budgets", systemImage: "wallet.pass")
                }
                .tag(1)

            RecurringListView()
                .tabItem {
                    Label("Recurring", systemImage: "clock")
                }
                .tag(2)
        }
    }
}
```

### iOS Detail/Form Navigation

Each tab root view embeds its own `NavigationStack`:

```swift
// BudgetListView.swift
struct BudgetListView: View {
    var body: some View {
        NavigationStack {
            // Budget list content
            // NavigationLink to BudgetDetailView / BudgetFormView
        }
    }
}
```

When a detail/form view is pushed within a `NavigationStack`, SwiftUI automatically hides the `tabItem` bar. This is the default SwiftUI behavior — no custom animation needed.

### iOS Tab Selection Persistence

SwiftUI `TabView` with `@State` selection automatically preserves each tab's navigation state when switching tabs, matching the Android per-tab back stack behavior.

### iOS Edge Cases

- **Tab switching**: SwiftUI preserves `NavigationStack` state per tab automatically
- **Back button**: SwiftUI's `NavigationStack` handles back navigation natively
- **Orientation changes**: `TabView` adapts to size class changes automatically

---

## 6. String Resources

### New Strings to Add

**File**: `shared/core/strings/src/commonMain/composeResources/values/strings.xml`

```xml
<!-- Bottom Navigation -->
<string name="nav_expenses">Expenses</string>
<string name="nav_budgets">Budgets</string>
<string name="nav_recurring">Recurring</string>
```

### Strings to Remove

**File**: `shared/core/strings/src/commonMain/composeResources/values/strings.xml`

```xml
<!-- REMOVE these — no longer needed in top bar -->
<!-- <string name="expense_budgets_button">Budgets</string> -->
<!-- <string name="recurring_nav_button">Recurring</string> -->
```

### Resource Key References (Generated)

After Compose resource generation, these become:
- `Res.string.nav_expenses`
- `Res.string.nav_budgets`
- `Res.string.nav_recurring`

---

## 7. ExpenseScreen Changes

### Remove from Top Bar Actions

| Action | Current | After |
|--------|---------|-------|
| "Budgets" button | `Button(text = stringResource(Res.string.expense_budgets_button), onClick = onNavigateToBudgets, variant = ButtonVariant.Tertiary)` | **Removed** |
| "Recurring" button | `Button(text = stringResource(Res.string.recurring_nav_button), onClick = onNavigateToRecurring, variant = ButtonVariant.Tertiary)` | **Removed** |
| Add icon button | `IconButton(onClick = { viewModel.onAction(ExpenseAction.ToggleFormSheet) })` | **Kept** |

### Remove from Composable Parameters

| Parameter | After |
|-----------|-------|
| `onNavigateToBudgets: () -> Unit` | **Removed** |
| `onNavigateToRecurring: () -> Unit` | **Removed** |
| `onNavigateToRecurringEdit: (String) -> Unit` | **Removed** |
| `onNavigateBack: () -> Unit` | **Removed** (Expense is root tab, no back) |

### ExpenseContent Changes

Remove `onNavigateToRecurringList` and `onNavigateToRecurringEdit` parameters from `ExpenseContent` composable. Any internal navigation to recurring screens from within expense list should be removed (users navigate via the Recurring tab instead).

---

## 8. AppShell Changes

### Structural Change

The `AppShell` wraps the navigation content and bottom bar in a `Column` layout:

| Layer | Responsibility |
|-------|---------------|
| `AppTheme` | Dream theme tokens |
| `Surface` | Surface color background |
| `Box` | Fill parent, background color |
| `Column` | Vertical split: content + bottom bar |
| `Box(weight=1f)` | Content area with `safeDrawingPadding` |
| `AnimatedVisibility` | Bottom bar show/hide |
| `NavigationBar` | Design-system bottom nav component |

### safeDrawingPadding Migration

**Before**: `safeDrawingPadding()` on the outer Box → content + future bottom bar both inset
**After**: `safeDrawingPadding()` on the content Box only → bottom bar renders edge-to-edge, content respects system bars

### Koin Module Changes

**File**: `shared/umbrella-ui/src/commonMain/kotlin/com/expense/tracker/shared/di/UiModule.kt`

```kotlin
// Before
val navigatorModule = module {
    single { Navigator(startDestination = ExpenseRoute) }
}

// After
val navigatorModule = module {
    single {
        BottomNavNavigator(
            tabs = listOf(
                TabDefinition(id = "expenses", rootRoute = ExpenseRoute, labelRes = Res.string.nav_expenses),
                TabDefinition(id = "budgets", rootRoute = BudgetRoute, labelRes = Res.string.nav_budgets),
                TabDefinition(id = "recurring", rootRoute = RecurringListRoute, labelRes = Res.string.nav_recurring)
            )
        )
    }
}
```

---

## 9. Edge Cases

### Back Button Behavior (Android)

| Scenario | Behavior |
|----------|----------|
| On tab root, press back | Do nothing (app stays on current tab root) |
| On detail screen, press back | Pop detail → return to tab root → bottom bar shows |
| On tab root of non-default tab, press back | Switch to Expenses tab (first tab) or do nothing — **design decision: do nothing, stay on current tab** |
| On Expenses tab root, press back | Do nothing (Expenses is the default/first tab) |

### Deep Linking

| Scenario | Behavior |
|----------|----------|
| Deep link to ExpenseRoute | Select Expenses tab (index 0) |
| Deep link to BudgetRoute | Select Budgets tab (index 1) |
| Deep link to BudgetDetailRoute("123") | Select Budgets tab, push BudgetDetailRoute onto its stack → bottom bar hidden |
| Deep link to RecurringListRoute | Select Recurring tab (index 2) |
| Deep link to RecurringFormRoute("456") | Select Recurring tab, push RecurringFormRoute onto its stack → bottom bar hidden |

**Implementation**: Deep link handler resolves the route to a tab + optional push, then calls `selectTab(tabIndex)` followed by `goTo(pushRoute)` if needed.

### Orientation Changes

| Platform | Behavior |
|----------|----------|
| Android (rotation) | Per-tab back stacks preserved via `SnapshotStateList` in ViewModel scope; bottom bar stays visible at root, hidden at detail |
| iOS (rotation) | `TabView` + `NavigationStack` state preserved automatically by SwiftUI |

### Multiple Rapid Tab Switches

- Each tab's back stack is independent; switching tabs does not clear any stack
- Bottom bar visibility updates immediately on tab switch (new tab's root → visible)
- No animation conflict: `AnimatedVisibility` only reacts to `isDetailVisible`, not tab index

### Bottom Bar During Form Submission

- Bottom bar remains hidden while a form is pushed
- After successful save, form pops → bottom bar shows
- After cancel, form pops → bottom bar shows

### Memory Considerations

- Three back stacks are held in memory simultaneously
- Each stack contains at most 2-3 entries (root + 1-2 detail/form screens)
- Negligible memory footprint for this use case

---

## 10. Summary of Files to Modify

| Module | File | Change |
|--------|------|--------|
| `shared/navigation` | `BottomNavNavigator.kt` | **New file** — per-tab back stack navigator |
| `shared/navigation` | `AppNavigation.kt` | Use `BottomNavNavigator` instead of `Navigator` |
| `shared/navigation` | `AppShell.kt` | Add Column layout + AnimatedVisibility for bottom bar |
| `shared/navigation` | `Navigator.kt` | Keep for backward compat or remove if unused |
| `shared/core/strings` | `strings.xml` | Add `nav_expenses`, `nav_budgets`, `nav_recurring`; optionally remove button strings |
| `shared/umbrella-ui` | `UiModule.kt` | Replace `Navigator` with `BottomNavNavigator` in Koin module |
| `feature/expense/impl` | `ExpenseScreen.kt` | Remove Budgets/Recurring action buttons and parameters |
| `feature/expense/impl` | `ExpenseContent.kt` | Remove recurring navigation parameters |
| `iosApp` | `ContentView.swift` | Replace `ExpenseListView()` with `TabView` containing three tabs |
| `iosApp` | `BudgetListView.swift` | Wrap in `NavigationStack` for detail push |
| `iosApp` | `RecurringListView.swift` | Wrap in `NavigationStack` for form push |

---

## 11. Design System Component Usage

| Component | Location | Used For |
|-----------|----------|----------|
| `NavigationBar` | `shared/designsystem/components/Navigation.kt` | Bottom nav bar with 3 tabs |
| `NavigationDestination` | `shared/designsystem/components/Navigation.kt` | Tab definition (label + icon) |
| `TopAppBar` | `shared/designsystem/components/AppBar.kt` | Screen top bars (unchanged) |
| `Scaffold` | Material3 | Screen scaffolding (unchanged) |
| `FloatingActionButton` | `shared/designsystem/components/FloatingActionButton.kt` | Add actions (unchanged) |

---

*Spec authored by `dream-designer`. Implementation should follow this spec without deviation unless a blocker is documented.*
