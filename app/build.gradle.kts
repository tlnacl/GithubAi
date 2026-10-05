plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("codex.hilt")
}
android {
    namespace = "com.tlnacl.githubcompose"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.tlnacl.githubcompose"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":feature:github:presentation"))
    implementation(project(":feature:github:data"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
