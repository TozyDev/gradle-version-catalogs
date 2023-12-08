import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.plugins.signing.SigningExtension

internal val SoftwareComponentContainer.versionCatalog get() = getByName("versionCatalog")

fun SigningExtension.useInMemoryPgpKeys() {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey?.replace("\\n", "\n"), signingPassword)
}
