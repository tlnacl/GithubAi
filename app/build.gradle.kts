plugins {
    alias(libs.plugins.kover)
    id("codex.android-application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("codex.hilt")
}
android {
    namespace = "com.tlnacl.githubcompose"
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "com.tlnacl.githubcompose"
        versionCode = 1
        versionName = "1.0"
    }
    flavorDimensions += "environment"
    productFlavors {
        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            // Replace with the QA backend URL when it is available. Keep the trailing slash.
            buildConfigField("String", "API_BASE_URL", "\"https://api.github.com/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.github.com/\"")
        }
    }
}
dependencies {
    implementation(project(":core:design-system"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.turbine)
    testImplementation(libs.assertk)
    testRuntimeOnly(libs.junit.launcher)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
