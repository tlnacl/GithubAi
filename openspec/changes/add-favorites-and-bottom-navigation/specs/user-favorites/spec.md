# Spec Delta

## Purpose

Let users save GitHub accounts locally and revisit them from a dedicated Favorite list across app restarts.

## ADDED Requirements

### Requirement: Favorite toggle on user rows
Every user row in Home and Favorite SHALL expose a heart indicating whether the user is saved. Activating the heart SHALL toggle the saved state without opening repository details.

#### Scenario: Save from search results
- **WHEN** the user activates an outline heart in Home search results
- **THEN** the account is saved and its heart becomes filled after a successful write

#### Scenario: Remove a saved user
- **WHEN** the user activates a filled heart in either list
- **THEN** the account is removed from Favorite and any visible Home heart becomes outlined

### Requirement: Persistent offline favorites
Saved login, avatar URL, and profile URL SHALL survive app restarts. Listing, adding, and removing favorites SHALL not require network access. Each account SHALL appear at most once.

#### Scenario: Restart offline
- **WHEN** the app restarts without a network connection after accounts were saved
- **THEN** Favorite displays their saved logins and profile URLs without a GitHub request
- **AND** avatar images use the existing cache or placeholder when unavailable

#### Scenario: Repeated save
- **WHEN** the same account is saved more than once
- **THEN** Favorite contains one entry for that account

### Requirement: Favorite list behavior
Favorite SHALL display saved users in ascending login order, show an empty-state message when none are saved, and open the existing repository details when a row is selected.

#### Scenario: Empty list
- **WHEN** no accounts are saved or the last favorite is removed
- **THEN** Favorite shows a message explaining that no users have been favorited

#### Scenario: Open favorite details
- **WHEN** a saved user row is selected
- **THEN** the existing profile and repository screen opens for that login

### Requirement: Shared saved state
Home and Favorite SHALL reflect the same saved state, including Home's default list and search results, without requiring a manual refresh.

#### Scenario: Return to Home after removal
- **WHEN** a favorite is removed in Favorite and the user returns to Home
- **THEN** the corresponding visible user's heart is outlined

### Requirement: Accessible and reliable actions
Heart controls SHALL identify the user and the add or remove action to accessibility services. Storage failures SHALL show a user-facing error and preserve the last confirmed saved state.

#### Scenario: Failed save
- **WHEN** local storage rejects a save
- **THEN** an error is shown and the account remains unsaved

#### Scenario: Accessible remove action
- **WHEN** accessibility focus reaches a saved user's heart
- **THEN** its label identifies removal from favorites and the user's login
