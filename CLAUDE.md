# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Reforge is an Android workout tracking app built with Kotlin and Jetpack Compose. It uses Clean Architecture with a multi-module Gradle setup.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (with ProGuard minification)
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run all unit tests
./gradlew testDebugUnitTest

# Run tests for a specific module
./gradlew :core:data:testDebugUnitTest

# Run lint
./gradlew lintDebug
```

Requires JDK 17+. Gradle wrapper is included (`gradlew` / `gradlew.bat`).

## Architecture

**Clean Architecture layers with unidirectional dependency flow:**

```
Feature modules (UI + ViewModels)
    ↓
:core:domain (pure Kotlin — models + repository interfaces)
    ↑
:core:data (repository implementations, mappers, CSV sync, exercise seeder)
    ↓
:core:database (Room entities, DAOs, migrations)
```

**Module layout:**
- `:app` — MainActivity, navigation, onboarding, rest timer foreground service
- `:core:common` — AppResult sealed class, constants, UUID generator (pure Kotlin)
- `:core:domain` — Domain models and repository interfaces (pure Kotlin JVM, no Android deps)
- `:core:database` — Room database (v2, 8 entities), DAOs, type converters
- `:core:data` — Repository impls, entity↔domain mappers, CSV import/export (Strong app format), exercise seeder
- `:core:ui` — Shared Compose theme (Material3 with custom orange scheme, dark mode)
- `:feature:workout` — Active workout logging, templates
- `:feature:exercises` — Exercise library (200+ bundled from `assets/exercises.json`)
- `:feature:history` — Workout history calendar
- `:feature:analytics` — Charts (Vico), PRs, volume breakdown
- `:feature:profile` — User dashboard
- `:feature:measure` — Body measurements
- `:feature:settings` — Settings, plate calculator, warm-up calculator, import/export

## Key Patterns

- **MVVM:** ViewModels expose `StateFlow`, UI collects with `collectAsStateWithLifecycle()`. State combined with `combine()` and shared via `stateIn(WhileSubscribed(5_000))`.
- **Hilt DI:** `@HiltViewModel` for ViewModels. Modules use `@Binds` for repository interfaces, `@Provides` for concrete types, all `@InstallIn(SingletonComponent::class)`.
- **Repository pattern:** Interfaces in `:core:domain`, implementations in `:core:data`. All expose `Flow` for reactive data.
- **Mappers:** Extension functions `toEntity()` / `toDomain()` in `:core:data/mapper/`.
- **Navigation:** Type-safe using Kotlin Serialization route objects, centralized in `ReforgeNavHost.kt`.

## Tech Stack

Kotlin 2.0.21, Compose BOM 2024.10.01, Room 2.6.1, Hilt 2.51.1, Coroutines 1.8.1, kotlinx.datetime 0.6.1, kotlinx.serialization 1.7.3, Navigation Compose 2.8.4, Vico 2.0.0-beta.1, DataStore Preferences 1.1.1. Targets SDK 35, min SDK 29. Dependencies managed via `gradle/libs.versions.toml` version catalog.

## Code Style

- Kotlin official code style, 4-space indentation, LF line endings, UTF-8, max 120 chars per line (see `.editorconfig`)
- Package naming: `io.github.ikafire.reforge.[module].[layer]`
- Naming: `*Screen.kt`, `*ViewModel.kt`, `*Repository.kt` / `*RepositoryImpl.kt`, `*Entity.kt`, `*Dao.kt`, `*Module.kt`
- Conventional Commits for git messages (feat:, fix:, chore:, etc.)
- Tests use JUnit 4 + MockK, located in `src/test/java/`
