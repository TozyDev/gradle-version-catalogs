package io.github.tozydev.versioncatalogs.buildlogic.utils

import com.squareup.kotlinpoet.*
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.tomlj.Toml
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

class VersionCatalogExtensionWriter(private val toml: Path, private val packageName: String) {
    private val catalog = toml.nameWithoutExtension
    private val catalogName = "${catalog}Libs"
    private val catalogTitleName = catalogName.replaceFirstChar { it.titlecase() }
    private val className = ClassName(packageName, "${catalogTitleName}Builder")

    fun write(kotlinSrcDir: Path) {
        fileSpec {
            addType(classSpec {
                primaryConstructor(constructorSpec {
                    addParameter(CONSTRUCTOR_BUILDER_PARAM, VERSION_CATALOG_BUILDER)
                })
                addSuperinterface(VERSION_CATALOG_BUILDER, CONSTRUCTOR_BUILDER_PARAM)
                for (versionAlias in getVersionAliases()) {
                    addProperty(mutablePropertySpec(versionAlias))
                }
            })
            addFunction(funSpec(catalogName) {
                receiver(MUTABLE_VERSION_CATALOG_CONTAINER)
                addParameter(CONFIGURE_PARAM, LambdaTypeName.get(className, returnType = UNIT))
                addStatement(CONFIGURE_STMT, catalogName, className)
            })
        }.run {
            writeTo(kotlinSrcDir)
        }
    }

    private fun getVersionAliases() = Toml.parse(toml).getTableOrEmpty("versions").keySet()

    private fun fileSpec(block: FileSpec.Builder.() -> Unit) =
        FileSpec.builder(packageName, "${catalogTitleName}VersionCatalogExtension").apply(block).build()

    private fun classSpec(block: TypeSpec.Builder.() -> Unit) =
        TypeSpec.classBuilder(className).apply(block).build()

    private fun constructorSpec(block: FunSpec.Builder.() -> Unit) =
        FunSpec.constructorBuilder().apply(block).build()

    private fun funSpec(name: String, block: FunSpec.Builder.() -> Unit) =
        FunSpec.builder(name).apply(block).build()

    private fun mutablePropertySpec(versionAlias: String) =
        PropertySpec
            .builder(versionAlias, String::class.asTypeName())
            .getter(getterSpec(versionAlias))
            .setter(setterSpec(versionAlias))
            .mutable()
            .build()

    private fun getterSpec(versionAlias: String) =
        FunSpec.getterBuilder().addStatement(GETTER_STMT, versionAlias).build()

    private fun setterSpec(versionAlias: String) =
        FunSpec.setterBuilder().addParameter(SETTER_PARAM, STRING).addStatement(SETTER_STMT, versionAlias).build()

    companion object {
        private const val CONSTRUCTOR_BUILDER_PARAM = "builder"
        private const val GETTER_STMT = "return %S"
        private const val SETTER_PARAM = "value"
        private const val SETTER_STMT = "version(`%L`, $SETTER_PARAM)"
        private const val CONFIGURE_PARAM = "block"
        private const val CONFIGURE_STMT = "named(%S).configure { %T(this).$CONFIGURE_PARAM() }"
        private val VERSION_CATALOG_BUILDER = VersionCatalogBuilder::class.asTypeName()
        private val MUTABLE_VERSION_CATALOG_CONTAINER = MutableVersionCatalogContainer::class.asTypeName()
    }
}
