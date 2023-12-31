package io.github.tozydev.versioncatalogs.buildlogic.tasks

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.tomlj.Toml

@CacheableTask
abstract class GenerateVersionCatalogExtension : DefaultTask() {
    @get:Input
    abstract val catalog: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val catalogToml: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val catalogName = "${catalog.get()}Libs"
        val fileName = "${catalogName.replaceFirstChar { it.titlecase() }}VersionCatalogExtension"
        fileSpec(fileName) {
            val className = ClassName(packageName, fileName)
            addType(classSpec(className) {
                primaryConstructor(constructorSpec {
                    addParameter("provider", VERSION_CATALOG_BUILDER_PROVIDER_TYPE_NAME)
                })
                addSuperinterface(VERSION_CATALOG_BUILDER_PROVIDER_TYPE_NAME, CodeBlock.of("provider"))
                for (version in parseVersions()) {
                    addProperty(propertySpec(version, String::class.asTypeName()) {
                        initializer("%S", version)
                    })
                }
            })

            addProperty(propertySpec(catalogName, className) {
                receiver(MutableVersionCatalogContainer::class)
                getter(getterSpec { addStatement(GETTER_STATEMENT, className, catalogName) })
            })
        }.run {
            val outputDir = outputDir.get().asFile
            outputDir.mkdirs()
            writeTo(outputDir)
        }
    }

    private fun parseVersions() = Toml.parse(catalogToml.get().asFile.toPath()).getTableOrEmpty("versions").keySet()

    private fun fileSpec(name: String, block: FileSpec.Builder.() -> Unit) =
        FileSpec.builder(packageName.get(), name).apply(block).build()

    private fun classSpec(name: ClassName, block: TypeSpec.Builder.() -> Unit) =
        TypeSpec.classBuilder(name).apply(block).build()

    private fun constructorSpec(block: FunSpec.Builder.() -> Unit) = FunSpec.constructorBuilder().apply(block).build()

    private fun propertySpec(name: String, type: TypeName, block: PropertySpec.Builder.() -> Unit) =
        PropertySpec.builder(name, type).apply(block).build()

    private fun getterSpec(block: FunSpec.Builder.() -> Unit) = FunSpec.getterBuilder().apply(block).build()

    companion object {
        private const val GETTER_STATEMENT = "return %T(named(%S))"
        private val VERSION_CATALOG_BUILDER_PROVIDER_TYPE_NAME =
            NamedDomainObjectProvider::class.parameterizedBy(VersionCatalogBuilder::class)
    }
}
