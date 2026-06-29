plugins {
    alias(libs.plugins.kmpFeatureData)
}

dependencies {
    commonMainApi(projects.feature.expense.domain)
    commonMainApi(projects.shared.core.data)
    commonMainApi(projects.shared.core.domain)
    commonMainImplementation(libs.koin.core)
    commonTestImplementation(projects.shared.core.testing)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
