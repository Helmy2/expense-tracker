import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DreamKmpFeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.kmp")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.kotlinx.coroutines.core)
                    implementation(libs.ktor.client.core)
                    implementation(libs.ktor.client.content.negotiation)
                    implementation(libs.ktor.serialization.kotlinx.json)
                    implementation(libs.ktor.client.logging)
                    implementation(libs.ktor.client.auth)
                }

                sourceSets.getByName("androidMain").dependencies {
                    implementation(libs.ktor.client.okhttp)
                }

                sourceSets.getByName("iosArm64Main").dependencies {
                    implementation(libs.ktor.client.darwin)
                }

                sourceSets.getByName("iosSimulatorArm64Main").dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}
