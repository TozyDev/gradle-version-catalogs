package io.github.tozydev.versioncatalogs.buildlogic.tasks

import io.github.tozydev.versioncatalogs.buildlogic.utils.VersionCatalogExtensionWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import kotlin.io.path.createDirectories

@CacheableTask
abstract class GenerateVersionCatalogExtensions : DefaultTask() {
    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionCatalogs: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDir = outputDir.get().asFile.toPath()
        outputDir.createDirectories()

        val versionCatalogFiles = versionCatalogs.get().asFileTree.matching { include("*.toml") }
        val packageName = packageName.get()
        for (file in versionCatalogFiles) {
            VersionCatalogExtensionWriter(file.toPath(), packageName).write(outputDir)
        }
    }
}
