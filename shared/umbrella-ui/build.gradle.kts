plugins {
    alias(libs.plugins.kmpCompose)
}

dependencies {
    commonMainImplementation(projects.shared.umbrellaCore)
    commonMainImplementation(projects.feature.sample.data)
    commonMainImplementation(projects.feature.sample.domain)
    commonMainImplementation(projects.shared.core.presentation)
    commonMainApi(projects.shared.designsystem)
    commonMainApi(projects.shared.navigation)
    commonMainApi(projects.feature.sample.api)
    commonMainImplementation(projects.feature.sample.impl)
    commonMainImplementation(libs.compose.runtime)
    commonMainImplementation(libs.koin.compose)
    commonMainImplementation(libs.koin.composeViewModel)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
