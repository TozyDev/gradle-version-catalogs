plugins {
    `version-catalog`
}

catalog {
    versionCatalog {
        from(files("libs.versions.toml"))
    }
}

val maven by publishing.publications.creating(MavenPublication::class) {
    from(components["versionCatalog"])
}

signing {
    sign(maven)
}
