# Spec Delta

## Purpose

Provide predictable navigation between the main user list, saved favorites, and a future settings destination while retaining browsing context.

## ADDED Requirements

### Requirement: Three top-level destinations
The app SHALL start on Home and expose a bottom navigation bar with Home, Favorite, and Setting. The selected destination SHALL be visually indicated and accessible.

#### Scenario: Switch destination
- **WHEN** the user selects Favorite or Setting
- **THEN** its screen appears and the corresponding bottom navigation item is selected

#### Scenario: Select active destination
- **WHEN** the user selects the already active navigation item
- **THEN** no duplicate destination is added to the back stack

### Requirement: Preserve Home browsing context
Switching tabs SHALL retain Home's search query, loaded results, and scroll position. Existing one-second search debounce and request loading behavior SHALL remain unchanged.

#### Scenario: Return to a searched list
- **WHEN** the user searches and scrolls in Home, switches to Favorite, then returns
- **THEN** Home shows the retained query, results, and scroll position
- **AND** favorite indicators reflect current saved state

### Requirement: Repository detail navigation
Repository details SHALL hide bottom navigation. Back from details SHALL return to the originating Home or Favorite screen with its browsing context retained.

#### Scenario: Back to Favorite
- **WHEN** a user opens details from Favorite and presses Back
- **THEN** Favorite reappears with its bottom navigation item selected

#### Scenario: Back to Home
- **WHEN** a user opens details from Home and presses Back
- **THEN** Home reappears with its retained search and scroll position

### Requirement: Top-level Back behavior
Back from Favorite or Setting SHALL return to Home. Back from Home SHALL follow the normal app exit behavior without cycling through previously selected tabs.

#### Scenario: Back after switching tabs
- **WHEN** the user visits Favorite, then Setting, then presses Back
- **THEN** Home appears directly

### Requirement: Setting placeholder
Setting SHALL show a clearly labeled placeholder indicating that settings are coming later. This change SHALL not add theme controls or alter current system-following theme behavior.

#### Scenario: Visit Setting
- **WHEN** the user opens Setting
- **THEN** a placeholder appears with no theme selection controls
