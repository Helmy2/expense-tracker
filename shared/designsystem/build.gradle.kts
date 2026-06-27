plugins {
    alias(libs.plugins.kmpCompose)
}

dependencies {
    commonMainImplementation(libs.compose.runtime)
    commonMainImplementation(libs.compose.foundation)
    commonMainImplementation(libs.compose.material3)
    commonMainImplementation(libs.compose.ui)
    commonMainImplementation(libs.compose.components.resources)
}
