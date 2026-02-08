## ADDED Requirements

### Requirement: Backup database to Google Drive
The system SHALL allow the user to backup the entire local database to Google Drive's app data folder.

#### Scenario: Manual backup
- **WHEN** user taps "Backup to Google Drive" in settings
- **THEN** the system copies the Room database file, uploads it to Google Drive's app data folder with a timestamp, and shows a success confirmation

#### Scenario: Backup metadata
- **WHEN** a backup is uploaded
- **THEN** it includes metadata: app version, database schema version, backup timestamp, and workout count

### Requirement: Restore database from Google Drive
The system SHALL allow the user to restore the local database from a Google Drive backup.

#### Scenario: Restore from backup
- **WHEN** user taps "Restore from Google Drive" in settings
- **THEN** system lists available backups with timestamps and workout counts; user selects one; system closes the database, replaces it with the backup file, and reopens it

#### Scenario: Schema version mismatch
- **WHEN** user attempts to restore a backup with a newer schema version than the current app
- **THEN** system shows an error message asking the user to update the app first

#### Scenario: Restore confirmation
- **WHEN** user selects a backup to restore
- **THEN** system warns that restoring will replace ALL local data and requires confirmation

### Requirement: Google account authentication
The system SHALL require Google account sign-in for backup and restore functionality.

#### Scenario: Sign in for backup
- **WHEN** user attempts to backup or restore without being signed in
- **THEN** system initiates the Google sign-in flow

### Requirement: Strong CSV import
The system SHALL allow importing workout history from Strong's CSV export format.

#### Scenario: Import Strong CSV
- **WHEN** user selects a Strong CSV export file
- **THEN** system parses the semicolon-delimited, double-quoted CSV with columns: "Workout #", "Date", "Workout Name", "Duration (sec)", "Exercise Name", "Set Order", "Weight (kg)", "Reps", "RPE", "Distance (meters)", "Seconds", "Notes", "Workout Notes"

#### Scenario: Parse Set Order special values
- **WHEN** parsing CSV rows
- **THEN** system maps Set Order values: "W" → warm-up set, "D" → drop set, "1"/"2"/... → working sets, "Note" → exercise note (text in Notes column), "Rest Timer" → rest timer entry (duration in Seconds column, skipped for set import)

#### Scenario: Match exercise names
- **WHEN** parsing exercise names from the CSV
- **THEN** system matches against the bundled library using the Strong naming convention "Name (Equipment)" (e.g., "Bench Press (Dumbbell)", "Pull Up", "Face Pull (Cable)"), case-insensitive. Unmatched names create custom exercises automatically.

#### Scenario: Import progress feedback
- **WHEN** import is in progress
- **THEN** system shows a progress indicator with counts: "Importing workout 45/120..."

#### Scenario: Import summary
- **WHEN** import completes
- **THEN** system shows a summary: number of workouts imported, exercises matched, custom exercises created, and any rows skipped with reasons

### Requirement: CSV export
The system SHALL allow exporting all workout data as a CSV file.

#### Scenario: Export to CSV
- **WHEN** user taps "Export to CSV" in settings
- **THEN** system generates a CSV file containing all workouts, exercises, sets, and metadata, and opens the system share sheet

#### Scenario: CSV format
- **WHEN** a CSV is exported
- **THEN** it includes columns: Date, Workout Name, Exercise Name, Set Order, Weight, Reps, Distance, Seconds, Notes, Workout Notes, RPE, Set Type, Effective Weight
