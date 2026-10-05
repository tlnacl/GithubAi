plugins { id("codex.android-library"); id("codex.hilt") }
android { namespace = "com.tlnacl.githubcompose.core.data" }
dependencies {
    api(project(":core:domain"))
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)
}
