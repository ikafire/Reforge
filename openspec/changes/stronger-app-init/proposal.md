## Why

Strong is the leading workout tracker app, but locks core features (unlimited templates, progress charts, unlimited custom exercises) behind a paywall. It also lacks support for effective resistance tracking on leverage-based machines and has no AI training assistance. Since it's closed-source, these gaps can't be fixed. Stronger is a free, open-source (MIT) Android alternative that replicates Strong's proven UX while removing all artificial limits and adding genuinely useful features.

## What Changes

This is a greenfield Android app. All capabilities are new.

- Full workout logging with sets, reps, weight, RPE, set tagging (warm-up, working, failure, drop), supersets, and notes
- Bundled exercise library (~200+ exercises) with descriptions, muscle targeting, and equipment categories
- Unlimited custom exercises (no caps)
- Unlimited workout templates organized in folders
- Effective resistance tracking: per-exercise leverage/angle/pulley ratios so charts reflect true resistance, not just loaded weight
- Full analytics for all users: 1RM progression, volume charts, best set tracking, PR detection, records, muscle heat map
- Body tracking: weight, body fat, body measurements over time
- Workout history with calendar view
- Rest timer with notification-based set logging
- Plate calculator and warm-up calculator
- Imperial/metric with retroactive conversion
- Data import from Strong (CSV) and export to CSV
- SQLite database backup/restore via Google Drive
- Dark/light theme (Material Design 3)

## Capabilities

### New Capabilities

- `workout-logging`: Core workout session lifecycle — start, log sets, supersets, notes, finish/discard. The primary UX loop.
- `exercise-library`: Bundled exercise database, custom exercise creation (unlimited), exercise metadata (muscles, equipment, instructions), effective resistance profiles.
- `templates`: Workout templates (unlimited), template folders, create/edit/delete/duplicate/reorder, start workout from template.
- `analytics`: Per-exercise charts (1RM, volume, best set), PR detection and records, muscle heat map, body part volume distribution.
- `body-tracking`: Bodyweight, body fat, arbitrary body measurements logging with history and charts.
- `history`: Workout history list and calendar view, per-workout summaries, search and filtering.
- `timer`: Auto rest timer on set completion, per-exercise timer configuration, notification with inline set logging.
- `tools`: Plate calculator (configurable bar weight), warm-up calculator, unit conversion (imperial/metric with retroactive conversion).
- `data-sync`: SQLite file backup/restore to Google Drive, Strong CSV import, CSV export.
- `app-shell`: App navigation, theme (Material Design 3, dark/light), settings, onboarding.

### Modified Capabilities

None (greenfield project).

## Impact

- **New codebase**: Kotlin, Jetpack Compose, Room, Hilt, Coroutines/Flow
- **Min SDK**: API 29 (Android 10)
- **Architecture**: MVVM + Clean Architecture, multi-module, KMP-ready domain layer
- **External dependencies**: Google Drive API (backup/sync), charting library (Vico or MPAndroidChart), kotlinx.datetime, kotlinx.serialization
- **Publishing**: Google Play Store (free), GitHub (MIT license)
- **Data migration**: Strong CSV parser needed for import
