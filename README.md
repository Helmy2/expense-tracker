# Dream KMP Base Template

This template follows JetBrains' new top-level Kotlin Multiplatform app structure and Dream's modular internal architecture.

This `temp` folder is the canonical starter project that Dream's initializer copies and renames. It is self-contained for OpenCode work: Dream agents, commands, guidance, and graph rules are packaged inside hidden project folders.

Packaged Dream context:

- `.opencode/opencode.json`: OpenCode instructions and MCP hot reload config
- `.opencode/agents/`: Dream agent definitions
- `.opencode/commands/`: Dream slash-command workflows
- `.opencode/guidance/`: operating guidance and prompt scaffolds
- `.opencode/knowledge-graph/`: architecture graph and best-practice rules
- `.features/`: generated app contracts, decisions, feature contracts, and verification reports
- `maestro/`: Android and iOS smoke-flow scaffolding for generated feature validation
- `scripts/dream-preflight.sh`: git, Android SDK, and Maestro readiness check used before verification depends on local tooling
- `scripts/sync-pbxproj.rb`: auto-registers orphaned Swift files in Xcode project.pbxproj
- `scripts/remove-sample.sh`: removes sample feature files, strips sample deps, and checks for leftovers
- `.opencode/guidance/new-feature-checklist.md`: contract-first feature wiring, testing, verification, and git checklist

Modules:

- `androidApp`: Android host app
- `iosApp-compose`: iOS host app using Compose Multiplatform (consumes the exported `Shared` framework from `umbrella-ui`)
- `iosApp-swiftui`: iOS host app using native SwiftUI (consumes the exported `SharedCore` framework from `umbrella-core` via SKIE)
- `shared/core/*`: split shared infrastructure modules
- `shared/core/data`: shared Room builder defaults, DataStore key-value storage factory, Ktor HttpClient factory, and session storage
- `shared/core/strings`: Compose Resources string catalog exported for feature UI
- `shared/designsystem`: theme, tokens, and reusable components
- `shared/navigation`: centralized shell, Navigator, and Navigation 3 display infrastructure
- `shared/umbrella-core`: non-Compose shared business logic aggregation and data/domain dependency wiring for SwiftUI or other native UI consumers
- `shared/umbrella-ui`: exported Compose UI composition root, Koin startup, Navigator wiring, and Koin Navigation 3 entry aggregation
- `feature/sample/*`: sample feature split into `domain`, `data`, `api`, and `impl`
- `build-logic`: Dream convention plugins used by the modular template

Key architecture rules:

- thin app shells
- `Presentation -> Domain <- Data`
- domain stays pure Kotlin
- shared core is split by responsibility
- navigation stays centralized

Template baseline:

- Koin compiler plugin DSL modules are the default DI path for new modules; do not use annotation-based DI such as `@KoinViewModel`, `@Module`, or `@ComponentScan`
- shared screen state uses lifecycle `ViewModel` plus Koin ViewModel wiring at stable navigation/composition roots
- app navigation uses Navigation 3 typed routes, a Koin-provided `Navigator`, `NavDisplay`, and Koin `navigation<Route>` entry providers installed by feature `impl` Koin modules
- feature route types (`NavKey`) live in `feature/<name>/api`; screens and Koin navigation installers live in `feature/<name>/impl`; shared navigation stays centralized by operating on `NavKey`
- the shared MVI base processes actions sequentially instead of launching one coroutine per action
- the sample feature uses Room for local persisted data on Android and iOS
- common Room builder setup lives in `shared/core/data` and uses a bundled SQLite driver plus `Dispatchers.Default` for KMP-safe query execution
- common Room builder setup enables destructive migration fallback for first-pass MVP iteration; replace with real migrations when preserving local data matters
- DataStore Preferences key-value storage is available via `shared/core/data` with expect/actual factory creation for Android and iOS
- Ktor HTTP client support is wired into feature data modules by default; a shared `HttpClientFactory` in `shared/core/data` provides ContentNegotiation (JSON), Logging, and optional bearer token auth
- Kotlin serialization is applied globally so `@Serializable` is available across all modules including Navigation 3 route definitions
- `SessionStorage` domain contract and DataStore-backed implementation provide optional session/auth token persistence
- user-visible sample copy comes from `shared/core/strings` Compose Resources instead of hardcoded composable text
- shared result helpers preserve `CancellationException` instead of swallowing structured concurrency
- `shared/core/testing` is the reusable home for coroutine-aware test helpers
- app shells start Koin once and pass only platform factories into shared code
- `shared/umbrella-core` stays free of Compose and Navigation 3 UI dependencies; `shared/umbrella-ui` owns Compose startup and navigation display wiring
- the sample UI demonstrates shared loading, empty, content, and error-state components
- the shared shell applies safe-area padding and token-backed theme styling
- Android and iOS are product runtime targets
- the packaged SwiftUI iOS host includes an `iosAppTests` XCTest target and shared scheme so native host changes are verifiable through `xcodebuild test`

Verification commands:

```bash
./gradlew :feature:sample:impl:allTests :shared:navigation:allTests :shared:umbrella-core:allTests :shared:core:domain:allTests :shared:core:data:allTests :shared:core:presentation:allTests :shared:core:testing:allTests
./gradlew :androidApp:assembleDebug
xcodebuild -project iosApp-swiftui/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.6' build
xcodebuild -project iosApp-swiftui/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.6' test
```
