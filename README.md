# Sumi Launcher

Sumi Launcher is a minimal Android home screen built for e-ink devices, with the current implementation targeting the BOOX Palma. It uses Jetpack Compose, keeps the primary flow gesture-driven, and focuses on a short list of pinned apps instead of a dense icon grid.

## Features

- Guided onboarding for setting Sumi as the default launcher and enabling notification-shade access through an accessibility service
- Home screen with a live date/time header and a user-managed list of pinned apps
- Swipe left from home to open an alphabetical full-screen app drawer
- Swipe down from the top of the home list to open the notifications shade
- Long-press entry into edit mode from the home header or any pinned app row
- Edit screen for adding apps, removing apps, and reordering pinned items
- Preferences persisted with DataStore
- Unit tests for sorting and pinned-app persistence
- Compose UI tests for onboarding, home/drawer gestures, and edit mode

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Navigation
- AndroidX DataStore
- Gradle

## Requirements

- JDK 17
- API 36
- Build Tools 36.0.0
- Android Studio
- An Android 11+ device or emulator for instrumented/UI tests

Project defaults:

- `applicationId`: `com.sumilauncher`
- `minSdk`: `30`
- `targetSdk`: `36`
- `compileSdk`: `36`

## Project Layout

- `app/src/main/java/com/sumilauncher/ui`: Compose screens, view model, and app shell
- `app/src/main/java/com/sumilauncher/data`: DataStore-backed repositories and pinned-app encoding
- `app/src/main/java/com/sumilauncher/platform`: launcher setup checks, app launching, and notification accessibility integration
- `app/src/test`: JVM unit tests
- `app/src/androidTest`: Compose UI and instrumentation tests

## Run Locally

1. Open the project in Android Studio.
2. Let Gradle sync and install any missing Android SDK packages.
3. Start an emulator or connect a device that can act as a home app target.
4. Run the `app` configuration.
5. On first launch, complete onboarding by setting Sumi as the default launcher and enabling the accessibility service used for swipe-down notifications.

You can also build from the CLI:

```bash
./gradlew assembleDebug
```

The debug APK is written to:

`app/build/outputs/apk/debug/app-debug.apk`

## Test

Run JVM tests:

```bash
./gradlew testDebugUnitTest
```

Run instrumented tests on a connected device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

## Behavior Notes

- The app advertises both `HOME` and `LAUNCHER` intent filters.
- The accessibility service is only used to open the notification shade from the home gesture.
- The launcher is portrait-only in the current implementation.
- The home screen intentionally uses a simple text-first presentation suited to e-ink displays.

## Current Status

This repository already includes a working Android app module, Gradle wrapper, JVM tests, instrumented tests, and a generated debug APK from a local build. If you want cleaner repo hygiene next, the main follow-up would be excluding `app/build/` artifacts from version control.
