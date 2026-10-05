# GithubCompose in Codex

Migrated from `~/Codes/github/GithubCompose`. The source project is preserved.

Open this folder in Android Studio, sync Gradle, and run `app`. Uses AGP 9.4.1, Gradle 9.6.0, and Android SDK 36. Java 25 is supported as the Gradle JVM; compilation uses the installed JDK 17 toolchain. The Gradle wrapper is included.

## Features

Browse public GitHub users, open a user profile and repositories, view repository counts, open profile/blog/repository links, and navigate back. Network failures show localized messages and Retry. Missing blogs show a dialog.

## Architecture

- `app`: Android entry points, NavHost assembly, and GitHub feature code; original application ID `com.tlnacl.githubcompose`.
  Feature code is organized into `feature/github/domain`, `data`, and `presentation` packages within this module, with its unit tests in `app/src/test`.
- `core:domain`: typed Result and shared error types.
- `core:data`: configured Ktor client and safe HTTP response/exception handling.
- `core:presentation`: UiText, error components and lifecycle event collection.
- `core:design-system`: shared theme and Compose components.
- `build-logic`: shared Android library, Compose and Hilt convention plugins.

The remaining Android skills live in `.agents/skills`; `AGENTS.md` applies them to future changes. Feature layers use packages inside `app` rather than separate Gradle modules.
The app uses public, unauthenticated GitHub endpoints, so GitHub rate limits apply. Like the old app, it displays the first API page.

## Validation

Debug APK build succeeded with Android Studio’s Java 25 runtime on AGP 9.4.1 and Gradle 9.6.0; all 16 unit tests passed. Device/emulator UI checks have not been run.

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The migration raises the minimum Android version from 21 to 24 to match the Codex starter and current UI dependencies, and targets SDK 36.
