import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

class DreamAndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<ApplicationExtension> {
                namespace = "com.expense.tracker.android"
                compileSdk = libs.versions.android.compileSdk.get().toInt()

                defaultConfig {
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    targetSdk = libs.versions.android.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            dependencies {
                add("implementation", libs.androidx.activity.compose)
                add("implementation", libs.androidx.splashscreen)
                add("implementation", libs.compose.foundation)
                add("implementation", libs.compose.uiToolingPreview)
                add("implementation", libs.kotlinx.coroutines.android)
            }
        }
    }
}
