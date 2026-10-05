pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

// The modules ask for a Java 17 toolchain. Without a resolver, a machine with
// only a newer JDK cannot build at all; with it, Gradle fetches 17 once.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ShopKart"

include(":app")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":feature:catalog")
include(":feature:cart")
