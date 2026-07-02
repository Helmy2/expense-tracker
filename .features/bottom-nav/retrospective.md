# Template Improvement Retrospective — bottom-nav

## Context

- Project: expense-tracker
- Template base: dream-kmp-base
- Work reviewed: Bottom navigation bar feature — persistent 3-tab NavigationBar with per-tab back stacks
- Date: 2026-07-02

## What Worked Well

- Design system `NavigationBar` component was ready to use — no modifications needed
- Navigation3 + Koin DSL integration was straightforward
- Per-tab back stack pattern with `MutableState<SnapshotStateList<NavKey>>` identity switching worked cleanly with NavDisplay
- iOS SwiftUI `TabView` automatically handles tab bar hide/show on NavigationStack push — zero custom code needed
- Parallel Phase 2 delegation (Android + iOS) saved significant time

## What Did Not Work Well

- `StringResource` type in `TabDefinition` required `@OptIn(InternalResourceApi::class)` workaround for tests
- Inspector found missing tests that should have been created during Phase 1 — the contract explicitly required them
- Dead strings (`expense_budgets_button`, `recurring_nav_button`) were left behind by the Android implementer — cleanup should be part of the phase
- The `dream-tester` subagent was cancelled during execution — had to run verification manually

## What Was Easy

- Creating the BottomNavNavigator class (clean API, straightforward per-tab stack management)
- Updating AppShell with AnimatedVisibility for bottom bar
- SwiftUI TabView integration (1 file change, automatic behavior)
- Removing navigation action buttons from ExpenseScreen

## What Was Hard

- Figuring out the correct `StringResource` constructor for test instances
- Ensuring `currentBackStack` identity switching worked with NavDisplay's recomposition model
- Coordinating 3 parallel subagent calls (Phase 1 + Phase 2 Android + Phase 2 iOS) with shared state

## Specific Things Learned

### Reliable Smoke Patterns

- `assertVisible: "Expenses"` / `"Budgets"` / `"Recurring"` for tab label verification
- Tab switching via `tapOn: "Budgets"` then `assertVisible: "Budgets"` for content verification

### Brittle Smoke Patterns

- Bottom bar visibility assertion is tricky in Maestro — prefer tab content assertions over bar visibility assertions

### Architecture Lessons

- Shared navigation infrastructure (BottomNavNavigator) belongs in `shared/navigation`, not feature modules
- Per-tab back stacks with identity switching is the cleanest Navigation3 pattern for bottom nav
- Design-system components should be tested for readiness before feature work begins
- When modifying shared modules, all consumers must be updated in the same phase

### Token Optimization Lessons

- Parallel delegation of independent phases (Android + iOS) reduces total wall time
- Including file content in subagent task prompts avoids redundant reads
- Condensed rules file is sufficient for most subagent tasks

## Recommended Template Improvements

### High Priority

- Add `BottomNavNavigator` pattern to knowledge graph as a reusable navigation pattern
- Require test creation as part of Phase 1 delegation, not as a separate inspector-found defect
- Add checklist item: "Remove dead strings from strings.xml when removing UI references"

### Medium Priority

- Add `StringResource` test helper to `shared/core/testing` module
- Document per-tab back stack pattern in best-practices for Navigation3

### Low Priority

- Add Maestro smoke flow template for bottom-nav features

## Token Optimization Recommendations

### Agent Efficiency

- Pass contract + UI spec content directly to subagents instead of file paths
- Use fresh subagent per repair round to avoid context bloat

### Verification Efficiency

- Run compilation checks before full test suite to catch errors early
- Skip Maestro flows when emulator/simulator is not available — document as environment blocker

### Output Structure

- Combine verification report and retrospective into a single workflow step

### Context Management

- Write state to `.features/` after each phase completion for fresh subagent handoff

## Suggested Template Defaults To Add

- `BottomNavNavigator` class template with per-tab back stacks
- `TabDefinition` data class with `StringResource` label
- Test helper for creating `StringResource` instances in tests
- Maestro flow template for bottom-nav features

## Application Status

- Applied: `2026-07-02`
- What was updated: Added BottomNavNavigator pattern, StringResource test workaround, dead string cleanup checklist

## Bottom Line

The bottom-nav feature demonstrated that design-system readiness and parallel delegation significantly accelerate implementation. The main friction points were test creation (StringResource type requirements) and cleanup of dead references — both should be addressed in the template as explicit checklist items rather than inspector-found defects.
