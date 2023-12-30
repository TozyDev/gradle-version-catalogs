@file:Suppress("UnstableApiUsage")

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
    website = prop("url")
    vcsUrl = prop("url")
    val versionCatalogsBinding by plugins.registering {
        id = prop("pluginId").get()
        implementationClass = prop("pluginImplementationClass").get()
        displayName = prop("name").get()
        description = prop("description").get()
        tags = prop("tags").get().split(",").map { it.trim() }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    val writeCatalogs by registering(WriteProperties::class)
    afterEvaluate {
        writeCatalogs.configure {
            destinationFile = temporaryDir.resolve("version-catalogs.properties")
            property("version", this@afterEvaluate.version)
            val catalogSuffix = "-catalog"
            val catalogProjects = rootProject.subprojects.filter { it.name.endsWith(catalogSuffix) }
            val catalogs = catalogProjects.joinToString(",") { it.name.removeSuffix(catalogSuffix) }
            property("catalogs", catalogs)
            property("group", this@afterEvaluate.group)
        }
    }

    jar {
        dependsOn(writeCatalogs)
        metaInf {
            from(writeCatalogs.flatMap { it.destinationFile })
        }
    }

    validatePlugins {
        enableStricterValidation = true
    }
}

fun Project.prop(key: String): Provider<String> = providers.gradleProperty(key)
