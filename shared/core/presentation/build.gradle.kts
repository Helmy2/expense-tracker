plugins {
    alias(libs.plugins.kmpSharedCore)
}

dependencies {
    commonMainImplementation(projects.shared.core.domain)
    commonMainApi(libs.androidx.lifecycle.viewmodel)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
