plugins {
    alias(libs.plugins.kmpFeatureData)
}

dependencies {
    commonMainApi(projects.feature.recurringTransactions.domain)
    commonMainApi(projects.shared.core.data)
    commonMainApi(projects.shared.core.domain)
    commonMainImplementation(libs.koin.core)
    commonMainImplementation(libs.kotlinx.datetime)
    commonTestImplementation(projects.shared.core.testing)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
    commonTestImplementation(libs.kotlinx.datetime)
}
