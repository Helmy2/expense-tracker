plugins {
    alias(libs.plugins.kmpAndroidApp)
}

android {
    namespace = "com.expense.tracker.android"

    defaultConfig {
        applicationId = "com.expense.tracker.android"
    }
}

dependencies {
    implementation(projects.shared.umbrellaCore)
    implementation(projects.shared.umbrellaUi)
    implementation(libs.androidx.splashscreen)
}
