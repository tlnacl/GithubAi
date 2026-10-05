plugins { id("codex.android-compose") }
android { namespace = "com.tlnacl.githubcompose.core.presentation" }
dependencies {
    api(project(":core:domain"))
    implementation(libs.androidx.lifecycle.runtime.compose)
}
