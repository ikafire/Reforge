## ADDED Requirements

### Requirement: Bottom navigation
The system SHALL provide a bottom navigation bar with 5 tabs matching Strong's layout: Profile, History, Workout (center), Exercises, and Measure.

#### Scenario: Navigate between tabs
- **WHEN** user taps a tab in the bottom navigation
- **THEN** the corresponding screen is displayed and the tab is highlighted as active

#### Scenario: Preserve tab state
- **WHEN** user navigates away from a tab and back
- **THEN** the tab's scroll position and state are preserved

#### Scenario: Workout tab as home
- **WHEN** the app is launched with no active workout
- **THEN** the Workout tab is selected by default, showing "Quick start" (Start Empty Workout) and the template browser

### Requirement: Workout tab layout
The system SHALL display the Workout tab with a "Quick start" section (Start Empty Workout button) at the top, followed by a "Templates" section showing all template folders and templates.

#### Scenario: Workout tab with no active workout
- **WHEN** user navigates to the Workout tab with no active workout
- **THEN** the screen shows: "Workout" heading, "Quick start" → "START AN EMPTY WORKOUT" button, "Templates" heading with add/folder/menu icons, and the template list grouped by folders

#### Scenario: Workout tab with active workout
- **WHEN** user navigates to the Workout tab while a workout is in progress
- **THEN** the active workout logging screen is displayed (full screen with minimize, timer, and Finish button)

### Requirement: Profile tab
The system SHALL provide a Profile tab showing user statistics and a dashboard.

#### Scenario: View profile
- **WHEN** user navigates to the Profile tab
- **THEN** the screen displays: user name, total workout count, and a Dashboard section with a "Workouts per week" bar chart

#### Scenario: Workouts per week chart
- **WHEN** the Dashboard is displayed
- **THEN** a bar chart shows the number of workouts per week over the last 8 weeks

### Requirement: Dark and light theme
The system SHALL support both dark and light themes following Material Design 3.

#### Scenario: Switch theme
- **WHEN** user selects "Dark", "Light", or "System" in settings
- **THEN** the app's color scheme updates immediately

#### Scenario: Default theme
- **WHEN** the app is launched for the first time
- **THEN** the theme follows the system setting (dark/light)

### Requirement: Settings screen
The system SHALL provide a settings screen accessible from the Profile tab's gear icon.

#### Scenario: Access settings
- **WHEN** user taps the settings/gear icon on the Profile tab
- **THEN** the settings screen is displayed with sections: Units, Timer Defaults, Theme, Data (import/export/backup), and About

### Requirement: Onboarding flow
The system SHALL display a first-launch onboarding flow that collects initial preferences.

#### Scenario: First launch
- **WHEN** user opens the app for the first time
- **THEN** an onboarding flow collects: preferred unit (kg/lbs), measurement unit (cm/in), and optionally offers to import from Strong CSV

#### Scenario: Skip onboarding
- **WHEN** user dismisses the onboarding
- **THEN** defaults are applied (metric) and the user lands on the Workout tab

### Requirement: Active workout persistence
The system SHALL persist an active workout across app restarts, crashes, and device reboots.

#### Scenario: App killed during workout
- **WHEN** the system kills the app while a workout is active
- **THEN** on next launch, the workout tab shows the in-progress workout with all logged sets intact and the duration timer resuming from the correct elapsed time

### Requirement: Analytics access
The system SHALL provide access to analytics (charts, heat map) from the Exercises tab.

#### Scenario: Access analytics from exercise
- **WHEN** user taps an exercise in the Exercises tab
- **THEN** the exercise detail screen opens with tabs: About | History | Charts | Records

#### Scenario: Access muscle heat map
- **WHEN** user navigates to the analytics/heat map section (from Profile or Exercises)
- **THEN** the muscle heat map and aggregate statistics are displayed
