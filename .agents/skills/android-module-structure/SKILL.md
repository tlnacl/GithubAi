---
name: android-module-structure
description: Module layout, dependency rules, and Gradle convention plugins for Android projects. Use this skill when setting up an Android project, deciding where a module should live, creating feature or core modules, configuring convention plugins, or managing the version catalog. Trigger on phrases like "add a module", "create a feature", "project structure", "convention plugin", "build-logic", or "where does X live".
---

# Android Module Structure

## Core Philosophy

- Organize modules by feature, then by layer when the project benefits from separate layers.
- Keep domain rules independent of Android APIs. Android UI and data implementations depend on domain contracts.
- Keep code in the feature that owns it. Move code into `:core` when multiple features genuinely share it.
- Features should not depend on one another directly. Put shared contracts and UI in appropriate core modules or expose narrow feature APIs.
- Choose module boundaries based on ownership and build needs; avoid creating modules for a single trivial class.

## Module Layout

A layered Android app may use a structure like:

```text
:app
:build-logic
:core:domain          # Shared domain models, repository contracts, errors
:core:data            # Shared data utilities, database/network setup
:core:presentation    # Shared UI utilities and presentation models
:core:design-system   # Reusable Compose components and theme
:feature:<name>:domain
:feature:<name>:data
:feature:<name>:presentation
```

A feature does not need all three layers when its scope is small. A standalone concern with meaningful complexity or a stable API (such as analytics, database, or authentication) can live in its own `:core` module.

A shared Room database can live in `:core:database`, containing the database, entities, DAOs, and migrations. Features that need it depend on that module.

## Dependency Rules

| Layer | May depend on |
|---|---|
| `presentation` | Its feature's `domain`, shared domain, presentation, and design-system modules |
| `data` | Its feature's `domain` and shared data/domain modules |
| `domain` | Shared domain contracts only; never `data` or `presentation` |
| `:app` | Feature entry points and modules needed to assemble the application |

Keep module dependency direction acyclic. Avoid making domain modules depend on Android framework APIs unless a concrete platform requirement justifies it.

## Convention Plugins (`:build-logic`)

Use convention plugins for Gradle configuration that is repeated across modules. Common examples include:

| Plugin | Purpose |
|---|---|
| `android-application` | Android app configuration |
| `android-library` | Shared Android library configuration |
| `android-feature` | Common feature library and Compose setup |
| `android-compose` | Compose compiler and common UI dependencies |
| `hilt` | Hilt plugin and annotation processor configuration |
| `room` | Room and KSP configuration |
| `kotlinx-serialization` | Kotlin serialization plugin and dependency |

Use the version catalog (`gradle/libs.versions.toml`) for plugin and dependency versions. Apply the Hilt convention/plugin and compiler to Android modules that declare Hilt annotations or modules. Keep Hilt bindings with the module that owns the implementation; Hilt collects the bindings through the app's compile dependency graph, so the app does not need a manually assembled list of DI modules.

## Common Android Libraries

| Concern | Typical choices |
|---|---|
| Dependency injection | Hilt |
| Networking | Retrofit/OkHttp or Ktor Client |
| Local database | Room |
| Preferences | DataStore |
| Navigation | Jetpack Compose Navigation |
| Serialization | Kotlin Serialization or Moshi/Gson as appropriate |
| Image loading | Coil |
| Async | Coroutines and Flow |
| Background work | WorkManager |
| Testing | JUnit, Turbine, coroutine test tools, and Compose UI testing |

Use the choices already established by the project where possible; do not add a library just because it appears in this list.

## Checklist: Adding a Feature Module

- [ ] Choose the smallest module structure that preserves clear ownership and dependency direction.
- [ ] Add modules to Gradle settings and configure their convention plugins.
- [ ] Keep feature dependencies pointed toward shared contracts, not other feature implementations.
- [ ] Put Hilt bindings in the owning Android module and ensure `:app` depends on that module.
- [ ] Extract shared code into `:core` only when multiple features need it.
