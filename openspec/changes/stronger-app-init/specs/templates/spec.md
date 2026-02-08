## ADDED Requirements

### Requirement: Create workout templates (unlimited)
The system SHALL allow the user to create an unlimited number of workout templates.

#### Scenario: Create template
- **WHEN** user taps "Create Template" and adds exercises with target sets and reps
- **THEN** the template is saved and available for starting workouts

#### Scenario: No template limit
- **WHEN** user has 50 templates
- **THEN** user can create more with no restriction

### Requirement: Template folders
The system SHALL allow the user to organize templates into named folders.

#### Scenario: Create a folder
- **WHEN** user creates a folder named "PPL Split"
- **THEN** the folder appears in the template list and templates can be moved into it

#### Scenario: Templates without a folder
- **WHEN** a template is not assigned to any folder
- **THEN** it appears in a top-level "Unfiled" section

### Requirement: Edit templates
The system SHALL allow the user to edit a template's name, exercises, target sets/reps, and exercise order.

#### Scenario: Add exercise to template
- **WHEN** user edits a template and adds a new exercise
- **THEN** the exercise is appended to the template with default target sets

#### Scenario: Reorder exercises in template
- **WHEN** user drags an exercise to a new position in the template editor
- **THEN** the exercise order updates and is preserved

### Requirement: Duplicate templates
The system SHALL allow the user to duplicate an existing template.

#### Scenario: Duplicate template
- **WHEN** user selects "Duplicate" on a template
- **THEN** a copy is created with the name "<original name> (copy)" in the same folder

### Requirement: Delete templates
The system SHALL allow the user to delete templates.

#### Scenario: Delete template
- **WHEN** user deletes a template
- **THEN** the template is removed; existing workout history that was started from this template is NOT affected

### Requirement: Template preview screen
The system SHALL display a template preview screen when a template is tapped, showing all exercises with set counts, muscle groups, exercise icons, and a "Last performed" timestamp.

#### Scenario: View template preview
- **WHEN** user taps a template in the Workout tab's template list
- **THEN** a preview screen displays: template name, "Last performed: X ago", exercise list showing set count × exercise name (e.g., "5 × Pull Up") with muscle group and exercise icon, and a "START WORKOUT" button at the bottom

### Requirement: Start workout from template
The system SHALL allow starting a new workout session from any template via the template preview screen.

#### Scenario: Start from template
- **WHEN** user taps "START WORKOUT" on the template preview screen
- **THEN** a new workout is created with the template's exercises and target sets pre-loaded, with weights pre-filled from the most recent workout of each exercise

### Requirement: Superset configuration in templates
The system SHALL allow exercises to be grouped as supersets within a template.

#### Scenario: Configure superset in template
- **WHEN** user groups exercises in the template editor
- **THEN** the superset grouping carries over when starting a workout from this template
