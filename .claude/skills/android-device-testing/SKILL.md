---
name: android-device-testing
description: >
  Run Android E2E instrumented tests on an emulator and install debug APKs on physical devices
  in a WSL2 environment. Use this skill whenever the user asks to run E2E tests, instrumented tests,
  connected tests, set up an Android emulator, install the app on a phone, or connect a physical
  device. Also use when test commands fail due to emulator/device issues, KVM permissions, or
  adb connectivity problems.
---

# Android Device Testing in WSL2

This skill covers running instrumented (E2E) tests on an Android emulator inside WSL2 and
installing debug APKs on a physical device connected to the Windows host. The agent handles
all WSL-side operations directly; Windows-side operations require the user to act.

## Key Principle: WSL vs Windows Split

- **You (the agent) handle**: everything inside WSL -- environment variables, Gradle commands,
  adb commands, emulator launch, test execution.
- **The user handles**: anything requiring the Windows host -- PowerShell commands, usbipd,
  USB debugging prompts on the phone, and any `sudo` commands.

When something needs to happen on the Windows side or requires `sudo`, tell the user exactly
what to run and wait for confirmation before proceeding.

---

## Environment Setup

Always set these before running Android commands:

```bash
export JAVA_HOME=/usr
export ANDROID_HOME=/home/ikafire/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin
```

## Part 1: Running E2E Tests on Emulator

### 1a. Check if emulator is already running

```bash
adb devices
```

If you see `emulator-5554 device`, skip to step 1d.

### 1b. KVM Permissions (first-time / new shell)

The Android emulator needs KVM. If the emulator crashes with "doesn't have permissions to use KVM":

1. Check group membership:
   ```bash
   groups | grep kvm
   ```

2. If `kvm` is missing, ask the user to run:
   ```
   USER ACTION REQUIRED (in WSL terminal):
   sudo gpasswd -a $USER kvm
   ```

3. After the user confirms, apply the new group in the current shell. Because `newgrp` replaces
   the shell (breaking tool execution), use this pattern to run commands under the new group:
   ```bash
   newgrp kvm <<'INNER'
   # commands that need kvm here
   INNER
   ```

### 1c. Create and Launch Emulator

Check if an AVD exists:
```bash
$ANDROID_HOME/emulator/emulator -list-avds
```

If none exists, create one:
```bash
# Download system image if needed
sdkmanager "system-images;android-35;google_apis;x86_64"

# Create AVD
avdmanager create avd -n Pixel_6_API_35 -k "system-images;android-35;google_apis;x86_64" -d pixel_6
```

Launch headless (no GUI in WSL):
```bash
nohup $ANDROID_HOME/emulator/emulator -avd Pixel_6_API_35 -no-window -no-audio -gpu swiftshader_indirect > /tmp/emulator.log 2>&1 &
```

If KVM is needed, wrap in `newgrp kvm <<'INNER' ... INNER`.

Wait for boot:
```bash
adb wait-for-device
adb shell getprop sys.boot_completed  # wait until this returns "1"
```

### 1d. Run Tests

These commands run on whatever device adb sees -- emulator OR physical phone. If both are
connected, specify the target with `-Pandroid.testInstrumentationRunnerArguments.device=<serial>`
or disconnect the one you don't want.

Run all E2E tests:
```bash
./gradlew :app:connectedDebugAndroidTest
```

Run a specific test class:
```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.ikafire.reforge.e2e.WorkoutLoggingE2ETest
```

Run a specific test method:
```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.ikafire.reforge.e2e.WorkoutLoggingE2ETest#fullWorkoutLoggingFlow
```

### 1e. Running E2E Tests on a Physical Device

E2E tests work on physical devices the same way as on the emulator. The device just needs to
be connected via adb (see Part 2 for USB passthrough setup).

Steps:
1. Follow Part 2 (sections 2a-2b) to connect the phone via usbipd
2. Verify with `adb devices` -- you should see the phone's serial
3. If the emulator is also running and you only want to test on the phone, kill the emulator
   first or use `-s <serial>` with adb
4. Try `./gradlew :app:connectedDebugAndroidTest` first

**If Gradle's installer fails** with `Failed to install split APK(s)` (common on Samsung
devices), bypass it by installing manually and using `adb shell am instrument`:

```bash
# Build the APKs
./gradlew :app:assembleDebug :app:packageDebugAndroidTest

# Install manually
adb install app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

# Run all tests
adb shell am instrument -w io.github.ikafire.reforge.test/io.github.ikafire.reforge.HiltTestRunner

# Run a specific test class
adb shell am instrument -w \
  -e class io.github.ikafire.reforge.e2e.WorkoutLoggingE2ETest \
  io.github.ikafire.reforge.test/io.github.ikafire.reforge.HiltTestRunner
```

### 1f. Reading Test Results

On failure, check the XML report for stack traces:
```bash
cat "app/build/outputs/androidTest-results/connected/debug/TEST-*.xml"
```

The line number in the stack trace tells you which assertion or `waitUntil` failed.

---

## Part 2: Installing on a Physical Device

This requires USB passthrough from Windows to WSL2 via `usbipd`.

### 2a. Ask user to connect and share the device

Tell the user:

```
USER ACTION REQUIRED (in Windows PowerShell as Administrator):

1. Connect your phone via USB
2. Run: usbipd list
   -- Find your phone (e.g., "Samsung" or the device name)
   -- Note the BUSID (e.g., 2-2)
3. Run: usbipd bind --busid <BUSID>
4. Run: usbipd attach --wsl --busid <BUSID>
5. On your phone, tap "Allow USB debugging" if prompted

Let me know when done.
```

### 2b. Verify connection in WSL

```bash
adb devices
```

You should see a device serial (not `emulator-*`). If it shows `unauthorized`, ask the user
to check their phone for the USB debugging authorization prompt.

### 2c. Install the APK

```bash
./gradlew installDebug
```

Or if the APK is already built:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2d. Important: API level compatibility

The debug APK targets SDK 35 (min SDK 29 / Android 10). If the device runs Android < 10,
installation will fail. Check first:
```bash
adb shell getprop ro.build.version.sdk
```

If the SDK version is below 29, inform the user that the device is not compatible.

### 2e. Disconnect when done

When finished, tell the user:

```
USER ACTION REQUIRED (in Windows PowerShell):
Run: usbipd detach --busid <BUSID>
```

Then in WSL:
```bash
adb disconnect
```

---

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `KVM permission denied` | User not in kvm group | Ask user to run `sudo gpasswd -a $USER kvm`, then use `newgrp kvm` |
| `emulator: ERROR: x86_64 emulation requires hardware acceleration` | KVM not available | Same as above |
| `adb: no devices/emulators found` | Emulator not running or phone not attached | Check `adb devices`, relaunch emulator or re-attach USB |
| `INSTALL_FAILED_OLDER_SDK` | Device API < minSdk 29 | Device not compatible, use emulator instead |
| `unauthorized` in adb devices | USB debugging not approved | Ask user to tap "Allow" on phone |
| `error: device offline` | USB connection dropped | Ask user to re-run `usbipd attach --wsl --busid <BUSID>` |
| Test timeout at `waitUntil` | UI state not updating | Check if the assertion target actually appears; may be a real bug |

---

## Test Infrastructure Summary

This project uses:
- **Hilt testing** with `HiltTestRunner` and `@HiltAndroidTest`
- **In-memory Room DB** via `TestDatabaseModule` (`@TestInstallIn` replaces production DB)
- **Zero mocks** -- all tests run against real (in-memory) data layer
- **Compose UI testing** with `createAndroidComposeRule<MainActivity>`
- Test helper in `E2ETestHelper.kt` provides `waitForMainScreen()` and `navigateToTab()`
