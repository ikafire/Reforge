## ADDED Requirements

### Requirement: Per-exercise total volume added chart
The system SHALL display a chart showing total volume added (weight × reps, summed across working sets) per workout over time.

#### Scenario: View total volume added chart
- **WHEN** user navigates to the Charts tab for an exercise
- **THEN** a chart displays "Total volume added (kg)" with total volume per workout session on the Y-axis and dates on the X-axis

#### Scenario: Effective resistance in charts
- **WHEN** an exercise has a resistance profile configured
- **THEN** volume charts use effectiveWeight instead of loaded weight

### Requirement: Per-exercise best set chart
The system SHALL display a chart showing the best set (max reps at max weight) per workout over time.

#### Scenario: View best set chart
- **WHEN** user navigates to the Charts tab for an exercise
- **THEN** a chart displays "Best set (reps)" with the best set's rep count per workout over time

### Requirement: Per-exercise total reps chart
The system SHALL display a chart showing total reps per workout over time.

#### Scenario: View total reps chart
- **WHEN** user navigates to the Charts tab for an exercise
- **THEN** a chart displays "Total reps" with total reps across all sets per workout session over time

### Requirement: Per-exercise 1RM progression chart
The system SHALL display a line chart showing estimated 1RM over time for any exercise.

#### Scenario: View 1RM chart
- **WHEN** user navigates to the Charts tab for an exercise
- **THEN** a line chart displays estimated 1RM values on the Y-axis and dates on the X-axis, using the Epley formula (weight × (1 + reps/30))

### Requirement: PR detection
The system SHALL automatically detect and highlight personal records when a set is completed.

#### Scenario: New 1RM PR
- **WHEN** user completes a set that results in a higher estimated 1RM than any previous set for that exercise
- **THEN** the set's check button displays a PR indicator (star icon) and a brief PR notification is shown

#### Scenario: PR types tracked
- **WHEN** system evaluates PRs
- **THEN** it tracks: max volume added, max reps, and max weight added per exercise

### Requirement: Exercise records view
The system SHALL display all-time personal records and lifetime stats for each exercise in the Records tab.

#### Scenario: View personal records
- **WHEN** user navigates to the Records tab for an exercise
- **THEN** system displays PERSONAL RECORDS section with: "Max volume added" (weight value), "Max reps" (rep count), "Max weight added" (weight value)

#### Scenario: View lifetime stats
- **WHEN** user navigates to the Records tab for an exercise
- **THEN** system displays LIFETIME STATS section with: "Total weight added" (cumulative weight), "Total reps" (cumulative reps)

#### Scenario: View records history
- **WHEN** user taps "VIEW RECORDS HISTORY" on the Records tab
- **THEN** system displays a chronological list of when each personal record was set

### Requirement: Muscle heat map
The system SHALL display a visual body map showing training volume distribution across muscle groups over a selectable time period.

#### Scenario: View heat map for last 7 days
- **WHEN** user opens the muscle heat map and selects "Last 7 Days"
- **THEN** a body diagram highlights muscle groups by color intensity based on total sets performed, with muscles not trained shown as unlit

#### Scenario: Tap muscle group for detail
- **WHEN** user taps a highlighted muscle group on the heat map
- **THEN** system shows the exercises and total sets/volume for that muscle group in the selected period
