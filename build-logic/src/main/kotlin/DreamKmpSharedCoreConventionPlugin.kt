import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DreamKmpSharedCoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.kmp")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonTest.dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
}
