# ETEA Study Plan — Spaced Repetition Tracker

A native, offline-first Android application engineered to help students preparing for ETEA (Educational Testing and Evaluation Agency), MDCAT, and ECAT entrance examinations master their syllabus through systematic spaced repetition.

---

## Overview

Retaining dense syllabus material across subjects like Biology, Physics, Chemistry, Mathematics, and English requires structured revision at increasing intervals. **ETEA Study Plan** automates this schedule: when a student logs a newly studied topic, the app automatically generates spaced revision sessions, rings local notifications 15 minutes before scheduled study hours, tracks completion rates, and visualizes upcoming study loads across an interactive calendar.

The application operates completely offline with zero tracking, zero cloud dependencies, and zero unnecessary permissions—keeping student data entirely private and self-contained on the device.

---

## Features

- **Automated Spaced Repetition Engine**: Computes exact revision milestones based on proven memory retention curves (default intervals: Day 1, 3, 7, 15, and 30, with support for custom intervals).
- **Exact Local Notifications**: Schedules alerts via Android `AlarmManager` with an exact 15-minute advance reminder (`SCHEDULE_EXACT_ALARM`) and background resilience.
- **Interactive Multi-Month Calendar**: Collapsible monthly view cards with animated expansion (`AnimatedVisibility`) and detailed day-by-day revision review bottom sheets.
- **Offline Backup & Restore**: Storage Access Framework (SAF) JSON export and import for lossless, zero-cloud data preservation and cross-device migration.
- **Repository Management**: Search and filter topics by subject, chapter, or keyword; track completion status (`SCHEDULED`, `DONE`, `OVERDUE`).
- **Privacy by Design**: No network connectivity required; does not declare or request `android.permission.INTERNET`.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Language & Tooling** | Kotlin 1.9, JDK 17, Gradle 8.4 (KTS build scripts) |
| **UI & Theming** | Jetpack Compose, Material 3, Compose Animations |
| **Architecture** | MVVM with Coroutines and Kotlin `StateFlow` |
| **Local Database** | AndroidX Room 2.6.1 with KSP code generator |
| **Background Scheduling** | Android `AlarmManager`, BroadcastReceiver, WorkManager |
| **Testing** | JUnit 4, `org.json` |
| **Target SDKs** | compileSdk 34, targetSdk 34, minSdk 24 |

---

## Building the Project

### Continuous Integration (GitHub Actions)

The repository includes an automated CI/CD pipeline in `.github/workflows/android-build.yml`:
1. **Unit Test Execution**: Executes `./gradlew testDebugUnitTest --no-daemon` to validate scheduling logic and JSON backup serialization.
2. **Debug Build (`build`)**: Compiles `./gradlew clean assembleDebug --no-daemon` and archives the APK as the `app-debug` artifact (retention: 7 days).
3. **Release Build (`build-release`)**: Triggered upon completion of the build job, compiles `./gradlew assembleRelease --no-daemon` and archives the APK as the `app-release` artifact (retention: 7 days).

### Local Command-Line Build

To compile locally, ensure JDK 17 is installed and run:

```bash
# Run unit test suite
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Assemble Release APK
./gradlew assembleRelease
```

Generated APKs will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

---

## Keystore Secrets Setup (GitHub Actions)

For automated release signing in GitHub Actions, configure the following **Repository Secrets** under **Settings > Secrets and variables > Actions**:

| Secret Name | Description | Example Generation |
|---|---|---|
| `KEYSTORE_BASE64` | Base64-encoded string of your `.jks` release keystore file | `base64 -w 0 release.jks` *(Linux)* or `base64 -i release.jks` *(macOS)* |
| `KEYSTORE_PASSWORD` | Password protecting the keystore file | Set during `keytool` generation |
| `KEY_ALIAS` | Alias name of the private key entry | e.g. `studyplan-key` |
| `KEY_PASSWORD` | Password protecting the private key alias | Set during key generation |

> **Note**: If these secrets are not configured, the build pipeline automatically falls back to the default debug signing configuration, ensuring tests and builds continue to succeed.

---

## Installation Instructions

1. **Download APK**:
   - Navigate to the **Actions** tab of the GitHub repository.
   - Click on the latest workflow run on `main`.
   - Scroll down to the **Artifacts** section and download `app-release` (or `app-debug`).
   - Extract the `.zip` archive to retrieve `app-release.apk`.
2. **Install on Device**:
   - Transfer the APK to your Android device (or download directly via browser).
   - Open the `.apk` file. If prompted, enable **Install unknown apps** for your file manager or browser in Android system settings.
   - Follow the system prompt to complete the installation.
3. **Permissions on Android 13+**:
   - Grant the notification permission (`POST_NOTIFICATIONS`) on first launch to ensure revision reminders ring reliably.
   - For uninterrupted exact alarms, ensure battery optimizations are disabled for the app.
