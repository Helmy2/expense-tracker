plugins {
    alias(libs.plugins.kmpFeatureData)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainApi(projects.feature.budget.domain)
    commonMainApi(projects.feature.expense.domain)
    commonMainApi(projects.shared.core.data)
    commonMainApi(projects.shared.core.domain)
    commonMainImplementation(libs.koin.core)
    commonMainImplementation(libs.kotlinx.datetime)
    commonTestImplementation(projects.shared.core.testing)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
