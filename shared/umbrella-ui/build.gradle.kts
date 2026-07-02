plugins {
    alias(libs.plugins.kmpCompose)
}

dependencies {
    commonMainImplementation(projects.shared.umbrellaCore)
    commonMainImplementation(projects.shared.core.strings)
    commonMainImplementation(projects.feature.budget.data)
    commonMainImplementation(projects.feature.budget.domain)
    commonMainImplementation(projects.feature.budget.impl)
    commonMainImplementation(projects.feature.expense.data)
    commonMainImplementation(projects.feature.expense.domain)
    commonMainImplementation(projects.shared.core.presentation)
    commonMainApi(projects.shared.designsystem)
    commonMainApi(projects.shared.navigation)
    commonMainApi(projects.feature.budget.api)
    commonMainApi(projects.feature.expense.api)
    commonMainApi(projects.feature.recurringTransactions.api)
    commonMainImplementation(projects.feature.expense.impl)
    commonMainImplementation(projects.feature.recurringTransactions.data)
    commonMainImplementation(projects.feature.recurringTransactions.domain)
    commonMainImplementation(projects.feature.recurringTransactions.impl)
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
