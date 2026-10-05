import org.gradle.api.artifacts.VersionCatalogsExtension
plugins {
    id("codex.android-library")
    id("org.jetbrains.kotlin.plugin.compose")
}
android { buildFeatures { compose = true } }
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    "implementation"(platform(catalog.findLibrary("androidx-compose-bom").get()))
    "implementation"(catalog.findLibrary("androidx-compose-material3").get())
    "implementation"(catalog.findLibrary("androidx-compose-ui-tooling-preview").get())
    "debugImplementation"(catalog.findLibrary("androidx-compose-ui-tooling").get())
}
