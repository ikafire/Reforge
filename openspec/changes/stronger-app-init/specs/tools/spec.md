## ADDED Requirements

### Requirement: Plate calculator
The system SHALL provide a plate calculator that shows which plates to load on each side of the bar.

#### Scenario: Calculate plates for 100kg
- **WHEN** user enters a target weight of 100kg with a 20kg bar
- **THEN** system displays: "Load per side: 1×20kg + 1×10kg + 1×5kg + 1×2.5kg + 1×2.5kg" (or equivalent plate breakdown)

#### Scenario: Configurable bar weight
- **WHEN** user sets bar weight to 15kg (e.g., women's Olympic bar)
- **THEN** plate calculations use 15kg as the base

#### Scenario: Configurable available plates
- **WHEN** user configures their available plate set (e.g., no 1.25kg plates)
- **THEN** the calculator only uses plates from the configured set

### Requirement: Warm-up calculator
The system SHALL suggest warm-up sets based on a target working weight.

#### Scenario: Calculate warm-ups for 100kg bench
- **WHEN** user enters a working weight of 100kg
- **THEN** system suggests a progression of warm-up sets (e.g., bar only × 10, 50kg × 8, 70kg × 5, 85kg × 3)

### Requirement: Unit conversion
The system SHALL support both imperial (lbs) and metric (kg) units.

#### Scenario: Switch to imperial
- **WHEN** user changes unit preference from kg to lbs in settings
- **THEN** all weight displays throughout the app show values in lbs

#### Scenario: Convert all existing data
- **WHEN** user switches unit preference and confirms "Convert all data"
- **THEN** all stored weight values in the database are recalculated to the new unit (kg × 2.20462 = lbs, or lbs / 2.20462 = kg) and the conversion is irreversible within that action

### Requirement: Measurement unit support
The system SHALL support both cm and inches for body measurements.

#### Scenario: Switch measurement unit
- **WHEN** user changes measurement preference from cm to inches
- **THEN** all body measurement displays show values in inches
