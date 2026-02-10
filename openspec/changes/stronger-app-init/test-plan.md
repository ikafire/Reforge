# Test Plan

## 1. CSV Parser (core:data — unit tests)

- [x] 1.1 Parse valid CSV with standard rows → correct CsvWorkout list
- [x] 1.2 Parse empty input → returns empty list
- [x] 1.3 Parse header-only CSV → returns empty list
- [x] 1.4 Set Order "W" maps to SetType.WARMUP
- [x] 1.5 Set Order "D" maps to SetType.DROP
- [x] 1.6 Set Order "F" maps to SetType.FAILURE
- [x] 1.7 Numeric Set Order maps to SetType.WORKING
- [x] 1.8 Set Order "Note" → captured as exercise note, not a set
- [x] 1.9 Set Order "Rest Timer" → skipped entirely
- [x] 1.10 Quoted values with embedded semicolons are parsed correctly
- [x] 1.11 Escaped double-quotes ("") inside quoted values are unescaped
- [x] 1.12 Multiple exercises within one workout are grouped correctly
- [x] 1.13 Multiple workouts (different dates) produce separate CsvWorkout objects
- [x] 1.14 Missing/blank optional fields (weight, reps, RPE, distance) → null

## 2. Exercise Name Matching (core:data — unit tests)

- [x] 2.1 Exact case-insensitive match returns the library exercise
- [x] 2.2 Parenthetical equipment stripped: "Bench Press (Barbell)" matches "Bench Press"
- [x] 2.3 Substring containment match: "Squat" matches "Barbell Squat"
- [x] 2.4 No match → returns null (importer should create custom exercise)

## 3. Plate Calculator (feature:settings — unit tests)

- [x] 3.1 100 kg target, 20 kg bar → correct plates per side
- [x] 3.2 20 kg target, 20 kg bar → empty plate list (bar weight only)
- [x] 3.3 Target less than bar weight → empty plate list
- [x] 3.4 Odd weight that can't be exactly reached → closest achievable returned
- [x] 3.5 Zero target → empty plate list

## 4. Warm-Up Calculator (feature:settings — unit tests)

- [x] 4.1 100 kg working, 20 kg bar → 6 warm-up sets at correct percentages
- [x] 4.2 Working weight ≤ bar → empty list
- [x] 4.3 Each warm-up weight is rounded to nearest 2.5 kg increment
- [x] 4.4 Warm-up weight never goes below bar weight
- [x] 4.5 Last set (100%) has 0 reps (working set marker)

## 5. 1RM Calculation — Epley Formula (feature:exercises — unit tests)

- [x] 5.1 Single rep (reps=1) → 1RM = weight
- [x] 5.2 Multiple reps → 1RM = weight × (1 + reps/30)
- [x] 5.3 Zero weight → returns 0
- [x] 5.4 Zero reps → returns 0
- [x] 5.5 Null weight → returns 0
- [x] 5.6 Uses effectiveWeight when available instead of raw weight

## 6. PR History Builder (feature:exercises — unit tests)

- [x] 6.1 Empty history → empty PR list
- [x] 6.2 Single workout → PRs generated for weight, reps, volume
- [x] 6.3 Progressive workouts → only new PRs recorded when records broken
- [x] 6.4 Same records repeated → no duplicate PRs
- [x] 6.5 PR list sorted by date descending

## 7. Unit Conversion Factors (feature:settings — unit tests)

- [x] 7.1 KG → LBS factor is ≈ 2.20462
- [x] 7.2 LBS → KG factor is ≈ 1/2.20462
- [x] 7.3 CM → IN factor is ≈ 1/2.54
- [x] 7.4 IN → CM factor is ≈ 2.54

## 8. Workout Volume Calculation (feature:history — unit tests)

- [ ] 8.1 Total volume = sum(weight × reps) for completed sets only
- [ ] 8.2 Sets with null weight or null reps → contribute 0 to volume
- [ ] 8.3 Incomplete sets are excluded from volume

> Skipped: volume calculation is inline in ViewModel; would need extensive ViewModel mocking with repository setup for minimal additional coverage.

## 9. WorkoutRepository Mapper (core:data — unit tests)

- [x] 9.1 WorkoutEntity → Workout domain model maps all fields correctly
- [x] 9.2 Workout → WorkoutEntity maps Instant to epoch millis correctly
- [x] 9.3 WorkoutExerciseEntity ↔ WorkoutExercise maps restTimerSeconds
- [x] 9.4 WorkoutSetEntity → WorkoutSet maps SetType string to enum
- [x] 9.5 WorkoutSet → WorkoutSetEntity maps Duration to millis

## 10. CSV Exporter (core:data — unit tests)

- [ ] 10.1 Export with no workouts → header row only
- [ ] 10.2 Export with one workout → correct semicolon-delimited rows
- [ ] 10.3 Special set types (warmup/drop/failure) exported as W/D/F
- [ ] 10.4 Null optional fields exported as empty strings

> Skipped: CsvExporter calls multiple repository methods internally; would need extensive MockK setup for each test. Lower ROI vs. manual testing.

## 11. AnalyticsViewModel — Muscle Volume (feature:analytics — unit tests)

- [ ] 11.1 Primary muscle gets full volume credit
- [ ] 11.2 Secondary muscles get 50% volume credit
- [ ] 11.3 Empty workout list → empty muscle data

> Skipped: same reason as section 10 — deeply coupled to repository mocking. Suitable for integration tests.
