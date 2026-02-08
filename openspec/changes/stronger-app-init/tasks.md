## 1. Project Setup

- [x] 1.1 Create new Android project with Kotlin, Compose, min SDK 29
- [ ] 1.2 Configure multi-module Gradle structure (:app, :core:*, :feature:*)
- [x] 1.3 Add core dependencies (Hilt, Room, Compose Navigation, kotlinx.datetime, kotlinx.serialization)
- [x] 1.4 Set up Material Design 3 theme with dark/light support
- [x] 1.5 Create base Application class with Hilt setup
- [ ] 1.6 ✓ VALIDATE: Project builds and launches on emulator
  - Run: `./gradlew assembleDebug lintDebug`
  - Run: `adb shell am start -n io.github.ikafire.stronger/.MainActivity`
  - Verify: App launches without crash, shows "Stronger" text

## 2. Core Infrastructure

- [ ] 2.1 Create :core:common module (pure Kotlin utilities, constants)
- [ ] 2.2 Create :core:domain module (pure Kotlin models, repository interfaces, use case base)
- [ ] 2.3 Create :core:database module with Room setup and database class
- [ ] 2.4 Define Exercise entity (id, name, category, primaryMuscle, secondaryMuscles, instructions, isCustom, resistanceProfile, createdAt)
- [ ] 2.5 Define ResistanceProfile embedded class (type, multiplier, notes)
- [ ] 2.6 Define Workout entity (id, templateId, startedAt, finishedAt, notes, isActive)
- [ ] 2.7 Define WorkoutExercise entity (id, workoutId, exerciseId, sortOrder, supersetGroup, notes)
- [ ] 2.8 Define WorkoutSet entity (id, workoutExerciseId, sortOrder, type, weight, reps, distance, duration, rpe, effectiveWeight, isCompleted, completedAt)
- [ ] 2.9 Define WorkoutTemplate entity (id, name, folderId, sortOrder)
- [ ] 2.10 Define TemplateFolder entity (id, name, sortOrder)
- [ ] 2.11 Define TemplateExercise entity (id, templateId, exerciseId, sortOrder, targetSets, targetReps, supersetGroup)
- [ ] 2.12 Create :core:data module with repository implementations
- [ ] 2.13 Create :core:ui module with shared composables and theme
- [ ] 2.14 ✓ VALIDATE: All modules build, Room schema compiles
  - Run: `./gradlew :core:database:assembleDebug :core:database:kspDebugKotlin`
  - Run: `./gradlew :core:database:connectedAndroidTest` (Room DAO tests)
  - Tests: ExerciseDaoTest, WorkoutDaoTest, WorkoutSetDaoTest
  - Verify: All entities can be inserted/queried/updated/deleted

## 3. MVP: App Shell & Navigation

- [x] 3.1 Implement MainActivity with Compose content
- [ ] 3.2 Implement bottom navigation bar (Profile, History, Workout, Exercises, Measure tabs)
- [ ] 3.3 Set up Compose Navigation with type-safe routes
- [ ] 3.4 Implement tab state preservation
- [ ] 3.5 Create placeholder screens for all 5 tabs
- [ ] 3.6 Add settings navigation from Profile tab (gear icon)
- [ ] 3.7 Implement settings screen skeleton (Units, Theme sections)
- [ ] 3.8 Implement theme switching (Dark/Light/System)
- [ ] 3.9 Implement unit preference setting (kg/lbs, cm/in)
- [ ] 3.10 ✓ VALIDATE: App Shell meets specs/app-shell/spec.md
  - Run: `./gradlew :app:connectedAndroidTest --tests "*.AppShellTest*"`
  - Spec: Bottom navigation - "Navigate between tabs" scenario
  - Spec: Bottom navigation - "Preserve tab state" scenario
  - Spec: Bottom navigation - "Workout tab as home" scenario
  - Spec: Dark and light theme - "Switch theme" scenario
  - Spec: Dark and light theme - "Default theme" scenario
  - Spec: Settings screen - "Access settings" scenario
  - Tests:
    - NavigationTest: tap each tab → correct screen displayed, tab highlighted
    - TabStateTest: scroll in tab, navigate away, return → scroll preserved
    - ThemeTest: toggle dark/light → colors change, persists on restart
    - SettingsTest: gear icon → settings screen with Units, Theme sections

## 4. MVP: Exercise Library

- [ ] 4.1 Create :feature:exercises module
- [ ] 4.2 Create bundled exercises JSON asset file (~200+ exercises)
- [ ] 4.3 Implement first-launch exercise database seeding
- [ ] 4.4 Create ExerciseDao with CRUD operations and Flow queries
- [ ] 4.5 Create ExerciseRepository with domain models
- [ ] 4.6 Implement ExerciseListScreen with usage frequency grouping (50+, 26-50, 11-25, 1-10, Never used)
- [ ] 4.7 Implement exercise search by name
- [ ] 4.8 Implement equipment category filter
- [ ] 4.9 Implement muscle group filter
- [ ] 4.10 Implement ExerciseDetailScreen with About tab (name, equipment, muscles, instructions)
- [ ] 4.11 Implement create custom exercise flow (unlimited)
- [ ] 4.12 Implement edit/delete custom exercise
- [ ] 4.13 Implement resistance profile configuration (direct, angle, lever, pulley, custom multiplier)
- [ ] 4.14 Implement exercise picker dialog (for adding exercises to workouts/templates)
- [ ] 4.15 ✓ VALIDATE: Exercise Library meets specs/exercise-library/spec.md
  - Run: `./gradlew :feature:exercises:connectedAndroidTest`
  - Spec: Bundled exercise library - "First launch" scenario
  - Spec: Exercise list grouped by usage frequency scenarios
  - Spec: Search exercises - "Search by partial name" scenario
  - Spec: Equipment categories - "Filter by equipment" scenario
  - Spec: Muscle group categories - "Filter by muscle group" scenario
  - Spec: Create custom exercises - "Create custom exercise" + "No limit" scenarios
  - Spec: Edit and delete custom exercises - all scenarios
  - Spec: Effective resistance profile - all scenarios
  - Tests:
    - ExerciseSeedingTest: first launch → 200+ exercises in DB
    - ExerciseListTest: displays grouped by usage frequency
    - ExerciseSearchTest: type "bench" → matching exercises shown
    - ExerciseFilterTest: filter by equipment/muscle → correct subset
    - CustomExerciseTest: create → appears in list, edit → changes persist, delete → removed
    - ResistanceProfileTest: configure multiplier → effectiveWeight calculated correctly

## 5. MVP: Workout Logging

- [ ] 5.1 Create :feature:workout module
- [ ] 5.2 Create WorkoutDao, WorkoutExerciseDao, WorkoutSetDao
- [ ] 5.3 Create WorkoutRepository with domain models and use cases
- [ ] 5.4 Implement WorkoutHomeScreen (Quick start + Template browser)
- [ ] 5.5 Implement "Start Empty Workout" flow
- [ ] 5.6 Implement active workout persistence (survives app kill/restart)
- [ ] 5.7 Implement ActiveWorkoutScreen with duration timer
- [ ] 5.8 Implement add exercise to active workout
- [ ] 5.9 Implement set logging table (SET | PREVIOUS | weight | REPS | checkmark columns)
- [ ] 5.10 Implement weight column header adaptation (+KG/+LBS for weighted bodyweight)
- [ ] 5.11 Implement pre-fill weight/reps from previous workout
- [ ] 5.12 Implement set completion (tap checkmark to complete)
- [ ] 5.13 Implement set type tagging (warm-up, working, failure, drop)
- [ ] 5.14 Implement add/remove sets
- [ ] 5.15 Implement total reps badge per exercise
- [ ] 5.16 Implement rest timer divider display between sets
- [ ] 5.17 Implement in-app rest timer banner (auto-start on set completion)
- [ ] 5.18 Implement timer controls (+30s, -30s, skip)
- [ ] 5.19 Implement superset creation and visual grouping (orange border)
- [ ] 5.20 Implement superset logging flow (scroll to next exercise in group)
- [ ] 5.21 Implement exercise reorder via drag-and-drop
- [ ] 5.22 Implement remove exercise (with confirmation if sets logged)
- [ ] 5.23 Implement workout notes (workout-level)
- [ ] 5.24 Implement exercise notes
- [ ] 5.25 Implement finish workout flow (save to history)
- [ ] 5.26 Implement finish with incomplete sets prompt
- [ ] 5.27 Implement discard workout with confirmation
- [ ] 5.28 Implement only-one-active-workout enforcement
- [ ] 5.29 Implement RPE input (6.0-10.0) per set
- [ ] 5.30 Implement effectiveWeight computation on set completion
- [ ] 5.31 ✓ VALIDATE: Workout Logging meets specs/workout-logging/spec.md
  - Run: `./gradlew :feature:workout:connectedAndroidTest`
  - Spec: Start a workout from scratch - "Start empty workout" scenario
  - Spec: Only one active workout at a time - "Attempt to start while active" scenario
  - Spec: Add exercises to an active workout - "Add exercise" scenario
  - Spec: Log a set - all scenarios (weight/reps, RPE, weight column adaptation)
  - Spec: Pre-fill sets from previous workout - all scenarios
  - Spec: Tag sets by type - all scenarios
  - Spec: Superset/group exercises - all scenarios
  - Spec: Reorder exercises - "Drag exercise to new position" scenario
  - Spec: Remove exercise - "Remove exercise with logged sets" scenario
  - Spec: Add notes - workout-level and exercise-level scenarios
  - Spec: Add and remove sets - all scenarios
  - Spec: Finish workout - all scenarios
  - Spec: Discard workout - "Discard with logged data" scenario
  - Spec: Exercise total reps badge - "Show total reps" scenario
  - Spec: Inline rest timer between sets - all scenarios
  - Spec: Workout duration timer - all scenarios
  - Tests:
    - StartWorkoutTest: tap "Start Empty Workout" → active workout screen with timer
    - SingleActiveWorkoutTest: start second → prompt to finish/discard first
    - AddExerciseTest: tap "Add Exercise" → picker shown, select → exercise added with 1 set
    - LogSetTest: enter weight/reps, tap check → set completed, timestamp recorded, timer starts
    - PreviousDataTest: add exercise with history → PREVIOUS column shows data, weight pre-filled
    - SetTypeTest: long-press set → can tag as warm-up/failure/drop, displays correctly
    - SupersetTest: group exercises → orange border, complete set → scrolls to next in group
    - ReorderTest: drag exercise → new position, sort order persisted
    - NotesTest: add workout note → persisted, add exercise note → persisted
    - FinishWorkoutTest: tap Finish → workout saved to history
    - DiscardWorkoutTest: tap Discard → confirmation, confirm → data deleted
    - PersistenceTest: kill app mid-workout → reopen → workout restored with timer

## 6. MVP: Templates

- [ ] 6.1 Create TemplateDao, TemplateFolderDao, TemplateExerciseDao
- [ ] 6.2 Create TemplateRepository with domain models
- [ ] 6.3 Implement template list in Workout tab (grouped by folders)
- [ ] 6.4 Implement template folder CRUD
- [ ] 6.5 Implement create template flow
- [ ] 6.6 Implement template editor (add/remove/reorder exercises, set targets)
- [ ] 6.7 Implement superset configuration in templates
- [ ] 6.8 Implement template preview screen (exercises, set counts, muscle groups, last performed)
- [ ] 6.9 Implement "Start Workout" from template (pre-populate exercises and weights)
- [ ] 6.10 Implement duplicate template
- [ ] 6.11 Implement delete template
- [ ] 6.12 Implement move template between folders
- [ ] 6.13 ✓ VALIDATE: Templates meet specs/templates/spec.md
  - Run: `./gradlew :feature:workout:connectedAndroidTest --tests "*.Template*"`
  - Spec: Create workout templates - "Create template" + "No template limit" scenarios
  - Spec: Template folders - "Create a folder" + "Templates without folder" scenarios
  - Spec: Edit templates - all scenarios
  - Spec: Duplicate templates - "Duplicate template" scenario
  - Spec: Delete templates - "Delete template" scenario
  - Spec: Template preview screen - "View template preview" scenario
  - Spec: Start workout from template - "Start from template" scenario
  - Spec: Superset configuration in templates - "Configure superset" scenario
  - Tests:
    - CreateTemplateTest: create with exercises → appears in list
    - TemplateFolderTest: create folder → templates can be organized into it
    - EditTemplateTest: modify exercises/targets → changes persist
    - TemplatePreviewTest: tap template → preview with exercises, set counts, last performed
    - StartFromTemplateTest: tap "Start Workout" → active workout with template exercises, weights pre-filled
    - DuplicateTemplateTest: duplicate → copy created with "(copy)" suffix
    - DeleteTemplateTest: delete → removed from list, history unaffected

## 7. MVP: History

- [ ] 7.1 Create :feature:history module
- [ ] 7.2 Implement HistoryScreen with chronological workout list grouped by month
- [ ] 7.3 Implement workout card (name, date, exercises with set count and best set, duration, volume, PR count)
- [ ] 7.4 Implement WorkoutDetailScreen (full workout details)
- [ ] 7.5 Implement calendar modal dialog (green checkmarks on workout days)
- [ ] 7.6 Implement calendar spanning multiple months (scrollable)
- [ ] 7.7 Implement tap calendar day to scroll to workout
- [ ] 7.8 Implement search history by exercise or workout name
- [ ] 7.9 Implement delete workout from history (with confirmation and PR recalculation)
- [ ] 7.10 ✓ VALIDATE: History meets specs/history/spec.md
  - Run: `./gradlew :feature:history:connectedAndroidTest`
  - Spec: Workout history list - "View history" + "Workout card exercise list" scenarios
  - Spec: Workout detail view - "Open workout detail" scenario
  - Spec: Calendar view - all scenarios
  - Spec: Search and filter history - "Search by exercise" scenario
  - Spec: Delete a workout from history - "Delete workout" scenario
  - Tests:
    - HistoryListTest: completed workouts → grouped by month, most recent first
    - WorkoutCardTest: card shows name, date, exercises with set count/best set, duration, volume
    - WorkoutDetailTest: tap card → full workout details displayed
    - CalendarTest: tap calendar icon → modal with workout days marked, tap day → scrolls to workout
    - HistorySearchTest: search "squat" → workouts containing squat shown
    - DeleteWorkoutTest: delete → confirmation, confirm → removed, PRs recalculated

## 8. MVP: Profile Tab

- [ ] 8.1 Create :feature:profile module
- [ ] 8.2 Implement ProfileScreen with workout count
- [ ] 8.3 Implement Dashboard "Workouts per week" bar chart (last 8 weeks)
- [ ] 8.4 ✓ VALIDATE: Profile Tab meets specs/app-shell/spec.md (Profile section)
  - Run: `./gradlew :feature:profile:connectedAndroidTest`
  - Spec: Profile tab - "View profile" scenario
  - Spec: Profile tab - "Workouts per week chart" scenario
  - Tests:
    - ProfileScreenTest: displays workout count, dashboard section
    - WorkoutsPerWeekChartTest: bar chart shows last 8 weeks of workout counts

## 9. MVP: Exercise History & Charts Tab (Basic)

- [ ] 9.1 Implement Exercise History tab (workout history for specific exercise)
- [ ] 9.2 Implement Exercise Charts tab with 1RM progression chart (Epley formula)
- [ ] 9.3 Implement Exercise Records tab (max volume, max reps, max weight, lifetime stats)
- [ ] 9.4 Implement basic PR detection on set completion (star icon indicator)
- [ ] 9.5 ✓ VALIDATE: Exercise Charts meet specs/analytics/spec.md (basic requirements)
  - Run: `./gradlew :feature:exercises:connectedAndroidTest --tests "*.Charts*"`
  - Spec: Per-exercise 1RM progression chart - "View 1RM chart" scenario
  - Spec: PR detection - "New 1RM PR" + "PR types tracked" scenarios
  - Spec: Exercise records view - all scenarios
  - Tests:
    - ExerciseHistoryTabTest: shows workout history for that exercise
    - OneRMChartTest: displays Epley-calculated 1RM over time
    - RecordsTabTest: shows max volume, max reps, max weight, lifetime stats
    - PRDetectionTest: complete set that beats previous → star indicator shown

## 10. Post-MVP: Timer Notifications

- [ ] 10.1 Implement timer notification when app is backgrounded
- [ ] 10.2 Implement timer complete notification with sound/vibration
- [ ] 10.3 Implement configurable rest duration per exercise
- [ ] 10.4 Implement global default rest duration setting
- [ ] 10.5 Implement log set from notification action
- [ ] 10.6 ✓ VALIDATE: Timer Notifications meet specs/timer/spec.md
  - Run: `./gradlew :feature:workout:connectedAndroidTest --tests "*.Timer*"`
  - Spec: Auto-start rest timer on set completion - "Complete a set" scenario
  - Spec: Configurable rest duration per exercise - all scenarios
  - Spec: Timer controls - all scenarios
  - Spec: Timer notification - all scenarios
  - Spec: Log set from notification - "Quick log from notification" scenario
  - Tests:
    - TimerAutoStartTest: complete set → timer starts counting down
    - TimerNotificationTest: background app → notification shows remaining time
    - TimerCompleteTest: timer hits zero → notification with sound/vibration
    - QuickLogTest: tap "Log Set" on notification → next set completed with pre-filled values

## 11. Post-MVP: Analytics (Advanced)

- [ ] 11.1 Create :feature:analytics module
- [ ] 11.2 Implement total volume added chart per exercise
- [ ] 11.3 Implement best set chart per exercise
- [ ] 11.4 Implement total reps chart per exercise
- [ ] 11.5 Implement muscle heat map (body diagram with volume distribution)
- [ ] 11.6 Implement time period selector for heat map
- [ ] 11.7 Implement tap muscle group for exercise/volume detail
- [ ] 11.8 Implement VIEW RECORDS HISTORY (chronological list of PRs)
- [ ] 11.9 ✓ VALIDATE: Advanced Analytics meet specs/analytics/spec.md
  - Run: `./gradlew :feature:analytics:connectedAndroidTest`
  - Spec: Per-exercise total volume added chart - all scenarios
  - Spec: Per-exercise best set chart - "View best set chart" scenario
  - Spec: Per-exercise total reps chart - "View total reps chart" scenario
  - Spec: Muscle heat map - all scenarios
  - Tests:
    - VolumeChartTest: displays total volume per workout over time
    - BestSetChartTest: displays best set reps per workout over time
    - TotalRepsChartTest: displays total reps per workout over time
    - MuscleHeatMapTest: body diagram highlights trained muscles by intensity
    - HeatMapPeriodTest: select "Last 7 Days" → shows correct period
    - RecordsHistoryTest: tap "VIEW RECORDS HISTORY" → chronological PR list

## 12. Post-MVP: Body Tracking

- [ ] 12.1 Create :feature:measure module
- [ ] 12.2 Define BodyMeasurement entity
- [ ] 12.3 Create BodyMeasurementDao and repository
- [ ] 12.4 Implement MeasureScreen layout (vitals + body parts list)
- [ ] 12.5 Implement bodyweight logging
- [ ] 12.6 Implement body fat percentage logging
- [ ] 12.7 Implement caloric intake logging
- [ ] 12.8 Implement body part measurements (neck, shoulders, chest, biceps, forearms, waist, hips, thighs, calves)
- [ ] 12.9 Implement measurement line charts
- [ ] 12.10 Implement delete measurement entries
- [ ] 12.11 ✓ VALIDATE: Body Tracking meets specs/body-tracking/spec.md
  - Run: `./gradlew :feature:measure:connectedAndroidTest`
  - Spec: Log bodyweight - all scenarios
  - Spec: Log body fat percentage - "Log body fat" scenario
  - Spec: Log caloric intake - "Log calories" scenario
  - Spec: Log body part measurements - "Log chest measurement" scenario
  - Spec: Measure tab layout - "View Measure tab" scenario
  - Spec: Body measurement charts - all scenarios
  - Spec: Delete body measurement entries - "Delete a weight entry" scenario
  - Tests:
    - MeasureScreenLayoutTest: vitals at top, body parts section below
    - BodyweightLoggingTest: log weight → saved, displayed below label
    - BodyFatLoggingTest: log body fat → saved, appears in chart
    - BodyPartMeasurementTest: log chest → saved with correct unit
    - MeasurementChartTest: tap measurement → line chart over time
    - DeleteMeasurementTest: long-press → delete → removed, chart updates

## 13. Post-MVP: Tools

- [ ] 13.1 Implement plate calculator (configurable bar weight and available plates)
- [ ] 13.2 Implement warm-up calculator (suggest progression to working weight)
- [ ] 13.3 Implement unit conversion with "convert all data" option
- [ ] 13.4 Implement measurement unit conversion (cm/in)
- [ ] 13.5 ✓ VALIDATE: Tools meet specs/tools/spec.md
  - Run: `./gradlew :app:connectedAndroidTest --tests "*.Tools*"`
  - Spec: Plate calculator - all scenarios
  - Spec: Warm-up calculator - "Calculate warm-ups" scenario
  - Spec: Unit conversion - all scenarios
  - Spec: Measurement unit support - "Switch measurement unit" scenario
  - Tests:
    - PlateCalculatorTest: 100kg with 20kg bar → correct plate breakdown
    - WarmUpCalculatorTest: 100kg target → progression of warm-up sets
    - UnitConversionTest: switch kg→lbs → all displays in lbs
    - ConvertAllDataTest: switch + confirm → DB values recalculated
    - MeasurementUnitTest: switch cm→in → body measurements in inches

## 14. Post-MVP: Data Sync

- [ ] 14.1 Implement Google account sign-in flow
- [ ] 14.2 Implement backup database to Google Drive app data folder
- [ ] 14.3 Implement backup metadata (app version, schema version, timestamp, workout count)
- [ ] 14.4 Implement restore database from Google Drive (list backups, select, replace)
- [ ] 14.5 Implement schema version mismatch handling
- [ ] 14.6 Implement Strong CSV parser (semicolon-delimited, double-quoted)
- [ ] 14.7 Implement Set Order special value handling (W, D, Note, Rest Timer)
- [ ] 14.8 Implement exercise name matching to bundled library
- [ ] 14.9 Implement import progress indicator and summary
- [ ] 14.10 Implement CSV export with share sheet
- [ ] 14.11 ✓ VALIDATE: Data Sync meets specs/data-sync/spec.md
  - Run: `./gradlew :app:connectedAndroidTest --tests "*.DataSync*"` + `./gradlew :core:data:test --tests "*.CsvParser*"`
  - Spec: Backup database to Google Drive - all scenarios
  - Spec: Restore database from Google Drive - all scenarios
  - Spec: Google account authentication - "Sign in for backup" scenario
  - Spec: Strong CSV import - all scenarios
  - Spec: CSV export - all scenarios
  - Tests:
    - StrongCsvParserTest (unit): parse semicolon-delimited CSV → correct workout/set objects
    - SetOrderParsingTest (unit): W→warmup, D→drop, Note→exercise note, Rest Timer→skipped
    - ExerciseMatchingTest (unit): "Bench Press (Dumbbell)" → matches bundled exercise
    - GoogleSignInTest (instrumented): tap backup without sign-in → sign-in flow triggered
    - BackupRestoreTest (instrumented): backup → restore → data intact (requires test account)
    - CsvExportTest (instrumented): export → share sheet with CSV file

## 15. Post-MVP: Onboarding

- [ ] 15.1 Implement first-launch onboarding flow
- [ ] 15.2 Collect unit preference (kg/lbs)
- [ ] 15.3 Collect measurement preference (cm/in)
- [ ] 15.4 Offer Strong CSV import option
- [ ] 15.5 Implement skip onboarding with defaults
- [ ] 15.6 ✓ VALIDATE: Onboarding meets specs/app-shell/spec.md (Onboarding section)
  - Run: `./gradlew :app:connectedAndroidTest --tests "*.Onboarding*"`
  - Spec: Onboarding flow - "First launch" scenario
  - Spec: Onboarding flow - "Skip onboarding" scenario
  - Tests:
    - OnboardingFlowTest: first launch → onboarding displayed, collect preferences
    - OnboardingSkipTest: dismiss → defaults applied (metric), lands on Workout tab
    - OnboardingImportTest: select import → file picker for Strong CSV
