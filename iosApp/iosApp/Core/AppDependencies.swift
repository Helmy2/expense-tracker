import Foundation

@Observable
final class AppDependencies {
    static let shared = AppDependencies()

    let bridge: any SampleRepositoryBridge

    private init() {
        bridge = SharedCoreBridge()
    }
}
