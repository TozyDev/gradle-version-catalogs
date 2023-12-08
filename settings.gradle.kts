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

setOf("test1", "test2").forEach { name ->
    module(name) {
        projectDir = file("catalogs/$it")
        buildFileName = "../build.gradle.kts"
    }
}

fun module(name: String, block: ProjectDescriptor.(String) -> Unit = {}) {
    val subprojectName = "${rootProject.name}-$name"
    include(subprojectName)
    project(":$subprojectName").block(name)
}
