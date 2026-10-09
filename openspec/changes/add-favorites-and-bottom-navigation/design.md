# Design

## Context

See proposal.md for motivation and the two delta specs for behavior. The app currently has a typed Users/Repos graph in `presentation/navigation`, MVI screen folders, Hilt, and KSP through `codex.hilt`. UsersScreen renders rows inline. There is no database dependency. The app theme already accepts a dark-mode argument and follows the system by default.

## Goals / Non-Goals

**Goals:** Keep favorites as a single reactive local source of truth, fit existing package boundaries, and retain tab state without changing user search or repository loading.

**Non-Goals:** No remote synchronization, login, theme preference storage, pagination, or additional feature Gradle modules.

## Decisions

### Room-backed local source

Add Room runtime, Kotlin extensions if needed by the selected Room version, and compiler aliases to the version catalog. Apply the Room plugin for schema export in `app` using a version compatible with the existing Kotlin/KSP toolchain; keep exported schemas under `app/schemas`.

Create a version-1 database and favorite entity in the GitHub data layer. Store login as the case-insensitive unique key plus avatar and profile URLs. Normalize lookup keys so casing does not create duplicate GitHub accounts; preserve display login. Query deterministically by login. DAO operations provide Flow observations and suspend upsert/delete. Supply the database and DAO as Hilt singletons; bind a domain favorite data-source interface to the Room implementation. Room was explicitly requested; in-memory state would not persist and preferences would provide weaker structured query support.

Return typed local errors for expected storage failures, map them to UiText, and rethrow cancellation. Use explicit set-favorite commands rather than a read-then-invert transaction so repeated rapid taps cannot create duplicates. Serialize writes and disable that row's heart while its write is pending. Publish saved state from the database observation only, avoiding optimistic state rollback complexity.

### Shared reactive presentation

UsersViewModel combines its existing loaded/search state with observed favorites; it must not restart network requests when favorites change. FavoriteViewModel observes the same data source and owns its State/Action/Event. Keep the new screen in `presentation/favorites` and the Setting placeholder in `presentation/settings`. Keep domain models free of UI flags and expose saved state in presentation UI models. Handle observation failures with a visible error and retry while retaining the last confirmed data.

Extract a reusable user row into `core:design-system`, accepting plain display values, saved/pending flags, and click callbacks. It must not depend on app domain objects. Use accessible heart labels, adequate touch targets, and weighted text so long URLs do not displace the icon. Add saved/unsaved/pending examples to `design-demo`. Use targeted icon assets or a minimal dependency available to both flavors; current icons-core is QA-only and must not be relied on by shared production code.

### Top-level navigation shell

Place a Material 3 NavigationBar around the typed NavHost at the app shell. Introduce Favorite and Setting routes in the existing navigation package and retain Repos as a detail destination. Use single-top navigation with save/restore state and pop to Home for tab changes. This avoids stacking tab visits and makes Back from non-Home tabs return to Home. Save lazy-list positions with Compose saveable state; keep Home ViewModel attached to its restored navigation entry. Derive bar visibility and selection from the current destination rather than separate mutable selection state. Avoid double-applying Scaffold insets between the shell and screen Roots.

Room additions are internal; no GitHub endpoints or environment-specific debug-menu behavior changes.

## Risks / Trade-offs

- Storage failures or rapid taps could desynchronize hearts --> persist explicit commands, disable pending actions, use database Flow as truth, and expose retryable errors.
- Offline avatar files may be absent --> retain existing Coil cache/placeholder behavior; do not promise stored image bytes.
- Restored tab entries could reload or reset search --> test retained ViewModel state and saveable scroll state when switching tabs and opening details.
- New Room/KSP versions could conflict with AGP built-in Kotlin --> resolve compatible versions during dependency setup, using QA build verification without applying kotlin.android.

## Migration Plan

Create the new database at version 1 on first use; existing users have an empty favorite list. Export the initial schema and avoid destructive migration fallbacks. Future schema changes require migrations. No existing stored data needs conversion. Removing this feature later must not silently delete the database.
