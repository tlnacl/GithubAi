# GithubCompose in Codex

Migrated from `~/Codes/github/GithubCompose`. The source project is preserved.

Open this folder in Android Studio, sync Gradle, and run `app`. Uses AGP 9.4.1, Gradle 9.6.0, and Android SDK 36. Java 25 is supported as the Gradle JVM; compilation uses the installed JDK 17 toolchain. The Gradle wrapper is included.

## Features

Browse public GitHub users, open a user profile and repositories, view repository counts, open profile/blog/repository links, and navigate back. Network failures show localized messages and Retry. Missing blogs show a dialog.

## Architecture

- `app`: Android entry points, NavHost assembly, and GitHub feature code; original application ID `com.tlnacl.githubcompose`.
  Feature code is organized into `feature/github/domain`, `data`, and `presentation` packages within this module, with its unit tests in `app/src/test`.
- Shared app utilities live in `app` under `core/domain`, `core/data`, and `core/presentation` packages (Result/errors, Ktor client/safe calls, UiText and event collection).
- `core:design-system`: shared theme and Compose components, independent of app business logic.
- `design-demo`: standalone component gallery with light/dark themes, color and typography samples, avatar placeholder, loading indicator, and enabled/disabled Material buttons.
- `build-logic`: shared Android application/library, Compose and Hilt convention plugins.

The remaining Android skills live in `.agents/skills`; `AGENTS.md` applies them to future changes. Feature layers use packages inside `app` rather than separate Gradle modules.
The app uses public, unauthenticated GitHub endpoints, so GitHub rate limits apply. Like the old app, it displays the first API page.

## Validation

Both debug APKs build on AGP 9.4.1 and Gradle 9.6.0. Unit tests cover GitHub data/ViewModel behavior and gallery theme restoration/button events. Device/emulator UI checks have not been run.

```sh
./gradlew :app:assembleDebug :design-demo:assembleDebug
./gradlew :app:testDebugUnitTest :design-demo:testDebugUnitTest
```

The migration raises the minimum Android version from 21 to 24 to match the Codex starter and current UI dependencies, and targets SDK 36.

## Designer review

Run the `design-demo` configuration in Android Studio, or install
`design-demo/build/outputs/apk/debug/design-demo-debug.apk` on an Android device.
Its application ID differs from the product app, so both can be installed together.
The gallery uses local samples and requires no GitHub connection or account.
Add reusable components from Figma to `core/design-system` and their examples to
`design-demo`; the product and gallery then use the same implementations.
The current gallery demonstrates the existing components, not a new Figma design.

## Kotlin formatting

[ktlint](https://github.com/ktlint/ktlint) is pinned in the version catalog.
Both tasks include Kotlin source, tests, and Gradle scripts (including build-logic),
and exclude generated files and local skill files. Compose function names follow
the `@Composable` convention.

```sh
./gradlew ktlintFormat
./gradlew ktlintCheck
```

The root `check` task also runs `ktlintCheck`.

## Unit test coverage

[Kover](https://github.com/Kotlin/kotlinx-kover) measures debug unit test coverage
across `app`, `design-demo`, and `core:design-system`. Run:

```sh
./gradlew :koverHtmlReportCoverage :koverXmlReportCoverage
```

Open `build/reports/kover/htmlCoverage/index.html` for the combined report.
The XML report is in `build/reports/kover/reportCoverage.xml`. Reports for individual
modules are available through their corresponding Kover tasks.

Only view rendering is excluded: declarations annotated with `@Composable`,
the two `MainActivity` classes and their generated view code, Compose singleton
classes, and the UI navigation graph. ViewModels, state, actions/events, domain,
data, typed routes, and other app logic remain included. UI/device tests are not
part of this host unit test report. No coverage threshold is enforced.
