# Expense Feature — UI/UX Specification

## Overview

This spec describes the update to the existing single-screen expense tracker. Two structural changes from the current implementation are:

1. **Dashboard redesign** — Replace the three-column side-by-side metric layout with a prominent single-line "Total Balance" and a secondary row of "Total Income" / "Total Expenses".
2. **Form → Bottom sheet** — Move the inline transaction input form into a `ModalBottomSheet` (`Sheet`) triggered by a trailing icon button (plus/add) in the `TopAppBar` `actions` slot.

All other existing behavior (transaction list, delete confirmation dialog, event snackbar) is preserved.

---

## Screen

| Property | Value |
|---|---|
| **Screen name** | `ExpenseScreen` |
| **Route type** | `ExpenseRoute` (in `feature/expense/api`) |
| **Purpose** | Single-screen expense tracker showing a dashboard summary, a scrollable transaction list, and a modal bottom sheet for adding new transactions |

---

## View States

Four mutually exclusive states driven by `ExpenseContentState` (preserved from existing implementation, with updated visuals for Empty and Content):

### 1. Loading

| Aspect | Specification |
|---|---|
| **Trigger** | `ExpenseContentState.Loading` — initial state on screen entry; also shown after `ExpenseAction.Load` before data arrives |
| **What the user sees** | Full-screen centered `CircularProgressIndicator(size = ComponentSize.Large)` on a clean background (`surface`). No dashboard skeleton, no list. Top app bar is visible with title and add icon button |
| **Scrolling** | None |
| **Interactions** | None until loading completes |
| **Component hierarchy** | `ExpenseScreen` → `Scaffold` → `TopAppBar` + `CircularProgressIndicator` (centered) |

### 2. Empty

| Aspect | Specification |
|---|---|
| **Trigger** | `ExpenseContentState.Empty` — `loadTransactions()` returns `Result.Success` with an empty list |
| **What the user sees** | `TopAppBar` + an empty-state `Card(variant = CardVariant.Filled)` with `title`, `body`, and `accentColor = secondary`. Below the card: nothing — no dashboard, no transaction list area. The add icon button in the top-right corner remains visible and functional |
| **Scrolling** | The page is a vertical column with the card at center-top; no scroll needed unless keyboard forces it |
| **Interactions** | Tap the add icon button → opens the form bottom sheet. No transactions exist yet to delete |
| **Component hierarchy** | `ExpenseScreen` → `Scaffold` → `TopAppBar` + `Column` → `Card(Empty)` |

### 3. Content

| Aspect | Specification |
|---|---|
| **Trigger** | `ExpenseContentState.Content(transactions)` — `loadTransactions()` returns a non-empty list |
| **What the user sees** | Three distinct sections in a vertically scrollable column:
1. **Dashboard** (no Card wrapper): `Total Balance` as a large bold headline, then a row with `Total Income` (green) and `Total Expenses` (red)
2. A `Divider` separating dashboard from the transaction list
3. **Transaction list**: section title "Transactions", then a list of `ListItem` rows (amount + category + date + delete icon button)
The add icon button in the top-right corner remains visible and functional |
| **Scrolling** | `verticalScroll(rememberScrollState())` on the entire content column — dashboard scrolls away with the list |
| **Component hierarchy** | `ExpenseScreen` → `Scaffold` → `TopAppBar` + `Column`(scrollable) → `DashboardSection` + `Divider` + `TransactionListSection` |

### 4. Error

| Aspect | Specification |
|---|---|
| **Trigger** | `ExpenseContentState.Error(error)` — `loadTransactions()` returns `Result.Failure` |
| **What the user sees** | `TopAppBar` + an error-state `Card(variant = CardVariant.Filled, accentColor = error)` with `title` (string resource `expense_error_title`), `body` (the error message), and an `actionLabel` button labeled "Retry" (`expense_retry` string). The add icon button in the top-right corner remains visible and functional |
| **Interactions** | Tap "Retry" → dispatches `ExpenseAction.Load`. Tap add icon → opens the form bottom sheet despite error state |
| **Component hierarchy** | `ExpenseScreen` → `Scaffold` → `TopAppBar` + `Column` → `Card(Error)` |

---

## Section Details

### 1. Top App Bar

| Property | Specification |
|---|---|
| **Component** | `TopAppBar` (Dream design system) |
| **Title** | String resource `Res.string.expense_title` ("Expense Tracker") — rendered with `MaterialTheme.typography.titleLarge` |
| **Navigation icon** | `null` — no back button on this screen |
| **Actions slot** | `IconButton` with a plus/add icon (e.g., `Icons.Default.Add`) |
| **Icon behavior** | `onClick` dispatches `ExpenseAction.ToggleFormSheet` (NEW action) |
| **Accessibility** | Content description for icon: "Add transaction" |

### 2. Dashboard Section

| Property | Specification |
|---|---|
| **Context** | No longer wrapped in a `Card`. Rendered as direct child of the scrollable `Column` |
| **Total Balance** | Full-width `Text` with `style = MaterialTheme.typography.headlineLarge`, `fontWeight = FontWeight.Bold`, `color = MaterialTheme.colorScheme.onSurface`. Preceded by a label "Total Balance" (`expense_balance_label`) using `bodyMedium`, `onSurfaceVariant` color. |
| **Income / Expenses row** | A full-width `Row` with two `DashboardMetric` children side by side, spaced evenly. |
| **Income metric** | Label "Total Income" (`expense_income_label`) in `bodyMedium` on `onSurfaceVariant`. Value formatted as currency in `titleLarge`, colored `IncomeGreen` |
| **Expenses metric** | Label "Total Expenses" (`expense_expenses_label`) in `bodyMedium` on `onSurfaceVariant`. Value formatted as currency in `titleLarge`, colored `ExpenseRed` |
| **Color tokens** | `IncomeGreen = Color(0xFF4CAF50)`, `ExpenseRed = Color(0xFFEF5350)` — preserved from existing implementation. These are semantic colors, not `DreamTheme` palette colors, and are intentionally hardcoded for income/expense semantics. |
| **Spacing** | `DreamTheme.spacing.sm` between label and value within each metric. `DreamTheme.spacing.md` between Total Balance and the income/expenses row. Padding of `DreamTheme.spacing.md` horizontal, `DreamTheme.spacing.sm` vertical around the section. |

**Layout diagram (top-to-bottom):**

```
┌──────────────────────────────────────┐
│  Total Balance (bodyMedium, muted)    │
│  $1,230.45   (headlineLarge, bold)    │
│                                       │
│  ┌──────────────┐ ┌──────────────┐   │
│  │ Total Income │ │Total Expenses│   │
│  │ +$2,500.00   │ │ -$1,269.55   │   │
│  │ (green)      │ │ (red)        │   │
│  └──────────────┘ └──────────────┘   │
└──────────────────────────────────────┘
```

### 3. Transaction List Section

| Property | Specification |
|---|---|
| **Section title** | `Text` with `style = titleMedium`, horizontal padding `DreamTheme.spacing.md`, string `expense_transactions_label` |
| **Component** | `ListItem` (Dream design system) per transaction |
| **Leading content** | Colored circle (40dp, `CircleShape`, background tinted 15% alpha of income-green or expense-red) containing an arrow icon pointing up (income) or down (expense) |
| **Headline** | Formatted amount string (e.g., "+$45.00" or "-$15.00") using `ExpensePresentationMapper.formatAmount()` |
| **Supporting text** | `"{category label} · {formatted date}"` — category via `TransactionCategory.asLabel()`, date via `ExpensePresentationMapper.formatDate()` which delegates to `TimeProvider.formatDate()` |
| **Trailing content** | `IconButton` with `Icons.Default.Delete`, tinted `onSurfaceVariant`, content description "Delete transaction" |
| **Divider** | `Divider` between items; first item preceded by a `Divider` below the section title |
| **Delete interaction** | Tapping the delete icon sets `pendingDeleteId` in ViewModel and shows a `Dialog` (see Interactions) |

### 4. Form Bottom Sheet

| Property | Specification |
|---|---|
| **Trigger** | plus/add `IconButton` in `TopAppBar` `actions` slot |
| **Container** | `Sheet` (Dream design system wrapper over `ModalBottomSheet`). The `Sheet` uses `DreamTheme.spacing.lg` rounded top corners and standard `rememberModalBottomSheetState()` |
| **State flag** | `showBottomSheet: Boolean` — NEW field in `ExpenseState`, default `false` |
| **Dismiss behavior** | `Sheet.onDismissRequest` → dispatches `ExpenseAction.DismissFormSheet`. Also dismissible by swipe-down drag gesture (built into `ModalBottomSheet`) and by tapping the add icon again when sheet is open (toggle) |
| **Sheet content (top-to-bottom)** | 1. Sheet title: "New Transaction" (`expense_new_transaction`) in `titleMedium`
2. `AmountField` — `TextField` with keyboard type `KeyboardType.Decimal`, placeholder `expense_amount_label`, error state when value is non-numeric or ≤ 0, supporting text `expense_amount_validation`
3. "Type" label (`expense_type_label`) in `labelLarge` on `onSurfaceVariant`
4. `SegmentedButton` with two options: `expense_type_expense` (index 0 → `TransactionType.EXPENSE`) and `expense_type_income` (index 1 → `TransactionType.INCOME`)
5. `CategoryDropdown` — `TextField` (readOnly) with trailing dropdown arrow icon, wrapped in a `Box`; `Menu` listing all `TransactionCategory.entries` as `DropdownMenuItem`s with labels from `asLabel()`
6. `NoteField` — `TextField` with placeholder `expense_note_label`, singleLine
7. `Button(variant = Primary, size = ComponentSize.Large)` labeled "Save" (`expense_save`), full width, `enabled` only when form is valid (amount > 0) |
| **Form validation** | Save button disabled when `amountText` is empty, non-numeric, or ≤ 0. Error indicator shown on amount field under same conditions via `isError` and `supportingText` |
| **On save** | `ExpenseAction.SaveTransaction` → ViewModel calls `repository.addTransaction(...)` → on success: form fields reset to defaults, sheet dismissed (`showBottomSheet = false`), list reloaded |
| **Sheet padding** | `DreamTheme.spacing.md` horizontal, `DreamTheme.spacing.lg` bottom (applied by `Sheet` component). Internal form spacing uses `Arrangement.spacedBy(DreamTheme.spacing.sm)` |

---

## State: ExpenseState (Updated Fields)

The existing `ExpenseState` receives one new field:

```kotlin
data class ExpenseState(
    val contentState: ExpenseContentState = ExpenseContentState.Loading,
    val amountText: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: TransactionCategory = TransactionCategory.OTHER,
    val noteText: String = "",
    val categoryMenuExpanded: Boolean = false,
    val dashboard: DashboardSummary = DashboardSummary(0.0, 0.0, 0.0),
    // NEW:
    val showBottomSheet: Boolean = false,
)
```

## Actions: NEW Additions

Two new actions are added to `ExpenseAction`:

| Action | Input | Effect |
|---|---|---|
| `ToggleFormSheet` | — | Toggle `showBottomSheet` between `true` / `false` |
| `DismissFormSheet` | — | Set `showBottomSheet = false` (called from `Sheet.onDismissRequest`) |

Existing actions remain unchanged. On successful `SaveTransaction`, the ViewModel should also set `showBottomSheet = false`.

---

## Interactions Summary

| User action | Trigger | Effect |
|---|---|---|
| Tap + icon in TopAppBar | `ExpenseAction.ToggleFormSheet` | Opens `Sheet` if closed; closes it if open |
| Swipe down on sheet | Native swipe gesture | Dismisses sheet → `ExpenseAction.DismissFormSheet` |
| Tap outside sheet scrim | Scrim tap | Dismisses sheet → `ExpenseAction.DismissFormSheet` |
| Tap Save button | `ExpenseAction.SaveTransaction` | Validates, saves, resets form, closes sheet, reloads list |
| Tap delete icon on transaction item | `ExpenseAction.DeleteTransaction(id)` | Shows confirmation dialog |

---

## Color Token Reference

| Usage | Token / Value |
|---|---|
| Income values | `Color(0xFF4CAF50)` |
| Expense values | `Color(0xFFEF5350)` |
| Income icon background | `IncomeGreen.copy(alpha = 0.15f)` |
| Expense icon background | `ExpenseRed.copy(alpha = 0.15f)` |

---

## Component Hierarchy (Full Screen Tree)

```
ExpenseScreen
├── Scaffold
│   ├── TopAppBar
│   │   ├── title: Text("Expense Tracker")
│   │   └── actions: IconButton(onClick: ToggleFormSheet)
│   │       └── Icon(Icons.Default.Add)
│   ├── SnackbarHost
│   └── Content Column (scrollable)
│       ├── [Loading] → CircularProgressIndicator (centered)
│       ├── [Empty] → Card(variant=Filled, title, body, accentColor=secondary)
│       ├── [Error] → Card(variant=Filled, title, error message, actionLabel="Retry", accentColor=error)
│       └── [Content]
│           ├── DashboardSection
│           │   ├── Text("Total Balance") [bodyMedium, muted]
│           │   ├── Text("$1,230.45") [headlineLarge, bold]
│           │   └── Row
│           │       ├── DashboardMetric: Text("Total Income") + Text("+$2,500.00", green)
│           │       └── DashboardMetric: Text("Total Expenses") + Text("-$1,269.55", red)
│           ├── Divider
│           └── TransactionListSection (unchanged)
│
└── [when showBottomSheet == true]
    └── Sheet(onDismissRequest: DismissFormSheet)
        └── Form Content Column
            ├── Text("New Transaction")
            ├── TextField(Amount, decimal keyboard, error state)
            ├── Text("Type")
            ├── SegmentedButton(["Expense", "Income"])
            ├── CategoryDropdown
            ├── TextField(Note)
            └── Button("Save", Primary, Large, enabled when valid)
```

---

## Handoff Summary

| Item | Detail |
|---|---|
| **View states** | 4 — Loading, Empty, Content, Error |
| **Design system components used** | `TopAppBar`, `IconButton`, `Card`, `CircularProgressIndicator`, `Divider`, `ListItem`, `SegmentedButton`, `Sheet`, `TextField`, `Menu`, `Button`, `Dialog` |
| **NEW state field** | `showBottomSheet: Boolean` |
| **NEW actions** | `ToggleFormSheet`, `DismissFormSheet` |
| **Semantic colors** | `IncomeGreen(0xFF4CAF50)`, `ExpenseRed(0xFFEF5350)` |
| **Platform concerns** | Keyboard avoidance in bottom sheet (Android: `imePadding`, iOS: `ScrollView`+keyboard) |
| **Time-sensitive behavior** | `TimeProvider.formatDate()` — no change |
