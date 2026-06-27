plugins {
    alias(libs.plugins.kmpFeatureImpl)
}

dependencies {
    commonMainImplementation(projects.feature.sample.api)
    commonMainImplementation(projects.feature.sample.domain)
    commonMainImplementation(projects.shared.core.domain)
    commonMainImplementation(projects.shared.core.presentation)
    commonMainImplementation(projects.shared.core.strings)
    commonMainImplementation(projects.shared.designsystem)
    commonMainImplementation(projects.shared.navigation)
    commonMainImplementation(libs.compose.runtime)
    commonMainImplementation(libs.compose.foundation)
    commonMainImplementation(libs.compose.components.resources)
    commonMainImplementation(libs.koin.compose)
    commonMainImplementation(libs.koin.composeNavigation3)
    commonMainImplementation(libs.koin.composeViewModel)
    commonMainImplementation(libs.compose.material3)
    commonMainImplementation(libs.compose.ui)
    commonTestImplementation(projects.shared.core.testing)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
