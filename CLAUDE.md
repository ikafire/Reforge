# CLAUDE.md

## Project Overview

Reforge is an Android workout tracking app built with Kotlin and Jetpack Compose. It uses Clean Architecture with a multi-module Gradle setup. Requires JDK 17+.

## Skills

Build, test, install, and lint commands are in the `reforge-build` skill (auto-triggered).
E2E and device testing is in the `android-device-testing` skill.

## Architecture

```
Feature modules (UI + ViewModels)
    ↓
:core:domain (pure Kotlin — models + repository interfaces)
    ↑
:core:data (repository implementations, mappers, CSV sync, exercise seeder)
    ↓
:core:database (Room entities, DAOs, migrations)
```

**Modules:** `:app` (navigation, onboarding, rest timer service) · `:core:common` (AppResult, constants, UUID) · `:core:domain` (domain models, repository interfaces) · `:core:database` (Room v2, 8 entities) · `:core:data` (repo impls, mappers, CSV import/export, exercise seeder) · `:core:ui` (Material3 theme, dark mode) · `:feature:workout` · `:feature:exercises` (200+ bundled) · `:feature:history` · `:feature:analytics` (Vico charts) · `:feature:profile` · `:feature:measure` · `:feature:settings` (plate/warm-up calculators, import/export)

## Key Patterns

- **MVVM:** ViewModels expose `StateFlow`, UI collects with `collectAsStateWithLifecycle()`. State combined with `combine()` and shared via `stateIn(WhileSubscribed(5_000))`.
- **Hilt DI:** `@HiltViewModel` for ViewModels. Modules use `@Binds` for repository interfaces, `@Provides` for concrete types, all `@InstallIn(SingletonComponent::class)`.
- **Repository pattern:** Interfaces in `:core:domain`, implementations in `:core:data`. All expose `Flow`.
- **Mappers:** Extension functions `toEntity()` / `toDomain()` in `:core:data/mapper/`.
- **Navigation:** Type-safe using Kotlin Serialization route objects in `ReforgeNavHost.kt`.

## Tech Stack

Kotlin 2.0.21, Compose BOM 2024.10.01, Room 2.6.1, Hilt 2.51.1, Coroutines 1.8.1, kotlinx.datetime 0.6.1, kotlinx.serialization 1.7.3, Navigation Compose 2.8.4, Vico 2.0.0-beta.1, DataStore Preferences 1.1.1. Targets SDK 35, min SDK 29. Dependencies managed via `gradle/libs.versions.toml`.

## Code Style

- Kotlin official style, 4-space indent, LF, UTF-8, max 120 chars (see `.editorconfig`)
- Package: `io.github.ikafire.reforge.[module].[layer]`
- Naming: `*Screen.kt`, `*ViewModel.kt`, `*Repository.kt` / `*RepositoryImpl.kt`, `*Entity.kt`, `*Dao.kt`, `*Module.kt`
- Conventional Commits (feat:, fix:, chore:, etc.)
- Tests: JUnit 4 + MockK in `src/test/java/`

## Gotchas

- Release signing reads env vars: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Falls back to `../reforge-release.jks` with empty password.
- Exercise seeder loads `assets/exercises.json` at first launch via `ExerciseSeeder` in `:core:data`.
- Room DB is at version 2 — bump version and add migration in `:core:database` when changing entities.
