plugins { id("codex.android-library"); id("codex.hilt"); alias(libs.plugins.kotlin.serialization) }
android { namespace = "com.tlnacl.githubcompose.feature.github.data" }
dependencies {
    implementation(project(":feature:github:domain"))
    implementation(project(":core:data"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertk)
    testRuntimeOnly(libs.junit.launcher)
}
