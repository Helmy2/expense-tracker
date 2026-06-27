plugins {
    alias(libs.plugins.kmpCompose)
}

dependencies {
    commonMainApi(libs.compose.runtime)
    commonMainApi(libs.compose.components.resources)
}