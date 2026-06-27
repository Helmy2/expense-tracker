plugins {
    alias(libs.plugins.kmpSharedCore)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.skie)
}

dependencies {
    commonMainApi(projects.feature.expense.data)
    commonMainApi(projects.feature.expense.domain)
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
            export(projects.feature.expense.data)
            export(projects.feature.expense.domain)
            export(projects.shared.core.data)
            export(projects.shared.core.domain)
        }
    }
}
