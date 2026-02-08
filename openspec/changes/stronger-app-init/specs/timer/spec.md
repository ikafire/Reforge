## ADDED Requirements

### Requirement: Auto-start rest timer on set completion
The system SHALL automatically start a rest timer when the user completes a set.

#### Scenario: Complete a set
- **WHEN** user taps the check button to complete a set
- **THEN** the rest timer starts counting down from the configured rest duration and a timer banner appears in the workout screen

### Requirement: Configurable rest duration per exercise
The system SHALL allow the user to configure a default rest timer duration per exercise.

#### Scenario: Set rest duration for bench press
- **WHEN** user sets the rest timer for "Bench Press" to 3 minutes
- **THEN** all future sets of Bench Press start a 3-minute countdown on completion

#### Scenario: Default rest duration
- **WHEN** an exercise has no custom rest duration configured
- **THEN** the system uses a global default rest duration (configurable in settings, initially 90 seconds)

### Requirement: Timer controls
The system SHALL provide controls to extend, reduce, or skip the rest timer.

#### Scenario: Extend timer
- **WHEN** user taps "+30s" during a rest timer countdown
- **THEN** 30 seconds are added to the remaining time

#### Scenario: Skip timer
- **WHEN** user taps "Skip" during a rest timer countdown
- **THEN** the timer stops and disappears immediately

### Requirement: Timer notification
The system SHALL show a system notification when the rest timer is running, and alert the user when the timer reaches zero.

#### Scenario: Timer in background
- **WHEN** the rest timer is running and the user leaves the app
- **THEN** a persistent notification shows the remaining time

#### Scenario: Timer complete
- **WHEN** the rest timer reaches zero
- **THEN** a notification with sound/vibration alerts the user that rest is over

### Requirement: Log set from notification
The system SHALL allow the user to log their next set directly from the timer notification without opening the app.

#### Scenario: Quick log from notification
- **WHEN** user taps "Log Set" on the timer notification
- **THEN** the next set is marked as completed with the pre-filled weight and reps values
