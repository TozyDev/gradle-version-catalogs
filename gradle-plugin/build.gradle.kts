import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    signing
    alias(libs.plugins.com.gradle.plugin.publish)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.AZUL
    }
}

signing {
    useInMemoryPgpKeys()
}

gradlePlugin {
    plugins {
        val versionCatalogs by registering {
            id = "io.github.tozydev.version-catalogs"
            implementationClass = "io.github.tozydev.versioncatalogs.plugins.BindingPlugin"
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    validatePlugins {
        enableStricterValidation = true
    }
}
