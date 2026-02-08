## ADDED Requirements

### Requirement: Workout history list
The system SHALL display a chronological list of all completed workouts, grouped by month, most recent first.

#### Scenario: View history
- **WHEN** user navigates to the History tab
- **THEN** workouts are grouped by month (e.g., "2月") with a workout count header (e.g., "3 workouts"), each workout card showing: workout name, date and time, exercises with "Sets" count and "Best set" columns, and a footer with duration, total volume (kg), and PR count

#### Scenario: Workout card exercise list
- **WHEN** a workout card is displayed in history
- **THEN** each exercise row shows: set count × exercise name (e.g., "5 × Pull Up"), and the best set for that exercise (e.g., "+0 kg × 5", "56 kg × 6")

### Requirement: Workout detail view
The system SHALL allow the user to view the full details of a completed workout.

#### Scenario: Open workout detail
- **WHEN** user taps a workout in the history list
- **THEN** system displays all exercises, sets (weight, reps, type, RPE), notes, and workout-level notes

### Requirement: Calendar view
The system SHALL display a calendar as a modal dialog showing which days the user worked out.

#### Scenario: Open calendar
- **WHEN** user taps the calendar icon in the History tab
- **THEN** a modal calendar dialog appears showing multiple months with green checkmarks on days the user worked out

#### Scenario: Tap a calendar day
- **WHEN** user taps a highlighted day on the calendar and dismisses the dialog
- **THEN** the history list scrolls to the workout(s) for that day

#### Scenario: Calendar spans months
- **WHEN** the calendar dialog is open
- **THEN** it displays a continuous scrollable view of months (not limited to the current month)

### Requirement: Search and filter history
The system SHALL allow the user to search workout history by exercise name or workout name.

#### Scenario: Search by exercise
- **WHEN** user searches for "Squat" in history
- **THEN** all workouts containing any squat exercise are displayed

### Requirement: Delete a workout from history
The system SHALL allow the user to delete a completed workout from history.

#### Scenario: Delete workout
- **WHEN** user selects "Delete" on a workout in history
- **THEN** system shows a confirmation dialog; on confirm, the workout and all its sets are permanently deleted and PR records are recalculated
