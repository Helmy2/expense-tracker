import org.gradle.api.Plugin
import org.gradle.api.Project

class DreamKmpFeatureDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.kmp")
        }
    }
}
