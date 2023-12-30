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

    val writeCatalogs by registering(WriteProperties::class)
    afterEvaluate {
        writeCatalogs.configure {
            destinationFile = temporaryDir.resolve("META-INF/version-catalogs.properties")
            property("version", version)
            val catalogSuffix = "-catalog"
            val catalogProjects = rootProject.subprojects.filter { it.name.endsWith(catalogSuffix) }
            val catalogs = catalogProjects.joinToString(",") { it.name.removeSuffix(catalogSuffix) }
            property("catalogs", catalogs)
        }
    }

    validatePlugins {
        enableStricterValidation = true
    }
}
