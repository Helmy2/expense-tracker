# Expense Tracker

A Kotlin Multiplatform expense tracker for Android and iOS, built with Jetpack Compose (Android) and native SwiftUI (iOS).

## Features

- Dashboard with total expenses and per-category breakdown
- Add expenses with amount, category, and optional note
- Delete expenses with swipe or long-press
- Bottom-sheet form for quick entry

## Tech Stack

- **Shared logic**: Kotlin Multiplatform — domain, data, and API layers
- **Android UI**: Jetpack Compose, Material 3, Koin DI, Navigation 3
- **iOS UI**: Native SwiftUI with KMP bridge via SKIE
- **Local storage**: Room (KMP), DataStore Preferences
- **Architecture**: Presentation → Domain ← Data (Clean Architecture)

## Modules

| Module | Description |
|---|---|
| `androidApp` | Android host app |
| `iosApp-swiftui` | iOS host app (SwiftUI) |
| `shared/core/*` | Shared infrastructure (data, domain, testing, strings) |
| `shared/designsystem` | Theme tokens and reusable components |
| `shared/navigation` | Centralized navigation shell |
| `shared/umbrella-core` | Business logic aggregation for native consumers |
| `shared/umbrella-ui` | Compose composition root and Koin startup |
| `feature/expense/*` | Expense feature split into domain, data, api, impl |

## Build & Run

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS
xcodebuild -project iosApp-swiftui/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.6' build
```
