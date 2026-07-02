plugins {
    alias(libs.plugins.kmpCompose)
    alias(libs.plugins.koinCompiler)
}

dependencies {
    commonMainImplementation(projects.shared.designsystem)
    commonMainImplementation(projects.shared.core.strings)
    commonMainImplementation(libs.koin.compose)
    commonMainImplementation(libs.koin.composeNavigation3)
    commonMainImplementation(libs.compose.material3)
    commonMainImplementation(libs.compose.icons.extended)
    commonMainApi(libs.compose.components.resources)
    commonMainApi(libs.navigation3.runtime)
    commonMainImplementation(libs.navigation3.ui)
    commonTestImplementation(kotlin("test"))
}
