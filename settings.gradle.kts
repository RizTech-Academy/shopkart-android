pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ShopKart"

// include(":app")   // not built yet
include(":core:domain")
include(":core:data")
// include(":core:designsystem")   // not built yet
// include(":feature:catalog")   // not built yet
// include(":feature:cart")   // not built yet
