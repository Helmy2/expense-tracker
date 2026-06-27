# iOS SwiftUI Variant

This packaged iOS host uses native SwiftUI and consumes the exported `SharedCore` framework from `shared/umbrella-core`.

Structure:

- `iosApp/Core`: bridge and dependency assembly for Swift-to-Kotlin interop
- `iosApp/Models`: native Swift models and form state
- `iosApp/ViewModels`: `@Observable` SwiftUI state holders
- `iosApp/Views`: native SwiftUI screens and forms
- `Configuration/Config.xcconfig`: bundle identity and visible app name

Build notes:

- Xcode runs `:shared:umbrella-core:embedAndSignAppleFrameworkForXcode`
- `FRAMEWORK_SEARCH_PATHS` points at `shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
- SKIE provides Swift-friendly `async throws` wrappers for Kotlin `suspend` functions

Testing notes:

- `iosAppTests` verifies the native SwiftUI bridge and view models without rebuilding Kotlin code in tests
- Keep business logic in shared Kotlin modules; keep the iOS host thin
