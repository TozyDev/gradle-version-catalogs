@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    signing
    alias(libs.plugins.com.gradle.plugin.publish)
    id("version-catalog-extensions-generator")
}

val versionCatalogElements: Configuration by configurations.creating {
    isCanBeConsumed = false
}

val catalogSuffix = "-catalog"
val catalogProjects = rootProject.subprojects.filter { it.name.endsWith(catalogSuffix) }

dependencies {
    for (catalogProject in catalogProjects) {
        versionCatalogElements(project(catalogProject.path, configuration = "versionCatalogElements"))
    }
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

val writeCatalogs by tasks.registering(WriteProperties::class)
val collectVersionCatalogs by tasks.registering

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    validatePlugins {
        enableStricterValidation = true
    }
}

afterEvaluate {
    val generatedKotlinDir = layout.buildDirectory.dir("generated/sources/kotlin")
    kotlin.sourceSets.main {
        kotlin.srcDirs(generatedKotlinDir)
    }

    tasks {
        withType<KotlinCompile> {
            dependsOn(generateVersionCatalogExtensions)
        }

        jar {
            dependsOn(writeCatalogs)
            metaInf {
                from(writeCatalogs.flatMap { it.destinationFile })
            }
        }

        named("sourcesJar") {
            mustRunAfter(generateVersionCatalogExtensions)
        }

        writeCatalogs {
            destinationFile = temporaryDir.resolve("version-catalogs.properties")
            property("version", this@afterEvaluate.version)
            val catalogs = catalogProjects.joinToString(",") { it.name.removeSuffix(catalogSuffix) }
            property("catalogs", catalogs)
            property("group", this@afterEvaluate.group)
        }

        collectVersionCatalogs {
            group = "version catalogs"
            inputs.files(versionCatalogElements)
            doFirst {
                for (artifact in versionCatalogElements.resolvedConfiguration.resolvedArtifacts) {
                    val projectId = artifact.id.componentIdentifier as ProjectComponentIdentifier
                    val name = projectId.projectName.removeSuffix(catalogSuffix)
                    artifact.file.copyTo(temporaryDir.resolve("$name.toml"), overwrite = true)
                }
            }
        }

        generateVersionCatalogExtensions {
            group = "version catalogs"
            dependsOn(collectVersionCatalogs)

            versionCatalogs = collectVersionCatalogs.map { it.temporaryDir }
            packageName = "io.github.tozydev.versioncatalogs.dsl"
            outputDir = generatedKotlinDir
        }
    }
}
