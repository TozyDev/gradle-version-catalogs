package io.github.tozydev.versioncatalogs.plugins

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.util.*

@Suppress("unused")
abstract class VersionCatalogsBindingPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val versionCatalogsProperties = readVersionCatalogsProperties()
        val version: String by versionCatalogsProperties
        val group: String by versionCatalogsProperties
        val catalogs = (versionCatalogsProperties["catalogs"] as String).split(",")
        settings.dependencyResolutionManagement {
            @Suppress("UnstableApiUsage")
            repositories {
                mavenCentral()
            }
            versionCatalogs {
                for (catalog in catalogs) {
                    create("${catalog}Libs") {
                        from("$group:$catalog$CATALOG_ARTIFACT_SUFFIX:$version")
                    }
                }
            }
        }
    }

    private fun readVersionCatalogsProperties() = Properties().apply {
        VersionCatalogsBindingPlugin::class.java.getResourceAsStream(VERSION_CATALOGS_PROPERTIES_PATH).use {
            load(it)
        }
    }

    companion object {
        private const val VERSION_CATALOGS_PROPERTIES_PATH = "/META-INF/version-catalogs.properties"
        private const val CATALOG_ARTIFACT_SUFFIX = "-catalog"
    }
}
