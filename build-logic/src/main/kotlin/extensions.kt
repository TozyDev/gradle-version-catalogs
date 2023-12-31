import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun Project.prop(key: String): Provider<String> = providers.gradleProperty(key)
