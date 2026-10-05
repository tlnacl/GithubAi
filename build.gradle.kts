// AGP also uses JavaPoet; keep the buildscript classpath compatible with Hilt.
buildscript { dependencies { classpath(libs.javapoet) } }

plugins {
    alias(libs.plugins.kover)
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

val ktlint = configurations.create("ktlint") {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    add(ktlint.name, variantOf(libs.ktlint.cli) { classifier("all") })
}

val kotlinSources = listOf(
    "**/*.kt",
    "**/*.kts",
    "!**/build/**",
    "!**/.gradle/**",
    "!**/.kotlin/**",
    "!**/.agents/**",
    "!**/.codex/**",
)

val ktlintCheck = tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Check Kotlin sources and Gradle scripts throughout the repository."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    args(kotlinSources)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Format Kotlin sources and Gradle scripts throughout the repository."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    args("--format")
    args(kotlinSources)
}

tasks.named("check") {
    dependsOn(ktlintCheck)
}

// Keep exclusions limited to view rendering; ViewModels and other logic stay measured.
allprojects {
    plugins.withId("org.jetbrains.kotlinx.kover") {
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            currentProject {
                createVariant("coverage") {
                    if (project != rootProject) add("debug")
                }
            }
            reports {
                filters {
                    excludes {
                        annotatedBy("androidx.compose.runtime.Composable")
                        classes(
                            "com.tlnacl.githubcompose.MainActivity*",
                            "com.tlnacl.githubcompose.Hilt_MainActivity*",
                            "*ComposableSingletons${'$'}*",
                            "com.tlnacl.githubcompose.feature.github.presentation.GithubNavigationKt",
                            "com.tlnacl.githubcompose.designdemo.MainActivity*",
                        )
                    }
                }
            }
        }
    }
}

dependencies {
    kover(project(":app"))
    kover(project(":design-demo"))
    kover(project(":core:design-system"))
}
