@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-version-catalogs"

module("gradle-plugin") {
    projectDir = file(it)
}

val catalogDirs = "catalogs"
file(catalogDirs)
    .walkTopDown()
    .maxDepth(1)
    .filter { it.isDirectory }
    .filterNot { it.name == catalogDirs }
    .forEach { file ->
        module(file.name) {
            projectDir = file
            buildFileName = "../build.gradle.kts"
        }
    }

fun module(name: String, block: ProjectDescriptor.(String) -> Unit = {}) {
    val subprojectName = "${rootProject.name}-$name"
    include(subprojectName)
    project(":$subprojectName").block(name)
}
