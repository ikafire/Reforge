# Reforge

A free, open-source (MIT) Android workout tracker app that replicates Strong's proven UX while removing artificial limits and adding genuinely useful features.

## Features

- **Workout Logging**: Full set/rep/weight tracking with RPE, set tagging (warm-up, working, failure, drop), supersets, and notes
- **Exercise Library**: 200+ bundled exercises with unlimited custom exercises
- **Templates**: Unlimited workout templates organized in folders
- **Effective Resistance**: Per-exercise leverage/angle/pulley ratios for accurate resistance tracking
- **Analytics**: 1RM progression, volume charts, best set tracking, PR detection, muscle heat map
- **Body Tracking**: Weight, body fat, body measurements over time
- **History**: Calendar view with workout history
- **Rest Timer**: Notification-based set logging
- **Tools**: Plate calculator, warm-up calculator, unit conversion
- **Data Sync**: Google Drive backup/restore, Strong CSV import/export

## Tech Stack

- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose (100% Compose, no XML)
- **Architecture**: MVVM + Clean Architecture, multi-module
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Async**: Coroutines + Flow
- **Date/Time**: kotlinx.datetime
- **Serialization**: kotlinx.serialization
- **Charts**: Vico
- **Cloud**: Google Drive API
- **Min SDK**: 29 (Android 10)

## Project Structure

```
:app                    MainActivity, Navigation, DI setup
:feature:workout        Workout logging + Templates
:feature:exercises      Exercise library
:feature:history        Workout history
:feature:analytics      Charts, PRs, heat map
:feature:profile        Dashboard, settings
:feature:measure        Body tracking
:core:ui               Shared composables, theme
:core:domain           Models, use cases, repo interfaces (pure Kotlin)
:core:data             Repository implementations
:core:database         Room DB, DAOs, entities
:core:common           Utilities, constants (pure Kotlin)
```

## Development Setup

### Prerequisites

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| JDK | 17+ | `java -version` |
| Android Studio | Ladybug (2024.2.1) or newer | - |
| Android SDK | API 35 (compile), API 29+ (min) | SDK Manager |
| Kotlin | 2.0+ | (bundled with project) |
| Gradle | 8.7+ | `./gradlew --version` |

### Required SDK Components

Install via Android Studio SDK Manager (`Tools > SDK Manager`):

**SDK Platforms:**
- Android 15 (API 35) - compile target
- Android 10 (API 29) - minimum for testing

**SDK Tools:**
- Android SDK Build-Tools 35
- Android SDK Platform-Tools
- Android Emulator
- Android SDK Command-line Tools

### Environment Setup

1. **Install Android Studio**
   - Download from [developer.android.com](https://developer.android.com/studio)
   - Complete the setup wizard (installs SDK, emulator, etc.)

2. **Configure JDK**
   - Android Studio bundles JDK 17; use it or install separately
   - Set `JAVA_HOME` environment variable:
     ```powershell
     # Windows (PowerShell)
     $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
     
     # Or add to system environment variables permanently
     [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "User")
     ```

3. **Configure Android SDK**
   - Set `ANDROID_HOME` environment variable:
     ```powershell
     # Windows
     [Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
     ```
   - Add to PATH: `$env:ANDROID_HOME\platform-tools`

4. **Clone and Open**
   ```bash
   git clone https://github.com/yourusername/reforge.git
   cd reforge
   ```
   Open in Android Studio: `File > Open > select project folder`

### Google Drive API Setup (Optional - for backup/restore)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable **Google Drive API**
4. Configure OAuth consent screen (External, app name, scopes)
5. Create OAuth 2.0 credentials (Android client)
   - Package name: `io.github.ikafire.reforge`
   - SHA-1 fingerprint: `./gradlew signingReport`
6. Download `credentials.json` and place in `app/` directory

## Build & Run

### Development Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Build and run
./gradlew :app:installDebug && adb shell am start -n io.github.ikafire.reforge/.MainActivity
```

### Release Build

```bash
# Create release APK (requires signing config)
./gradlew assembleRelease

# Create release bundle for Play Store
./gradlew bundleRelease
```

### Clean Build

```bash
./gradlew clean build
```

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :core:domain:test
./gradlew :feature:workout:test

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### Instrumented Tests (Android)

```bash
# Run all instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run Room database tests
./gradlew :core:database:connectedAndroidTest
```

### Lint & Static Analysis

```bash
# Run Android lint
./gradlew lint

# Run lint for specific module
./gradlew :app:lintDebug
```

## Dependencies

### Core

| Dependency | Version | Purpose |
|------------|---------|---------|
| `org.jetbrains.kotlin:kotlin-stdlib` | 2.0+ | Kotlin standard library |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.8+ | Coroutines for async |
| `org.jetbrains.kotlinx:kotlinx-datetime` | 0.6+ | Date/time handling |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.7+ | JSON serialization |

### Android Jetpack

| Dependency | Purpose |
|------------|---------|
| `androidx.core:core-ktx` | Kotlin extensions |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Lifecycle-aware coroutines |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | ViewModel for Compose |
| `androidx.activity:activity-compose` | Compose Activity |
| `androidx.navigation:navigation-compose` | Type-safe navigation |

### Jetpack Compose

| Dependency | Purpose |
|------------|---------|
| `androidx.compose:compose-bom` | Compose BOM (manages versions) |
| `androidx.compose.ui:ui` | Core Compose UI |
| `androidx.compose.material3:material3` | Material Design 3 |
| `androidx.compose.ui:ui-tooling-preview` | Preview support |

### Database

| Dependency | Purpose |
|------------|---------|
| `androidx.room:room-runtime` | Room database |
| `androidx.room:room-ktx` | Room Kotlin extensions |
| `androidx.room:room-compiler` (ksp) | Room annotation processor |

### Dependency Injection

| Dependency | Purpose |
|------------|---------|
| `com.google.dagger:hilt-android` | Hilt DI |
| `com.google.dagger:hilt-compiler` (ksp) | Hilt annotation processor |
| `androidx.hilt:hilt-navigation-compose` | Hilt + Compose Navigation |

### Charts

| Dependency | Purpose |
|------------|---------|
| `com.patrykandpatrick.vico:compose-m3` | Compose-native charts |

### Google APIs

| Dependency | Purpose |
|------------|---------|
| `com.google.android.gms:play-services-auth` | Google Sign-In |
| `com.google.api-client:google-api-client-android` | Google API client |
| `com.google.apis:google-api-services-drive` | Google Drive API |

### Testing

| Dependency | Purpose |
|------------|---------|
| `junit:junit` | Unit testing |
| `io.mockk:mockk` | Mocking for Kotlin |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | Coroutines testing |
| `androidx.test.ext:junit` | AndroidX JUnit |
| `androidx.test.espresso:espresso-core` | UI testing |
| `androidx.compose.ui:ui-test-junit4` | Compose UI testing |
| `androidx.room:room-testing` | Room testing |

## Version Catalog

All dependencies are managed in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.21"
coroutines = "1.8.1"
compose-bom = "2024.10.01"
room = "2.6.1"
hilt = "2.51.1"
navigation = "2.8.4"
vico = "2.0.0-beta.1"

[libraries]
# ... defined libraries

[plugins]
android-application = { id = "com.android.application", version = "8.7.2" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.27" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
```

## Common Issues

### Build Fails with "SDK location not found"

Create `local.properties` in project root:
```properties
sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
```

### Kotlin/Compose Version Mismatch

Ensure Kotlin and Compose Compiler versions are compatible. Use the Kotlin Compose plugin:
```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}
```

### Room Schema Export

For schema versioning, configure in `build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

## License

MIT License - see [LICENSE](LICENSE)
