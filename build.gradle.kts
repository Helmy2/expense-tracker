plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.koinCompiler) apply false
    alias(libs.plugins.skie) apply false
}

tasks.register("allTests") {
    description = "Run all tests across all modules."
    group = "verification"
    subprojects.forEach { subproject ->
        val testTask = subproject.tasks.findByName("allTests")
        if (testTask != null) {
            dependsOn(testTask)
        }
    }
}
