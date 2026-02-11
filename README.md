# Reforge

[![CI](https://github.com/ikafire/Reforge/actions/workflows/ci.yml/badge.svg)](https://github.com/ikafire/Reforge/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-10%2B-green.svg)](https://developer.android.com)

A free, open-source Android workout tracker. Track sets, reps, and weight with no artificial limits.

## Features

- **Workout Logging** — Full set/rep/weight tracking with RPE, set types (warm-up, working, failure, drop), supersets, and notes
- **Exercise Library** — 200+ bundled exercises with unlimited custom exercises
- **Templates** — Unlimited workout templates organized in folders
- **Effective Resistance** — Per-exercise leverage/angle/pulley ratios for accurate resistance tracking
- **Analytics** — 1RM progression, volume charts, best set tracking, PR detection, muscle volume breakdown
- **Body Tracking** — Weight, body fat, and body measurements over time
- **History** — Calendar view with full workout history
- **Rest Timer** — Notification-based timer with set logging
- **Tools** — Plate calculator, warm-up calculator, unit conversion (kg/lbs, cm/in)
- **Import/Export** — Strong CSV import/export for easy migration

## Screenshots

_Coming soon_

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose (100% Compose) |
| Architecture | MVVM + Clean Architecture, multi-module |
| Database | Room (SQLite) |
| DI | Hilt |
| Async | Coroutines + Flow |
| Date/Time | kotlinx.datetime |
| Charts | Vico |
| Min SDK | 29 (Android 10) |

## Project Structure

```
:app                    MainActivity, Navigation, DI setup
:core:domain            Models, repository interfaces (pure Kotlin)
:core:data              Repository implementations, CSV import/export
:core:database          Room DB, DAOs, entities
:core:ui                Shared theme and composables
:core:common            Utilities, constants (pure Kotlin)
:feature:workout        Workout logging + templates
:feature:exercises      Exercise library + detail screens
:feature:history        Workout history
:feature:analytics      Charts, PRs, muscle volume
:feature:profile        User profile + dashboard
:feature:measure        Body measurements
:feature:settings       Settings, plate/warm-up calculators
```

## Getting Started

### Prerequisites

- **JDK 17+** — bundled with Android Studio or install separately
- **Android Studio** — Ladybug (2024.2.1) or newer
- **Android SDK** — API 35 (compile), API 29+ (min)

### Build

```bash
git clone https://github.com/ikafire/Reforge.git
cd Reforge

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

### Test

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug
```

## Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes
4. Run tests (`./gradlew testDebugUnitTest`)
5. Commit your changes (`git commit -m 'feat: add my feature'`)
6. Push to the branch (`git push origin feature/my-feature`)
7. Open a Pull Request

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
