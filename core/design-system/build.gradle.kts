plugins {
    alias(libs.plugins.kover)
    id("codex.android-compose")
}
android { namespace = "com.tlnacl.githubcompose.core.designsystem" }
dependencies { implementation(libs.coil.compose) }
