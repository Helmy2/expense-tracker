# Budget Feature — UI/UX Specification

**Feature:** budget  
**Source contract:** `.features/budget/contract.yaml`  
**Target platforms:** Android (Compose), iOS (SwiftUI)  
**Design system:** Dream design-system components and `DreamTheme` tokens  

---

## 1. Design Token Reference

### Color Roles (Budget-Specific)

| Role | Token / Usage |
|---|---|
| Budget bar — under 75% | Custom green (`#4CAF50`). Passed as `color` override on `LinearProgressIndicator`. |
| Budget bar — 75%–90% | Custom yellow/amber (`#FFC107`). Passed as `color` override on `LinearProgressIndicator`. |
| Budget bar — 90%+ (including over-budget) | Custom red. Matches `MaterialTheme.colorScheme.error`. |
| Over-budget warning icon | `MaterialTheme.colorScheme.error` |
| Form validation error | `MaterialTheme.colorScheme.error` (existing `TextField` `isError`) |

> **Note for implementers:** The existing `LinearProgressIndicator` wrapper applies `MaterialTheme.colorScheme.primary` as the bar color. For budget progress bars the implementer must supply a custom `color` parameter matching the thresholds above. This is a documented deviation from the default wrapper.

### Spacing Tokens

Use `DreamTheme.spacing` exclusively:

| Token | Value |
|---|---|
| `xs` | 4.dp |
| `sm` | 8.dp |
| `md` | 16.dp |
| `lg` | 24.dp |
| `xl` | 32.dp |

---

## 2. Screens

### 2.1 BudgetList Screen

**Purpose:** Main budgets screen. Shows a list of all configured budgets with category name, spent vs. limit, and a color-coded progress bar. Provides navigation to add, edit, and view budget details.

**Entry point:** "Budgets" `Button` (variant `Tertiary`) in the main expense screen's `TopAppBar` actions.

#### View States

##### Loading State
- `TopAppBar` with title = `budget_list_title` ("Budgets") and back navigation icon.
- Body: centered `CircularProgressIndicator(size = ComponentSize.Large)`.
- No FAB visible.

##### Empty State
- `TopAppBar` with title and back navigation icon.
- Body: single `Card` (variant `Filled`, `accentColor = secondary`):
  - `title = budget_empty_title` ("No budgets yet")
  - `body = budget_empty_body` ("Create your first budget to start tracking spending limits by category.")
- `FloatingActionButton` at bottom-right with "+" icon → navigate to `BudgetForm` (create mode).

##### Content State
- `TopAppBar` with title and back navigation icon.
- Body: vertically scrollable `Column` with spacing `sm`, padding horizontal `md`, vertical `sm`.
- For each `BudgetWithSpending` item, a `Card` (variant `Elevated`, `onClick` → navigate to `BudgetDetail`):
  - **Row 1 — Header:** Leading 40dp circle with 15% opacity background tint matching status color. `titleMedium` category name. Trailing `IconButton` with edit icon → navigate to `BudgetForm` (edit mode).
  - **Row 2 — Amounts:** `bodyMedium` in `onSurfaceVariant`: `"$spentAmount of $monthlyLimit"`.
  - **Row 3 — Progress bar:** `LinearProgressIndicator` with progress clamped `[0.0, 1.0]`. Color by `BudgetStatus`: green (`#4CAF50`) < 75%, yellow (`#FFC107`) 75%-90%, red (`error`) >= 90%.
  - **Row 4 — Over-budget warning (conditional):** When `status == OVER_BUDGET`, show warning row with `Icons.Filled.Warning` icon in `error` color + `bodyMedium` text: `budget_over_budget_warning`.
- `HorizontalDivider` between cards.
- `FloatingActionButton` at bottom-right with "+" icon.

##### Error State
- `TopAppBar` with title and back navigation icon.
- Body: `Card` (variant `Filled`, `accentColor = error`):
  - `title = budget_error_title`, `body = budget_error_body`, `actionLabel = budget_retry` → re-trigger `LoadBudgets`.

#### Interactions

| Gesture | Action |
|---|---|
| Tap back icon | Navigate back to expense screen |
| Tap a budget card | Navigate to `BudgetDetail` with budget id |
| Tap edit icon on card | Navigate to `BudgetForm` (edit mode) |
| Tap FAB ("+") | Navigate to `BudgetForm` (create mode) |
| Tap "Retry" on error card | Re-trigger `LoadBudgets` |

### 2.2 BudgetDetail Screen

**Purpose:** Detail view for a single budget. Shows category, limit, spent, remaining, and contributing transactions.

#### View States

##### Loading State
- `TopAppBar` with title = category name and back navigation icon.
- Body: centered `CircularProgressIndicator(size = ComponentSize.Large)`.

##### Content State
- `TopAppBar` with title = category name, back navigation icon, edit and delete `IconButton` actions.
- Body: vertically scrollable `Column` with spacing `sm`, padding `md`/`sm`.

**Sections (top to bottom):**

1. **Summary Card** (`Card` variant `Elevated`):
   - Category name: `titleMedium`.
   - Row of two metrics (spaced evenly):
     - **Spent:** label `budget_spent_label` ("Spent") in `bodyMedium` `onSurfaceVariant`; value in `titleLarge` `error` color.
     - **Remaining:** label `budget_remaining_label` ("Remaining") in `bodyMedium` `onSurfaceVariant`; value in `titleLarge` — `error` if negative, green if positive.
   - Progress bar: `LinearProgressIndicator(progress = percentage, color = statusColor)`.
   - Over-budget banner (conditional): if `OVER_BUDGET`, warning row.

2. **Transactions Section:**
   - Section header: `titleMedium` = `budget_transactions_title` ("Transactions this month").
   - `HorizontalDivider`.
   - For each transaction: `ListItem` with leading 40dp circle, headline = formatted amount, supporting = `"$category · $formattedDate"`.
   - `HorizontalDivider(inset start = md)` between items.
   - Empty: `Card` (Filled, secondary) with `budget_no_transactions_title` / `budget_no_transactions_body`.

##### Error State
- `Card` (Filled, error) with title, body, and retry action.

#### Interactions

| Gesture | Action |
|---|---|
| Tap back icon | Navigate back to `BudgetList` |
| Tap edit icon | Navigate to `BudgetForm` (edit mode) |
| Tap delete icon | Show delete confirmation `Dialog` |
| Confirm delete | `DeleteBudget`, navigate back to `BudgetList` |

### 2.3 BudgetForm Screen

**Purpose:** Form for creating or editing a budget. Category picker + monthly limit input.

#### Create Mode
- `TopAppBar` title = `budget_form_create_title` ("New Budget"), back navigation.
- Form fields (top to bottom):
  1. **Category Picker:** `TextField` (readOnly) with dropdown arrow. `Menu` lists categories without existing budgets. Pattern matches expense `CategoryDropdown`.
  2. **Monthly Limit:** `TextField` with `KeyboardType.Decimal`, validation, `budget_limit_placeholder`.
  3. **Save Button:** full-width `Button` (Primary), enabled when valid.

#### Edit Mode
- Same layout. Title = `budget_form_edit_title` ("Edit Budget").
- Category field disabled (cannot change). Limit pre-filled.

#### Interactions

| Gesture | Action |
|---|---|
| Tap back icon | Navigate back (discard changes) |
| Tap category field / dropdown | Toggle `Menu` |
| Tap a category | Select, dismiss `Menu` |
| Type in limit field | Validate in real time |
| Tap "Save" | `CreateBudget` or `UpdateBudget`, navigate back, show snackbar |

---

## 3. Navigation Flow

```
Expense Screen → "Budgets" button → BudgetList
  ├─ Tap card → BudgetDetail
  │    ├─ Edit icon → BudgetForm (edit)
  │    ├─ Delete icon → Dialog → BudgetList
  │    └─ Back → BudgetList
  ├─ Edit icon → BudgetForm (edit)
  ├─ FAB → BudgetForm (create)
  └─ Back → Expense Screen

BudgetForm → Save → BudgetList (with snackbar)
BudgetForm → Back → BudgetList
```

---

## 4. Progress Bar Color-Coding

| BudgetStatus | Condition | Bar Color |
|---|---|---|
| `UNDER_75` | percentage < 0.75 | Green `#4CAF50` |
| `BETWEEN_75_90` | 0.75 ≤ percentage < 0.90 | Yellow `#FFC107` |
| `OVER_90` | 0.90 ≤ percentage ≤ 1.0 | Red (`error`) |
| `OVER_BUDGET` | percentage > 1.0 | Red (`error`) |

Progress value is clamped to 1.0 max. Track color remains `surfaceVariant`.

---

## 5. Over-Budget Warning Indicator

Trigger: `BudgetWithSpending.status == OVER_BUDGET` (percentage > 1.0).

Design: Row with warning icon (`Icons.Filled.Warning`, `error` tint) + `bodyMedium` text in `error` color: `budget_over_budget_warning`. Appears below the progress bar on both BudgetList and BudgetDetail.

---

## 6. String Resources

All strings use `budget_` prefix in `shared/core/strings/src/commonMain/composeResources/values/strings.xml`.

| Resource Name | Value |
|---|---|
| `budget_list_title` | "Budgets" |
| `budget_empty_title` | "No budgets yet" |
| `budget_empty_body` | "Create your first budget to start tracking spending limits by category." |
| `budget_error_title` | "Something went wrong" |
| `budget_error_body` | "Could not load your budgets." |
| `budget_retry` | "Retry" |
| `budget_spent_label` | "Spent" |
| `budget_remaining_label` | "Remaining" |
| `budget_transactions_title` | "Transactions this month" |
| `budget_no_transactions_title` | "No transactions yet" |
| `budget_no_transactions_body` | "No expense transactions for this category this month." |
| `budget_form_create_title` | "New Budget" |
| `budget_form_edit_title` | "Edit Budget" |
| `budget_category_label` | "Category" |
| `budget_limit_label` | "Monthly Limit" |
| `budget_limit_placeholder` | "Enter amount" |
| `budget_limit_validation` | "Please enter a valid amount greater than zero" |
| `budget_save` | "Save" |
| `budget_delete_title` | "Delete Budget" |
| `budget_delete_body` | "Are you sure you want to delete this budget? This cannot be undone." |
| `budget_delete_confirm` | "Delete" |
| `budget_delete_dismiss` | "Cancel" |
| `budget_created_snackbar` | "Budget created" |
| `budget_updated_snackbar` | "Budget updated" |
| `budget_deleted_snackbar` | "Budget deleted" |
| `budget_over_budget_warning` | "Over budget! You've exceeded your monthly limit for this category." |

---

## 7. Design System Component Inventory

| Component | Usage |
|---|---|
| `TopAppBar` | All screens — title bar |
| `Card` (Elevated) | Budget list items, summary detail card |
| `Card` (Filled) | Empty state, error state, empty transactions |
| `Button` (Primary) | Form save button |
| `Button` (Destructive) | Delete confirmation |
| `Button` (Tertiary) | Delete dismissal, "Budgets" nav button |
| `TextField` | Limit input, category picker (readOnly) |
| `LinearProgressIndicator` | Budget progress bars (with color override) |
| `CircularProgressIndicator` | Loading states |
| `IconButton` | Back nav, edit, delete, dropdown |
| `ListItem` | Transaction items in BudgetDetail |
| `Menu` | Category dropdown in form |
| `Dialog` | Delete confirmation |
| `HorizontalDivider` | Between cards and transaction items |
