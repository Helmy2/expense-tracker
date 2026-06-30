# Recurring Transactions Feature — UI/UX Specification

## Overview

Three screens + one embedded section:
1. **RecurringListScreen** — Full-screen list of all recurring templates with pause/resume toggles, swipe-to-delete, FAB
2. **RecurringFormScreen** — Create/edit form (amount, type, category, note, frequency, start/end date)
3. **DashboardUpcomingSection** — Embedded on main expense dashboard showing up to 3 upcoming items

---

## Screen 1: RecurringListScreen

| Route | `RecurringListRoute` (NavKey) |
|---|---|
| Purpose | Display all recurring templates in scrollable list. Each row: formatted amount (±), category, frequency label, next due date, pause/resume switch. FAB to add. Swipe-to-delete. |

### States

1. **Loading**: Full-screen centered `CircularProgressIndicator(size = Large)`. Top app bar with title "Recurring Transactions".
2. **Empty**: Card with title, body message, and "Create First" action button. FAB still visible.
3. **Content**: LazyColumn/List of template rows. Each row via `ListItem` (Dream DS): leading colored circle + arrow icon (up=income green, down=expense red), headline formatted amount, supporting text `"{category} · {frequency}"`, trailing `Switch` + next due date text. Paused rows at 0.5 opacity with "Paused" label.
4. **Error**: Error card with retry button.

### Interactions

- Tap FAB → navigate to create form (RecurringFormRoute with null templateId)
- Tap row → navigate to edit form (RecurringFormRoute with templateId)
- Toggle switch → calls togglePause, optimistic UI update, reload on success
- Swipe left / swipeActions → delete with confirmation dialog
- Tap "Retry" → reload
- Tap "Create First" → navigate to create form

---

## Screen 2: RecurringFormScreen

| Route | `RecurringFormRoute(templateId: String? = null)` |
|---|---|
| Purpose | Create or edit a recurring template. |

### States

1. **Create**: Empty form, title "New Recurring Transaction", back button, save disabled until valid.
2. **Edit**: Pre-filled form, title "Edit Recurring Transaction", back + delete toolbar button.
3. **Loading (edit)**: Centered spinner while fetching template data.
4. **Error**: Retry card on load failure. Snackbar on save failure.

### Form Fields (scrollable, top-to-bottom)

| Field | Component | Validation |
|---|---|---|
| Amount | TextField(decimal keyboard) | Required > 0 |
| Type | SegmentedButton: Expense/Income | Always valid |
| Category | TextField(readOnly) + Menu dropdown | Always valid (default OTHER) |
| Note | TextField(singleLine, optional) | Optional |
| Frequency | SegmentedButton: Daily/Weekly/Monthly/Yearly | Always valid (default MONTHLY) |
| Start Date | TextField(readOnly) + DatePicker | Required, default today |
| End Date | TextField(readOnly) + DatePicker + Clear(×) | Optional |

### Bottom: Save button (primary, full-width, disabled when invalid). Edit mode only: Delete (destructive) button below.

---

## Section 3: DashboardUpcomingSection

Embedded in ExpenseContent. Shows up to 3 upcoming recurring items.

Layout:
```
[Header: "Upcoming Recurring"  |  "See All" >]
[Card with rows:]
  +$1,500.00  Rent · Monthly
  Due: Jul 1, 2026
  -$45.00     Netflix · Monthly
  Due: Jul 5, 2026
```

Insert between Dashboard section and Transaction list section in ExpenseContent.

### Interactions
- Tap "See All" → navigate to RecurringListRoute
- Tap a row → navigate to edit form for that template

---

## Navigation Flow

```
ExpenseScreen
  ├── Top bar "Recurring" button → RecurringListRoute
  │     ├── FAB/+ → RecurringFormRoute(null) [Create]
  │     ├── Tap row → RecurringFormRoute(id) [Edit]
  │     └── Back → ExpenseScreen
  └── Dashboard "See All" → RecurringListRoute
```

## ExpenseScreen Changes

ExpenseState adds `upcomingRecurring: List<UpcomingRecurringUi>`. ExpenseContent gets new parameter `onNavigateToRecurringList`. Top bar gets a "Recurring" text button next to "Budgets".

---

## Platform Notes

| Android (Compose) | iOS (SwiftUI) |
|---|---|
| FAB for add | Toolbar "+" button instead of FAB |
| `SwipeToDismiss` for delete | `.swipeActions(edge: .trailing)` |
| `DatePickerDialog` (M3) | `.sheet` with `DatePicker` |
| `imePadding()` for keyboard | `.scrollDismissesKeyboard(.interactively)` |
| `KeyboardType.Decimal` | `.keyboardType(.decimalPad)` |
| Dream DS: Card, Button, TextField, Switch, SegmentedButton, Menu, Dialog, FAB, ListItem, Divider | Dream DS: DreamCard, DreamButton, DreamTextField, DreamSegmentedPicker, Toggle, DreamCircularProgressIndicator |

## Colors

| Usage | Color |
|---|---|
| Income | `#4CAF50` |
| Expense | `#EF5350` |
| Paused opacity | 0.5 |
