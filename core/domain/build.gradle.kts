plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Deliberately a plain Kotlin module, not an Android library. The domain layer
// must not be able to reach for a Context, a Cursor, or a Compose type — the
// compiler enforces that here rather than a code review having to catch it.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin { jvmToolchain(17) }

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
