# Category Dropdown Styling Fix

## Problem

The category dropdown in the expense form bottom sheet had two visual issues:

1. **Background color mismatch**: The dropdown menu background did not match the `OutlinedTextField`'s container color. Multiple approaches were tried (`surfaceContainerHigh`, `surfaceVariant`, `surface`) but none produced the correct result.

2. **Horizontal padding mismatch**: The dropdown menu items did not have the same horizontal inset as the `OutlinedTextField`'s content text. The padding was applied at different levels (design system `Menu` column vs `CategoryDropdown` item boxes) but never matched the TextField's visual padding.

## Attempts Made (all unsuccessful)

1. Added `clickable` + transparent overlay to `CategoryDropdown` to make the field tappable (worked for opening, not styling)
2. Wrapped trailing icon in `IconButton` (worked for toggling, not styling)
3. Replaced `ListItem` with `Box` + `Text` for simpler item layout
4. Added `clip(RoundedCornerShape)` + `background(surfaceContainerHigh)` to design system `Menu`
5. Moved padding from `CategoryDropdown`'s `Box` to `Menu`'s `Column`
6. Removed all `clip`/`background` overrides to use the `DropdownMenu`'s natural `Surface`
7. Tried `DreamTheme.spacing.sm` (8dp), `DreamTheme.spacing.md` (16dp), and `DreamTheme.spacing.xs` (4dp) for horizontal padding at different levels

None of these produced a dropdown that visually matched the `OutlinedTextField` in either background color or content padding.

## Final Fix (applied by user directly)

The user resolved the issue manually outside of this automated workflow. The exact fix applied is not recorded here; refer to the current state of the code for the working implementation.

## Key Files Affected

- `feature/expense/impl/src/commonMain/.../ExpenseContent.kt` — `CategoryDropdown` composable
- `shared/designsystem/.../components/Menu.kt` — design system `Menu` wrapper

## Lesson

The gap between a design system component wrapper and a Material3 primitive (`OutlinedTextField` content padding vs `DropdownMenu` surface color) proved difficult to reconcile through the design system abstraction alone. The user's direct intervention was required to get the exact visual match.
