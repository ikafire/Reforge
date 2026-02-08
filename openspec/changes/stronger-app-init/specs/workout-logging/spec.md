## ADDED Requirements

### Requirement: Start a workout from scratch
The system SHALL allow the user to start a new empty workout with no pre-loaded exercises.

#### Scenario: Start empty workout
- **WHEN** user taps "Start Empty Workout" from the Workout tab
- **THEN** system creates a new active workout session with a running duration timer and no exercises

### Requirement: Start a workout from a template
The system SHALL allow the user to start a workout pre-populated from a saved template.

#### Scenario: Start from template
- **WHEN** user selects a template and taps "Start Workout"
- **THEN** system creates a new active workout session pre-populated with the template's exercises, target sets, and previous workout weights/reps

### Requirement: Only one active workout at a time
The system SHALL enforce that at most one workout session is active at any time.

#### Scenario: Attempt to start while workout is active
- **WHEN** user tries to start a new workout while one is already active
- **THEN** system prompts user to finish or discard the current workout before starting a new one

### Requirement: Add exercises to an active workout
The system SHALL allow the user to add exercises from the exercise library to an active workout.

#### Scenario: Add exercise
- **WHEN** user taps "Add Exercise" in an active workout
- **THEN** system shows the exercise library picker and the selected exercise is appended to the workout with one empty set

### Requirement: Log a set
The system SHALL allow the user to record weight, reps, and optionally RPE for each set. The set table columns SHALL be: SET | PREVIOUS | weight | REPS | checkmark.

#### Scenario: Complete a set with weight and reps
- **WHEN** user enters weight and reps for a set and taps the check button
- **THEN** the set is marked as completed with a timestamp, and the rest timer starts

#### Scenario: Complete a set with RPE
- **WHEN** user enters weight, reps, and an RPE value (6.0–10.0) and completes the set
- **THEN** the set records all three values

#### Scenario: Weight column header adapts to exercise type
- **WHEN** an exercise is a weighted bodyweight type (e.g., Pull Up with added weight)
- **THEN** the weight column header displays "(+KG)" or "(+LBS)" to indicate added weight
- **WHEN** an exercise is a standard weighted type (e.g., Bench Press)
- **THEN** the weight column header displays "KG" or "LBS"

### Requirement: Pre-fill sets from previous workout
The system SHALL pre-fill weight and reps inputs with values from the user's most recent workout of the same exercise.

#### Scenario: Previous data exists
- **WHEN** user adds an exercise they have performed before
- **THEN** the PREVIOUS column displays the previous workout's weight × reps for each set (e.g., "+0 kg × 5"), and the weight input is pre-filled with the previous value

#### Scenario: No previous data
- **WHEN** user adds an exercise they have never performed
- **THEN** weight and reps inputs are blank

### Requirement: Tag sets by type
The system SHALL allow each set to be tagged as one of: warm-up, working, failure, or drop set.

#### Scenario: Tag a set as warm-up
- **WHEN** user long-presses or taps the set number and selects "Warm-up"
- **THEN** the set displays a "W" tag and is excluded from working set statistics

#### Scenario: Default set type
- **WHEN** a new set is added
- **THEN** its type defaults to "working"

### Requirement: Superset / group exercises
The system SHALL allow the user to group two or more exercises into a superset.

#### Scenario: Create a superset
- **WHEN** user selects multiple exercises and groups them
- **THEN** the exercises are visually grouped with a shared indicator (orange border) and labeled "Superset"

#### Scenario: Log within a superset
- **WHEN** user completes a set in a superset exercise
- **THEN** the UI scrolls to the next exercise in the superset group (not the next set of the same exercise)

### Requirement: Reorder exercises mid-workout
The system SHALL allow the user to reorder exercises within an active workout via drag-and-drop.

#### Scenario: Drag exercise to new position
- **WHEN** user long-presses an exercise card and drags it
- **THEN** the exercise moves to the new position and sort orders update

### Requirement: Remove exercise from workout
The system SHALL allow the user to remove an exercise from an active workout.

#### Scenario: Remove exercise with logged sets
- **WHEN** user removes an exercise that has completed sets
- **THEN** system shows a confirmation dialog before removing

### Requirement: Add notes
The system SHALL allow notes on both the workout level and per-exercise level.

#### Scenario: Add workout note
- **WHEN** user taps the workout note area and enters text
- **THEN** the note is saved and persisted with the workout

#### Scenario: Add exercise note
- **WHEN** user taps the note area under an exercise and enters text
- **THEN** the note is saved and persisted with that exercise entry

### Requirement: Add and remove sets
The system SHALL allow the user to add additional sets to an exercise or remove existing sets.

#### Scenario: Add a set
- **WHEN** user taps "+ Add Set" under an exercise
- **THEN** a new empty set row is appended, pre-filled with the previous set's weight

#### Scenario: Remove a set
- **WHEN** user swipes a set row or selects "Remove" from the set menu
- **THEN** the set is removed and remaining sets renumber

### Requirement: Finish workout
The system SHALL allow the user to finish an active workout, saving it to history.

#### Scenario: Finish with all sets completed
- **WHEN** user taps "Finish" and all sets are completed
- **THEN** the workout is saved to history with a finish timestamp and the active session ends

#### Scenario: Finish with incomplete sets
- **WHEN** user taps "Finish" and some sets are not completed
- **THEN** system prompts whether to save incomplete sets or discard them

### Requirement: Discard workout
The system SHALL allow the user to discard an active workout, deleting all logged data.

#### Scenario: Discard with logged data
- **WHEN** user taps "Discard Workout" and has logged at least one set
- **THEN** system shows a confirmation dialog; on confirm, all workout data is deleted

### Requirement: Exercise total reps badge
The system SHALL display a total reps summary badge next to each exercise name in the active workout.

#### Scenario: Show total reps
- **WHEN** user has completed sets for an exercise
- **THEN** a badge showing the total completed reps (e.g., "25 reps") is displayed next to the exercise name

### Requirement: Inline rest timer between sets
The system SHALL display the configured rest timer duration as a divider between sets in the set table.

#### Scenario: Show rest timer divider
- **WHEN** an exercise has a configured rest timer (e.g., 1:00)
- **THEN** a rest timer divider line showing the duration is displayed between each set row

#### Scenario: Add Set button shows rest timer
- **WHEN** an exercise has a configured rest timer
- **THEN** the "Add Set" button displays the rest duration (e.g., "ADD SET (1:00)")

### Requirement: Workout duration timer
The system SHALL display a running timer showing elapsed time since the workout started.

#### Scenario: Timer starts on workout creation
- **WHEN** a new workout session is created
- **THEN** a timer starts counting up and is displayed in the top bar

#### Scenario: Timer persists across app restarts
- **WHEN** the app is killed and reopened while a workout is active
- **THEN** the timer resumes from the correct elapsed time based on the stored start timestamp
