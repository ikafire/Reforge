## ADDED Requirements

### Requirement: Log bodyweight
The system SHALL allow the user to log their bodyweight with a date on the Measure tab.

#### Scenario: Log today's weight
- **WHEN** user taps "Weight" on the Measure tab and enters a value
- **THEN** the value is saved for today's date and the most recent value is shown below the "Weight" label (e.g., "65.2 kg")

#### Scenario: Edit a past entry
- **WHEN** user taps a past weight entry and changes the value
- **THEN** the entry updates and the chart reflects the change

### Requirement: Log body fat percentage
The system SHALL allow the user to log body fat percentage with a date.

#### Scenario: Log body fat
- **WHEN** user taps "Body fat percentage" on the Measure tab and enters a value
- **THEN** the value is saved for today's date and displayed in the body fat history chart

### Requirement: Log caloric intake
The system SHALL allow the user to log daily caloric intake.

#### Scenario: Log calories
- **WHEN** user taps "Caloric intake" on the Measure tab and enters a value
- **THEN** the value is saved for today's date

### Requirement: Log body part measurements
The system SHALL allow the user to log body part measurements: neck, shoulders, chest, left bicep, right bicep, left forearm, right forearm, waist, hips, left thigh, right thigh, left calf, right calf.

#### Scenario: Log chest measurement
- **WHEN** user taps "Chest" under the "Body part" section and enters a measurement value
- **THEN** the value is saved for today's date with the user's preferred unit (cm or inches)

### Requirement: Measure tab layout
The system SHALL display the Measure tab with vitals at the top (Weight, Body fat percentage, Caloric intake) followed by a "Body part" section listing all body part measurements.

#### Scenario: View Measure tab
- **WHEN** user navigates to the Measure tab
- **THEN** the screen displays: Weight (with most recent value), Body fat percentage, Caloric intake, then a "Body part" heading followed by: Neck, Shoulders, Chest, Left bicep, Right bicep, Left forearm, Right forearm, Waist, Hips, Left thigh, Right thigh, Left calf, Right calf

### Requirement: Body measurement charts
The system SHALL display line charts showing each measurement type over time.

#### Scenario: View weight trend
- **WHEN** user taps into the bodyweight section
- **THEN** a line chart displays bodyweight values on the Y-axis and dates on the X-axis

#### Scenario: View measurement trend
- **WHEN** user taps into a body part measurement (e.g., "Left bicep")
- **THEN** a line chart shows that measurement's values over time

### Requirement: Delete body measurement entries
The system SHALL allow the user to delete individual body measurement entries.

#### Scenario: Delete a weight entry
- **WHEN** user long-presses a weight entry and selects "Delete"
- **THEN** the entry is removed and the chart updates
