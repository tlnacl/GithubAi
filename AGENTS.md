# Android project guidance

Read and apply the relevant instructions in `.agents/skills/` before editing Android code.

Android modules use AGP 9 built-in Kotlin; do not apply `org.jetbrains.kotlin.android`.

This project uses Hilt, Ktor with Kotlin serialization, typed Compose Navigation, and Material 3.
Keep domain code independent of Android APIs. Feature code lives in `app` under feature-specific `data`, `domain`, and `presentation` packages; do not create separate feature Gradle modules by default. Data implementations bind to domain interfaces through Hilt.
Each screen uses State/Action/Event, StateFlow, Channel events, and Root/Screen composables in the same file.
Collect state with lifecycle awareness. Preserve cancellation and convert expected failures to typed Result errors.
Shared app utilities live in `app/core` packages. Keep `:core:design-system` independent of app/domain/data code; `:app` and `:design-demo` both consume it. The demo app is a standalone component gallery.
Use version catalog dependencies and the convention plugins in `build-logic` for shared Gradle configuration.

Build: `./gradlew :app:assembleDebug :design-demo:assembleDebug`
Unit tests: `./gradlew :app:testDebugUnitTest :design-demo:testDebugUnitTest`

Formatting: `./gradlew ktlintFormat`
Code style check: `./gradlew ktlintCheck` (also included in root `check`)
