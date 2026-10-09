# Tasks

## 1. Room storage and domain contract

- [x] 1.1 Add compatible Room dependencies/plugin and schema export through the version catalog and existing KSP configuration; verify the QA build generates the version-1 schema in app/schemas.
- [x] 1.2 Add favorite domain contract, Room entity/DAO/database, and Hilt bindings; verify DAO tests cover persisted records after closing/reopening a file-backed database, case-insensitive uniqueness, sorted observation, upsert, and removal.
- [x] 1.3 Add typed local storage errors and UiText mapping with cancellation preserved; verify data-source tests cover failed writes, failed observations, and cancellation propagation.

## 2. Reusable heart-enabled user row

- [x] 2.1 Extract a user-row component into core:design-system with independent row and heart callbacks, saved/pending states, and accessible labels; verify previews and UI interaction tests distinguish selecting a row from toggling its heart.
- [x] 2.2 Add saved, unsaved, and pending examples to design-demo, using icons available to all consumers; verify the demo build/tests and visually inspect long login/URL layout and touch targets.

## 3. Home and Favorite presentation

- [x] 3.1 Observe favorites in UsersViewModel and expose heart state without restarting searches; verify ViewModel tests cover synchronized updates, preserved search debounce/loading behavior, pending writes, rapid repeated actions, and failed saves.
- [x] 3.2 Implement Favorite State/Action/Event, ViewModel, and Root/Screen in presentation/favorites; verify unit tests cover sorted results, empty state, removal, row navigation, observation retry, and storage errors.
- [x] 3.3 Use the shared row in both screens and retain saveable list positions; verify UI tests or emulator checks cover accessible heart labels, heart clicks without navigation, and removal of the last favorite.

## 4. Navigation shell and Setting placeholder

- [x] 4.1 Add typed Favorite and Setting routes and Material 3 bottom navigation at the app shell; verify navigation tests or emulator checks cover selected labels, repeated tab selection without duplicate entries, and Back returning directly to Home.
- [x] 4.2 Add the Setting placeholder and detail navigation from both lists, with bottom navigation hidden on details; verify returning from details restores the originating tab and Home search/scroll state, and Setting contains no theme controls.
- [x] 4.3 Update navigation coverage exclusions if new graph files are introduced; verify only rendering/navigation helpers are excluded and ViewModels remain measured.

## 5. Integration validation

- [x] 5.1 Run ./gradlew :app:assembleQaDebug :app:testQaDebugUnitTest ktlintCheck and ./gradlew :design-demo:assembleDebug :design-demo:testDebugUnitTest; resolve failures without running production builds by default.
- [x] 5.2 Verify on a QA emulator/device: save from Home search, restart offline, inspect Favorite, remove a favorite, return Home, and open/back out of details from both tabs; confirm persisted favorites and synchronized hearts. Run the Room/UI instrumentation tests on QA where added, and record results in the conversation rather than commit messages.
