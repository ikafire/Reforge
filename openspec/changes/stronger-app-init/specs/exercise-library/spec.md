## ADDED Requirements

### Requirement: Bundled exercise library
The system SHALL ship with a built-in library of 200+ exercises covering all major muscle groups and equipment types.

#### Scenario: First launch
- **WHEN** the app is launched for the first time
- **THEN** the bundled exercise library is seeded into the local database and available for browsing and selection

### Requirement: Exercise metadata
Each exercise SHALL have the following metadata: name, equipment category, primary muscle group, secondary muscle groups, and optional text instructions.

#### Scenario: View exercise details
- **WHEN** user taps on an exercise name
- **THEN** system displays the exercise detail screen with About, History, Charts, and Records tabs

### Requirement: Equipment categories
The system SHALL categorize exercises by equipment: barbell, dumbbell, cable, machine, bodyweight, cardio, and duration.

#### Scenario: Filter by equipment
- **WHEN** user selects an equipment filter in the exercise library
- **THEN** only exercises matching that equipment type are displayed

### Requirement: Muscle group categories
The system SHALL categorize exercises by muscle group: chest, back, shoulders, biceps, triceps, forearms, core, quads, hamstrings, glutes, calves, and full body.

#### Scenario: Filter by muscle group
- **WHEN** user selects a muscle group filter
- **THEN** only exercises with that primary or secondary muscle group are displayed

### Requirement: Exercise list grouped by usage frequency
The system SHALL display exercises in the Exercises tab grouped by usage frequency: "50+ times", "26-50 times", "11-25 times", "1-10 times", "Never used", with a usage count shown next to each exercise.

#### Scenario: View exercise list
- **WHEN** user navigates to the Exercises tab
- **THEN** exercises are displayed grouped by usage frequency, with the most-used group at the top, and each exercise shows its total usage count

#### Scenario: Exercise with no history
- **WHEN** an exercise has never been used in a workout
- **THEN** it appears in the "Never used" group with count 0

### Requirement: Search exercises
The system SHALL allow searching exercises by name.

#### Scenario: Search by partial name
- **WHEN** user types "bench" in the search field
- **THEN** all exercises containing "bench" in their name are displayed (case-insensitive)

### Requirement: Create custom exercises (unlimited)
The system SHALL allow the user to create an unlimited number of custom exercises with all the same metadata fields as bundled exercises.

#### Scenario: Create custom exercise
- **WHEN** user taps "Create Exercise" and fills in name, equipment, and muscle group
- **THEN** a new custom exercise is created and immediately available in the library

#### Scenario: No limit on custom exercises
- **WHEN** user has already created 100 custom exercises
- **THEN** user can still create more with no restriction

### Requirement: Edit and delete custom exercises
The system SHALL allow editing and deleting user-created custom exercises but NOT bundled exercises.

#### Scenario: Edit custom exercise
- **WHEN** user edits a custom exercise's name or metadata
- **THEN** the changes are saved and reflected in all templates and history referencing that exercise

#### Scenario: Attempt to edit bundled exercise
- **WHEN** user views a bundled exercise
- **THEN** the edit option is not available (read-only)

#### Scenario: Delete custom exercise with history
- **WHEN** user deletes a custom exercise that has workout history
- **THEN** system warns that history will retain the exercise name but the exercise will be removed from the library

### Requirement: Effective resistance profile
The system SHALL allow an exercise to have an optional resistance profile that defines a multiplier between loaded weight and effective resistance.

#### Scenario: Configure resistance profile on a machine exercise
- **WHEN** user sets a resistance profile on "Leg Press" with type "angle" and multiplier 0.71
- **THEN** all future sets for that exercise auto-compute effectiveWeight = weight × 0.71

#### Scenario: Resistance profile types
- **WHEN** user configures a resistance profile
- **THEN** they can choose from: direct (1:1), angle-based, lever-based, pulley-based, or custom multiplier

#### Scenario: No resistance profile (default)
- **WHEN** an exercise has no resistance profile configured
- **THEN** effectiveWeight equals the loaded weight (multiplier = 1.0) and no EFF. column is shown

### Requirement: Exercise types
The system SHALL support multiple exercise input types: weighted (weight + reps), bodyweight (reps only), assisted bodyweight (assistance weight + reps), duration (time), and cardio (distance + time).

#### Scenario: Log a duration exercise
- **WHEN** user logs a set for a "Plank" (duration type)
- **THEN** the set input shows a duration field instead of weight/reps

#### Scenario: Log a cardio exercise
- **WHEN** user logs a set for "Running" (cardio type)
- **THEN** the set input shows distance and duration fields
