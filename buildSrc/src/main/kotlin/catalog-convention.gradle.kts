plugins {
    `version-catalog`
    `maven-publish`
    signing
}

catalog {
    versionCatalog {
        from(files("libs.versions.toml"))
    }
}

val maven by publishing.publications.creating(MavenPublication::class) {
    from(components.versionCatalog)
    configurePom()
}

signing {
    sign(maven)
    useInMemoryPgpKeys()
}
