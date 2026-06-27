plugins {
    alias(libs.plugins.kmpSharedCore)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.skie)
}

dependencies {
    commonMainApi(projects.feature.sample.data)
    commonMainApi(projects.feature.sample.domain)
    commonMainApi(projects.shared.core.data)
    commonMainApi(projects.shared.core.domain)
    commonMainImplementation(libs.koin.core)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonTestImplementation(kotlin("test"))
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(projects.feature.sample.data)
            export(projects.feature.sample.domain)
            export(projects.shared.core.data)
            export(projects.shared.core.domain)
        }
    }
}
