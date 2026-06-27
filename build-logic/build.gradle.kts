plugins {
    `kotlin-dsl`
}

java {
    val jvmVersion = JavaVersion.VERSION_17
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Makes the version catalog accessible in convention plugins via `the<LibrariesForLibs>()`
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.plugins.androidApplication.toDependency())
    implementation(libs.plugins.androidLibrary.toDependency())
    implementation(libs.plugins.kotlinAndroid.toDependency())
    implementation(libs.plugins.kotlinMultiplatform.toDependency())
    implementation(libs.plugins.kotlinSerialization.toDependency())
    implementation(libs.plugins.composeMultiplatform.toDependency())
    implementation(libs.plugins.composeCompiler.toDependency())
    implementation(libs.plugins.androidMultiplatformLibrary.toDependency())
    implementation(libs.plugins.mokkery.toDependency())
}

fun Provider<PluginDependency>.toDependency(): Provider<String> =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

gradlePlugin {
    plugins {
        register("kmp") {
            id = "app.kmp"
            implementationClass = "DreamKmpConventionPlugin"
        }
        register("kmpCompose") {
            id = "app.kmp.compose"
            implementationClass = "DreamKmpComposeConventionPlugin"
        }
        register("androidApp") {
            id = "app.kmp.android-app"
            implementationClass = "DreamAndroidAppConventionPlugin"
        }
        register("sharedCore") {
            id = "app.kmp.shared-core"
            implementationClass = "DreamKmpSharedCoreConventionPlugin"
        }
        register("featureDomain") {
            id = "app.kmp.feature-domain"
            implementationClass = "DreamKmpFeatureDomainConventionPlugin"
        }
        register("featureData") {
            id = "app.kmp.feature-data"
            implementationClass = "DreamKmpFeatureDataConventionPlugin"
        }
        register("featurePresentation") {
            id = "app.kmp.feature-presentation"
            implementationClass = "DreamKmpFeaturePresentationConventionPlugin"
        }
        register("featureApi") {
            id = "app.kmp.feature-api"
            implementationClass = "DreamKmpFeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = "app.kmp.feature-impl"
            implementationClass = "DreamKmpFeatureImplConventionPlugin"
        }
    }
}
