# GitHub Compose project guidance

Android app for browsing GitHub users, searching users, and viewing profiles and repositories. App namespace: `com.tlnacl.githubcompose`.

## Project map

| Location | Purpose |
| --- | --- |
| `app/src/main/kotlin/com/tlnacl/githubcompose/feature/` | Features organized into `data`, `domain`, and `presentation` packages |
| `app/src/main/kotlin/com/tlnacl/githubcompose/core/` | Shared app networking, errors, presentation helpers, and coroutine utilities |
| `core/design-system/` | Shared theme and reusable Compose components |
| `design-demo/` | Standalone component gallery for reviewing and sharing designs |
| `app/src/qa/`, `app/src/prod/` | Environment-specific implementations, including the QA debug menu |
| `app/src/test/` | Data and ViewModel unit tests |
| `gradle/libs.versions.toml` | Dependency and plugin versions |
| `build-logic/` | Shared Gradle convention plugins |
| `.editorconfig` | Kotlin formatting rules |
| `.agents/skills/` | Detailed Android implementation guidance |

Read only the relevant code, skill sections, and supplied design references for the task. Follow the existing design system and Figma references when provided; keep reusable components available to the demo app.

## Required skill loading

Read the corresponding `.agents/skills/<skill>/SKILL.md` before editing or reviewing that area. Load multiple skills when a change crosses layers.

| Area | Skill |
| --- | --- |
| ViewModels, State/Action/Event, UI models and mappings | `android-presentation-mvi` |
| Compose screens, components, previews, accessibility, design system | `android-compose-ui` |
| Ktor, data sources, DTOs and data mappings | `android-data-layer` |
| Hilt injection and bindings | `android-di-hilt` |
| Typed routes and navigation graphs | `android-navigation` |
| Result types, expected failures and error mapping | `android-error-handling` |
| Unit and Compose tests | `android-testing` |

## Validation

Default app validation is **QA only**:

```sh
./gradlew :app:assembleQaDebug :app:testQaDebugUnitTest
```

Run `./gradlew ktlintCheck` for Kotlin or Gradle changes. `./gradlew ktlintFormat` formats the entire repository; prefer formatting only edited files for focused changes.

Add or update meaningful tests for behavior changes. Documentation-only changes need no tests.

When changing the component gallery, also run `./gradlew :design-demo:assembleDebug :design-demo:testDebugUnitTest`. Do not run production builds or tests by default; run them when explicitly requested.

Coverage on request:

```sh
./gradlew :koverHtmlReportCoverage :koverXmlReportCoverage
```

The coverage variant currently uses `prodDebug` for `:app` and `debug` for other modules, so coverage is a deliberate exception to QA-only validation. Exclude only view rendering: composable declarations, Activities, generated UI helpers, and the UI navigation graph. Keep ViewModels and other logic measured.

## Change and Git hygiene

- git add every new file immediately after creating it. Create meaningful, modular commits at logical checkpoints — don't batch everything into one giant commit.
