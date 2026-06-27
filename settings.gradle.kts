rootProject.name = "expense-tracker"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":shared:core:domain")
include(":shared:core:data")
include(":shared:core:presentation")
include(":shared:core:strings")
include(":shared:core:testing")
include(":shared:designsystem")
include(":shared:navigation")
include(":shared:umbrella-core")
include(":shared:umbrella-ui")
include(":feature:expense:domain")
include(":feature:expense:data")
include(":feature:expense:api")
include(":feature:expense:impl")
include(":feature:budget:domain")
include(":feature:budget:data")
include(":feature:budget:api")
include(":feature:budget:impl")
