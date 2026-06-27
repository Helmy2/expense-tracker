plugins {
    alias(libs.plugins.kmpFeatureDomain)
}

dependencies {
    commonMainApi(projects.shared.core.domain)
    commonMainApi(projects.feature.expense.domain)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
