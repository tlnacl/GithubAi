plugins {
    alias(libs.plugins.room)
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 1
        versionName = "1.0"
    }
    flavorDimensions += "environment"
    productFlavors {
        create("qa") {
            dimension = "environment"
            minSdk = 26
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
room { schemaDirectory("$projectDir/schemas") }

dependencies {
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.timber)
    // Tapadoo uses Material icons but omits their runtime dependency.
    "qaImplementation"(libs.androidx.compose.material.icons.core)
    // JitPack publishes classified AARs; select the active menu for all QA build types.
    "qaImplementation"(libs.tapadoo.debug.menu) {
        artifact {
            classifier = "debug"
            type = "aar"
            extension = "aar"
        }
    }
    implementation(project(":core:design-system"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
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
