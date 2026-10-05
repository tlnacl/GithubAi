# Android project guidance

Read and apply the relevant instructions in `.agents/skills/` before editing Android code.

Android modules use AGP 9 built-in Kotlin; do not apply `org.jetbrains.kotlin.android`.

This project uses Hilt, Ktor with Kotlin serialization, typed Compose Navigation, and Material 3.
Keep domain modules pure Kotlin. Data implementations bind to domain interfaces through Hilt.
Each screen uses State/Action/Event, StateFlow, Channel events, and Root/Screen composables in the same file.
Collect state with lifecycle awareness. Preserve cancellation and convert expected failures to typed Result errors.
Use version catalog dependencies and the convention plugins in `build-logic` for shared Gradle configuration.

Build: `./gradlew :app:assembleDebug`
Unit tests: `./gradlew :feature:github:data:testDebugUnitTest :feature:github:presentation:testDebugUnitTest`
