pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Codex"
include(":app", ":core:domain", ":core:data", ":core:presentation", ":core:design-system",
    ":feature:github:domain", ":feature:github:data", ":feature:github:presentation")
