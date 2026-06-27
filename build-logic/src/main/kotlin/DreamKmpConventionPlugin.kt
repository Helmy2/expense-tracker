import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DreamKmpConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("dev.mokkery")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
            val coroutinesAndroid = libs.kotlinx.coroutines.android
            val mokkeryCore = libs.mokkery.core
            val serializationJson = libs.kotlinx.serialization.json

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(17)

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                configureAndroid {
                    namespace = target.path
                        .trim(':')
                        .split(':')
                        .joinToString(separator = ".") { segment ->
                            segment.replace("-", "")
                        }
                        .let { "com.expense.tracker.$it" }
                    compileSdk = libs.versions.android.compileSdk.get().toInt()
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    withHostTest {}
                }

                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                )

                sourceSets.getByName("androidMain").dependencies {
                    implementation(coroutinesAndroid)
                }

                sourceSets.commonMain.dependencies {
                    implementation(serializationJson)
                }

                sourceSets.commonTest.dependencies {
                    implementation(mokkeryCore)
                }
            }
        }
    }

    private fun KotlinMultiplatformExtension.configureAndroid(
        configure: KotlinMultiplatformAndroidLibraryExtension.() -> Unit
    ) {
        (this as ExtensionAware).extensions.configure("android", configure)
    }
}
