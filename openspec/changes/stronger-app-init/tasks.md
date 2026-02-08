## 1. Project Setup

- [x] 1.1 Create new Android project with Kotlin, Compose, min SDK 29
- [ ] 1.2 Configure multi-module Gradle structure (:app, :core:*, :feature:*)
- [x] 1.3 Add core dependencies (Hilt, Room, Compose Navigation, kotlinx.datetime, kotlinx.serialization)
- [x] 1.4 Set up Material Design 3 theme with dark/light support
- [x] 1.5 Create base Application class with Hilt setup

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

## 8. MVP: Profile Tab

- [ ] 8.1 Create :feature:profile module
- [ ] 8.2 Implement ProfileScreen with workout count
- [ ] 8.3 Implement Dashboard "Workouts per week" bar chart (last 8 weeks)

## 9. MVP: Exercise History & Charts Tab (Basic)

- [ ] 9.1 Implement Exercise History tab (workout history for specific exercise)
- [ ] 9.2 Implement Exercise Charts tab with 1RM progression chart (Epley formula)
- [ ] 9.3 Implement Exercise Records tab (max volume, max reps, max weight, lifetime stats)
- [ ] 9.4 Implement basic PR detection on set completion (star icon indicator)

## 10. Post-MVP: Timer Notifications

- [ ] 10.1 Implement timer notification when app is backgrounded
- [ ] 10.2 Implement timer complete notification with sound/vibration
- [ ] 10.3 Implement configurable rest duration per exercise
- [ ] 10.4 Implement global default rest duration setting
- [ ] 10.5 Implement log set from notification action

## 11. Post-MVP: Analytics (Advanced)

- [ ] 11.1 Create :feature:analytics module
- [ ] 11.2 Implement total volume added chart per exercise
- [ ] 11.3 Implement best set chart per exercise
- [ ] 11.4 Implement total reps chart per exercise
- [ ] 11.5 Implement muscle heat map (body diagram with volume distribution)
- [ ] 11.6 Implement time period selector for heat map
- [ ] 11.7 Implement tap muscle group for exercise/volume detail
- [ ] 11.8 Implement VIEW RECORDS HISTORY (chronological list of PRs)

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

## 13. Post-MVP: Tools

- [ ] 13.1 Implement plate calculator (configurable bar weight and available plates)
- [ ] 13.2 Implement warm-up calculator (suggest progression to working weight)
- [ ] 13.3 Implement unit conversion with "convert all data" option
- [ ] 13.4 Implement measurement unit conversion (cm/in)

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

## 15. Post-MVP: Onboarding

- [ ] 15.1 Implement first-launch onboarding flow
- [ ] 15.2 Collect unit preference (kg/lbs)
- [ ] 15.3 Collect measurement preference (cm/in)
- [ ] 15.4 Offer Strong CSV import option
- [ ] 15.5 Implement skip onboarding with defaults
