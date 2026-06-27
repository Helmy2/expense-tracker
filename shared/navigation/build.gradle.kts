plugins {
    alias(libs.plugins.kmpCompose)
    alias(libs.plugins.koinCompiler)
}

dependencies {
    commonMainImplementation(projects.shared.designsystem)
    commonMainImplementation(libs.koin.compose)
    commonMainImplementation(libs.koin.composeNavigation3)
    commonMainImplementation(libs.compose.material3)
    commonMainApi(libs.navigation3.runtime)
    commonMainImplementation(libs.navigation3.ui)
    commonTestImplementation(kotlin("test"))
}
