import org.gradle.api.artifacts.VersionCatalogsExtension
plugins { id("com.google.dagger.hilt.android"); id("com.google.devtools.ksp") }
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    "implementation"(catalog.findLibrary("hilt-android").get())
    "ksp"(catalog.findLibrary("hilt-compiler").get())
}
