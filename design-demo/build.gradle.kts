plugins {
    id("codex.android-application")
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.tlnacl.githubcompose.designdemo"
    defaultConfig {
        applicationId = "com.tlnacl.githubcompose.designdemo"
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation(project(":core:design-system"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.turbine)
    testImplementation(libs.assertk)
    testRuntimeOnly(libs.junit.launcher)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
