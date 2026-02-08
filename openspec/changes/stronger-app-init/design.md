## Context

Stronger is a greenfield Android workout tracker app. There is no existing codebase — this design establishes the foundational architecture for all 10 capabilities defined in the proposal. The developer is new to Android development, so the architecture must be learnable and well-documented while remaining robust enough for a production app with future KMP migration potential.

## Goals / Non-Goals

**Goals:**
- Establish a clean, modular architecture that a beginner can navigate
- Keep the domain layer free of Android dependencies (KMP-ready)
- Local-first data with optional Google Drive backup
- Ship a usable MVP (workout logging + exercises + templates + history) before building analytics and tools

**Non-Goals:**
- Real-time multi-device sync (backup/restore is sufficient)
- Backend server (no custom API — Google Drive API only)
- iOS or web support (future KMP migration, not now)
- AI features (deferred to a future change)
- Progress photos, Health Connect integration

## Decisions

### 1. UI Framework: Jetpack Compose (Compose-only, no XML)

**Choice**: 100% Jetpack Compose with Material Design 3.

**Alternatives considered**:
- XML Views + Fragments: Legacy approach, more boilerplate, harder to learn in 2026
- Compose + XML hybrid: Unnecessary complexity for a greenfield project

**Rationale**: Compose is the modern Android standard, has better tooling, less boilerplate, and is the foundation for Compose Multiplatform if we migrate to KMP later.

### 2. Architecture: MVVM + Clean Architecture with Multi-Module

**Choice**: Three-layer architecture across a multi-module project.

```
┌─────────────────────────────────────────────────────┐
│  :app                                               │
│  MainActivity, Navigation, DI setup                 │
├─────────────────────────────────────────────────────┤
│  :feature:*          (one per capability)            │
│  Screen composables + ViewModels                    │
│  feature:workout (includes templates), feature:       │
│  exercises, feature:history, feature:analytics,     │
│  feature:profile, feature:measure, feature:settings │
├─────────────────────────────────────────────────────┤
│  :core:ui            Shared composables, theme      │
│  :core:domain        Models, use cases, repo I/F    │  ← PURE KOTLIN
│  :core:data          Repo implementations           │
│  :core:database      Room DB, DAOs, entities        │
│  :core:common        Utilities, constants           │  ← PURE KOTLIN
└─────────────────────────────────────────────────────┘
```

**Rationale**: Multi-module enforces boundaries at compile time. `:core:domain` and `:core:common` have zero Android dependencies, making KMP migration a matter of moving these modules into a shared KMP module. Feature modules keep each capability isolated.

**Alternatives considered**:
- Single module: Simpler to start but becomes tangled fast. Harder to enforce layer separation.
- Feature-first single module with packages: No compile-time boundary enforcement.

### 3. Database: Room with a Strict Schema

**Choice**: Room (SQLite abstraction) with a typed, relational schema.

**Core entities and relationships**:

```
Exercise
├── id: UUID
├── name: String
├── category: Enum (barbell, dumbbell, cable, machine, bodyweight, cardio, duration)
├── primaryMuscle: Enum
├── secondaryMuscles: List<Enum>
├── instructions: String?
├── isCustom: Boolean
├── resistanceProfile: ResistanceProfile?   ← NEW (Stronger-unique)
└── createdAt: Instant

ResistanceProfile
├── type: Enum (direct, angle, lever, pulley, custom)
├── multiplier: Double              (e.g., 0.707 for 45° leg press)
└── notes: String?                  (e.g., "45-degree sled")

WorkoutTemplate
├── id: UUID
├── name: String
├── folderId: UUID?
├── sortOrder: Int
└── exercises: List<TemplateExercise>   (with sortOrder, target sets/reps)

TemplateFolder
├── id: UUID
├── name: String
└── sortOrder: Int

Workout (a completed or in-progress session)
├── id: UUID
├── templateId: UUID?           (null if started from scratch)
├── startedAt: Instant
├── finishedAt: Instant?
├── notes: String?
├── isActive: Boolean           (true = in-progress workout)
└── exercises: List<WorkoutExercise>

WorkoutExercise
├── id: UUID
├── workoutId: UUID
├── exerciseId: UUID
├── sortOrder: Int
├── supersetGroup: Int?         (null = not in a superset, same int = grouped)
├── notes: String?
└── sets: List<WorkoutSet>

WorkoutSet
├── id: UUID
├── workoutExerciseId: UUID
├── sortOrder: Int
├── type: Enum (warmup, working, failure, drop)
├── weight: Double?             (in user's preferred unit)
├── reps: Int?
├── distance: Double?           (for cardio)
├── duration: Duration?         (for timed exercises)
├── rpe: Double?                (6.0–10.0)
├── effectiveWeight: Double?    (computed: weight × resistanceProfile.multiplier)
├── isCompleted: Boolean
└── completedAt: Instant?

BodyMeasurement
├── id: UUID
├── date: LocalDate
├── type: Enum (weight, bodyFat, caloricIntake, neck, shoulders, chest, waist, hips, leftBicep, rightBicep, leftForearm, rightForearm, leftThigh, rightThigh, leftCalf, rightCalf)
├── value: Double
└── unit: Enum
```

**Rationale**: A strict relational schema is simpler to reason about, query, and migrate. Room enforces it at compile time. The `effectiveWeight` field is computed on set completion using the exercise's resistance profile — this powers charts that compare true resistance across exercises.

**Alternatives considered**:
- Flexible schema (Map<String, Any> per set): More extensible but loses type safety, makes queries harder, and Room doesn't support it natively.
- SQLDelight: KMP-native, but Room now supports KMP too and has a larger ecosystem.

### 4. Dependency Injection: Hilt

**Choice**: Hilt (built on Dagger).

**Rationale**: Industry standard for Android. ViewModels get constructor injection automatically. Compose navigation integration is mature.

**Alternatives considered**:
- Koin: Simpler API, runtime DI (no compile-time safety). Would be easier for a beginner but less robust.
- Manual DI: Too much boilerplate as the app grows.

### 5. Navigation: Compose Navigation with Type-Safe Routes

**Choice**: `androidx.navigation.compose` with type-safe route objects (Kotlin Serialization-based).

**Navigation structure**:

```
BottomNav (matches Strong's layout)
├── Profile Tab     → ProfileScreen (dashboard: workouts/week chart, lifetime stats)
├── History Tab     → HistoryScreen → WorkoutDetailScreen
│                     └── CalendarDialog (modal overlay)
├── Workout Tab     → WorkoutHomeScreen (start empty + template browser)
│                     ├── TemplateFolderScreen → TemplateEditScreen
│                     └── ActiveWorkoutScreen (full-screen when workout in progress)
├── Exercises Tab   → ExerciseListScreen (grouped by frequency) → ExerciseDetailScreen
│                     └── Tabs: About | History | Charts | Records
├── Measure Tab     → MeasureScreen (weight, body fat, caloric intake, body parts)
└── (TopBar)        → SettingsScreen
```

### 6. Async & Reactive Data: Coroutines + Flow

**Choice**: `kotlinx.coroutines` for async operations, `kotlinx.coroutines.flow.Flow` for reactive data streams from Room.

**Pattern**: Room DAOs return `Flow<List<T>>` → Repositories expose `Flow` → ViewModels collect in `stateIn()` → Compose UI observes via `collectAsStateWithLifecycle()`.

**Rationale**: This is the standard reactive pattern for Compose apps. Flow (not LiveData) is KMP-compatible.

### 7. Date/Time: kotlinx.datetime

**Choice**: `kotlinx.datetime` for all date/time handling.

**Rationale**: Pure Kotlin (no `java.time` dependency), KMP-compatible. Stores as ISO-8601 strings or epoch millis in Room.

### 8. Serialization: kotlinx.serialization

**Choice**: `kotlinx.serialization` for JSON (CSV import/export, Drive backup metadata, navigation args).

**Rationale**: KMP-compatible, compile-time safe, works with Compose Navigation type-safe routes.

### 9. Charts: Vico

**Choice**: Vico for Compose-native charts.

**Rationale**: Built specifically for Jetpack Compose, actively maintained, supports line/bar charts needed for 1RM progression and volume tracking. MPAndroidChart is View-based and requires interop.

### 10. Cloud Backup: SQLite File to Google Drive

**Choice**: Export the Room database file to Google Drive via the Drive API. Restore = download and replace.

**Flow**:

```
Backup:
  Room DB (.db file) → copy to temp → upload to Google Drive (app data folder)

Restore:
  Google Drive → download .db file → close Room → replace local DB → reopen Room

Conflict strategy: Last-write-wins (no merge). Single-user, single-device primary use.
```

**Rationale**: Simplest possible sync. No merge conflicts, no backend, no real-time complexity. The entire database is one file. Google Drive's app data folder is invisible to the user (no clutter) and free.

**Alternatives considered**:
- Firebase Firestore: Real-time sync but overkill, adds vendor complexity, NoSQL schema mismatch.
- Supabase: Good but requires hosting a service.
- Google Drive with JSON files: More granular but complex serialization/deserialization, conflict-prone.

### 11. Strong CSV Import

**Format**: Strong exports a **semicolon-delimited** CSV with all values double-quoted. Exact columns:

```
"Workout #";"Date";"Workout Name";"Duration (sec)";"Exercise Name";"Set Order";"Weight (kg)";"Reps";"RPE";"Distance (meters)";"Seconds";"Notes";"Workout Notes"
```

**Set Order special values** (not just numbers):
- `"W"` = warm-up set
- `"D"` = drop set
- `"1"`, `"2"`, ... = working sets (numbered)
- `"Note"` = exercise-level note (text stored in `Notes` column, no weight/reps)
- `"Rest Timer"` = rest timer entry (duration stored in `Seconds` column, no weight/reps)

**Example rows**:
```
"170";"2026-02-04 20:23:01";"Push focus";"4557";"Bench Press (Dumbbell)";"W";"48.0";"10";"";"";"";"";""
"170";"2026-02-04 20:23:01";"Push focus";"4557";"Bench Press (Dumbbell)";"Rest Timer";"";"";"";"";"90.0";"";""
"170";"2026-02-04 20:23:01";"Push focus";"4557";"Bench Press (Dumbbell)";"1";"56.0";"7";"";"";"";"";""
"170";"2026-02-04 20:23:01";"Push focus";"4557";"Pull Up";"Note";"";"";"";"";"";"Explosive";""
```

**Strategy**: Parse semicolon-delimited CSV → skip `"Rest Timer"` rows (optionally import rest durations per exercise) → extract `"Note"` rows as exercise notes → map `"W"` to warmup and `"D"` to drop set types → match exercise names to bundled library (case-insensitive, exact match on `"Name (Equipment)"` format) → create Workout + WorkoutExercise + WorkoutSet records → insert into Room.

Unmatched exercises are created as custom exercises automatically. Exercise names in Strong follow the pattern `"Name (Equipment)"` — e.g., `"Bench Press (Dumbbell)"`, `"Pull Up"`, `"Face Pull (Cable)"`.

### 12. Unit System

**Choice**: Store all weights in the user's preferred unit (kg or lbs). Provide a one-time "convert all" setting that recalculates stored values.

**Rationale**: Simpler than storing in a canonical unit and converting on display. The "convert all" migration runs once and rewrites weight values in the DB.

### 13. Exercise Library Data Source

**Choice**: Bundle exercise data as a JSON asset in the APK. Seed into Room on first launch.

**Source**: Curate from open-source exercise databases (wger, free-exercise-db) + manual additions.

**Format**: `assets/exercises.json` → parsed on first launch → inserted into Exercise table with `isCustom = false`.

### 14. UI/UX Design Process: HTML Mockups

**Choice**: Create static HTML mockups in `mockups/` for each major screen before implementing in Compose.

**Process**:

```
For each screen/feature:
  1. Create HTML mockup (phone-sized, Material Design 3 styled)
  2. Review layout, element placement, information hierarchy
  3. Iterate on feedback
  4. Implement in Jetpack Compose based on approved mockup
```

**Rationale**: CLI-based development has no visual design tool. HTML mockups bridge the gap — they render in any browser, show real colors/typography/spacing, and are fast to iterate on. They serve as a visual spec that the Compose implementation targets.

**Mockups created so far**:
- `mockups/workout-logging.html` — Core workout logging screen (approved)

**Screens that will need mockups**:
- Exercise detail (About / History / Charts / Records tabs)
- Template manager (folders, list, edit)
- History list + calendar view
- Analytics dashboard (charts, muscle heat map)
- Body tracking screen
- Settings / import-export
- Onboarding

## Risks / Trade-offs

**[Beginner developer]** → The multi-module setup and Clean Architecture have a learning curve. Mitigated by establishing a clear first feature module (`feature:workout`) as a reference pattern that other features copy.

**[No real sync, only backup/restore]** → If the user switches phones, they must manually backup and restore. Mitigated by prompting for backup on significant milestones (every N workouts).

**[Last-write-wins backup]** → If somehow used on two devices, one device's data will overwrite the other. Acceptable for single-user use. Documented in settings.

**[Bundled exercise library maintenance]** → Adding new exercises requires an app update. Mitigated by allowing unlimited custom exercises. A future change could add a community exercise database.

**[Vico charting library]** → Newer library, smaller community than MPAndroidChart. Mitigated by Vico's active development and Compose-native approach. Can swap later if needed since charts are isolated in `feature:analytics`.

**[Room DB file backup portability]** → Room DB files are tied to the schema version. Backup from v2 can't restore into v1. Mitigated by including schema version in backup metadata and validating before restore.
