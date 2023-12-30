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

include("gradle-plugin")

val catalogDirs = "catalogs"
file(catalogDirs)
    .walkTopDown()
    .maxDepth(1)
    .filter { it.isDirectory }
    .filterNot { it.name == catalogDirs }
    .forEach { file ->
        val subprojectName = "${file.name}-catalog"
        include(subprojectName)
        project(":$subprojectName").apply {
            projectDir = file
            buildFileName = "../build.gradle.kts"
        }
    }
