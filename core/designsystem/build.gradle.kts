plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.riztech.shopkart.designsystem"
    compileSdk = 35

    defaultConfig { minSdk = 24 }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { jvmToolchain(17) }
}

dependencies {
    // The design system knows about the domain's value types (Money) so that
    // formatting lives in one place. It knows nothing about data or features.
    api(projects.core.domain)

    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.material3)
    api(libs.compose.material.icons)
    api(libs.coil.compose)
    debugApi(libs.compose.ui.tooling)
    api(libs.compose.ui.tooling.preview)
}
