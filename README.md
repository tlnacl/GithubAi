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

## Backend environments

The product app has `qa` and `prod` flavors. Select `qaDebug` or `prodDebug` in
Android Studio's Build Variants panel. QA uses application ID
`com.tlnacl.githubcompose.qa`, so it can be installed alongside production.
Both initially use `https://api.github.com/`. Change the QA flavor's
`API_BASE_URL` in `app/build.gradle.kts` when a separate backend is available;
keep the trailing slash. The Ktor client reads `BuildConfig.API_BASE_URL`.
The environment is selected at build time.

## QA debug menu

[Tapadoo DebugMenu](https://github.com/Tapadoo/DebugMenu) is available only in
`qaDebug` and `qaRelease`. Tap its floating wrench button to view the environment,
app version, application ID, and API base URL. Shake-to-open is disabled.
The menu reports the URL selected at build time; it does not switch backends.

The dependency and menu implementation live only in the QA flavor. Production
uses an empty composable and does not package Tapadoo DebugMenu. QA requires
Android 8.0 (API 26) to match the library; production still supports API 24.
The version catalog pins version `1.0.6`, using the published `debug` AAR classifier
to keep the menu active in QA release builds. JitPack is restricted to the
`com.github.Tapadoo` dependency group.

## Validation

Debug APKs build on AGP 9.4.1 and Gradle 9.6.0. Unit tests cover GitHub data/ViewModel behavior and gallery theme restoration/button events. Device/emulator UI checks have not been run.

```sh
./gradlew :app:assembleQaDebug :app:assembleProdDebug :design-demo:assembleDebug
./gradlew :app:testQaDebugUnitTest :app:testProdDebugUnitTest :design-demo:testDebugUnitTest
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
across `app` (`prodDebug`), `design-demo`, and `core:design-system`.
QA runs the same app tests separately using `:app:testQaDebugUnitTest`. Run:

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
