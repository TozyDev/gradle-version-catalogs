import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.assign

internal fun MavenPublication.configurePom() {
    pom {
        name = GradleVersionCatalogs.PROJECT_NAME
        description = GradleVersionCatalogs.PROJECT_DESCRIPTION
        url = GradleVersionCatalogs.PROJECT_URL
        licenses {
            apache2()
        }
        developers {
            developer {
                id = GradleVersionCatalogs.PROJECT_AUTHOR_ID
                name = GradleVersionCatalogs.PROJECT_AUTHOR
            }
        }
        scm {
            connection = GradleVersionCatalogs.PROJECT_SCM_URL
            developerConnection = GradleVersionCatalogs.PROJECT_SCM_URL
            url = GradleVersionCatalogs.PROJECT_URL
        }
    }
}

internal fun MavenPomLicenseSpec.apache2() = license {
    name = "The Apache License, Version 2.0"
    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    distribution = "repo"
}
