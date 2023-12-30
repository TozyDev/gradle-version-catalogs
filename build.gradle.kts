subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            pom {
                name = prop("name")
                description = this@subprojects.description
                url = prop("url")
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "TozyDev"
                        name = "Tozy Fullbuster"
                    }
                }
                scm {
                    connection = prop("scmUrl")
                    developerConnection = prop("scmUrl")
                    url = prop("url")
                }
            }
        }
    }

    extensions.configure<SigningExtension> {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey?.replace("\\n", "\n"), signingPassword)
    }
}

fun Project.prop(key: String): Provider<String> = providers.gradleProperty(key)
