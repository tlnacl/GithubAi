# Proposal

## Why

Users need a persistent way to revisit GitHub accounts they care about. Bottom navigation gives favorites a dedicated destination and provides a place for future app settings.

## What Changes

- Add a heart toggle to users in Home and Favorite, synchronized across both screens.
- Persist favorites locally with Room, including login, avatar URL, and profile URL, so they survive app restarts and display offline.
- Add Home, Favorite, and Setting bottom-navigation destinations. Home keeps the existing search behavior.
- Preserve Home search and scroll position when switching tabs. Hide bottom navigation on repository details and return to the originating tab when going back.
- Add a Setting placeholder; theme selection remains future work.

## Capabilities

### New Capabilities

- `user-favorites`: Persistent local favorites, heart toggles, and the Favorite list.
- `app-navigation`: Three top-level destinations, retained tab state, repository detail navigation, and the Setting placeholder.

### Modified Capabilities

None. No main capability specs exist yet.

## Impact

App presentation, typed navigation, Hilt bindings, local data storage, and version catalog dependencies change. Room runtime/compiler and schema export are added using the existing KSP setup. Reusable user-row rendering can live in the design system and be demonstrated in the component gallery. GitHub API behavior and backend configuration remain unchanged. No authentication, cloud synchronization, theme preferences, or new Gradle feature modules are introduced.
