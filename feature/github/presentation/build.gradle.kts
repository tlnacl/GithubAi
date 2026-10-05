plugins { id("codex.android-compose"); id("codex.hilt"); alias(libs.plugins.kotlin.serialization) }
android { namespace = "com.tlnacl.githubcompose.feature.github.presentation" }
dependencies {
    implementation(project(":feature:github:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.turbine)
    testImplementation(libs.assertk)
    testRuntimeOnly(libs.junit.launcher)
}
