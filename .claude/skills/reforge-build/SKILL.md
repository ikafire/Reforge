---
name: reforge-build
description: >
  Build, test, install, and lint the Reforge Android app. Use this skill whenever the user asks
  to build the app, run unit tests, run instrumented tests, run lint, install on a device, check
  for compilation errors, or verify a change compiles. Also use when a build or test command fails
  and the user needs help diagnosing it.
---

# Reforge Build & Test

## Prerequisites

- JDK 17+ (set `JAVA_HOME=/usr` in WSL)
- Gradle wrapper included — always use `./gradlew`, never a system Gradle

## Build

```bash
# Debug APK (fast, no minification)
./gradlew assembleDebug

# Release APK (ProGuard minification + signing)
./gradlew assembleRelease
```

Output APKs land in `app/build/outputs/apk/{debug,release}/`.

## Install

```bash
# Build + install on connected device/emulator
./gradlew installDebug
```

If no device is connected, this fails. See the `android-device-testing` skill for emulator setup
and USB passthrough.

## Unit Tests

Unit tests use JUnit 4 + MockK and live in `src/test/java/`.

```bash
# All modules
./gradlew testDebugUnitTest

# Single module
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:workout:testDebugUnitTest
```

### Modules with unit tests

| Module | What's tested |
|---|---|
| `:app` | App-level logic |
| `:core:data` | Repository implementations, mappers, CSV sync |
| `:feature:exercises` | Exercise list/filter logic |
| `:feature:workout` | Workout logging logic |
| `:feature:settings` | Settings logic, calculators |

Modules without tests: `:core:common`, `:core:domain`, `:core:ui`, `:feature:history`,
`:feature:profile`, `:feature:analytics`, `:feature:measure`.

## Instrumented (Android) Tests

These require a running emulator or connected device. See the `android-device-testing` skill
for setup.

```bash
# All instrumented tests
./gradlew connectedDebugAndroidTest

# Single module
./gradlew :app:connectedDebugAndroidTest
./gradlew :core:database:connectedDebugAndroidTest

# Single test class
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.ikafire.reforge.e2e.WorkoutLoggingE2ETest

# Single test method
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.ikafire.reforge.e2e.WorkoutLoggingE2ETest#fullWorkoutLoggingFlow
```

### Modules with instrumented tests

| Module | What's tested |
|---|---|
| `:app` | E2E flows (9 test files) |
| `:core:database` | Room DAOs and migrations |
| `:feature:exercises` | Exercise list UI |
| `:feature:workout` | Workout UI |

### Reading test results

On failure, check the XML report for stack traces:
```bash
# Unit test results
find . -path "*/test-results/testDebugUnitTest/*.xml" -newer build.gradle.kts

# Instrumented test results
find . -path "*/androidTest-results/connected/debug/TEST-*.xml" -newer build.gradle.kts
```

## Lint

```bash
# All modules
./gradlew lintDebug

# Single module
./gradlew :feature:workout:lintDebug
```

Report at `{module}/build/reports/lint-results-debug.html`.

## Troubleshooting

| Problem | Fix |
|---|---|
| `JAVA_HOME is not set` | `export JAVA_HOME=/usr` |
| `Could not determine java version` | Ensure JDK 17+ is installed |
| `No connected devices` | Launch emulator or connect phone (see `android-device-testing` skill) |
| `Execution failed for task :app:kspDebugKotlin` | Usually a Room/Hilt annotation error — read the full error for the source file |
| Build seems stuck at "Configuring project" | Normal on first run — Gradle is downloading dependencies |
