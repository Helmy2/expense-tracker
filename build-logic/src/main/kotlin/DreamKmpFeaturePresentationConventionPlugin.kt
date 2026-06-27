import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DreamKmpFeaturePresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.kmp.compose")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonTest.dependencies {
                    implementation(libs.turbine)
                }
            }
        }
    }
}
