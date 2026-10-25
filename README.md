# Sumi Launcher

A quiet Android home for e-ink screens, designed first for the original BOOX Palma. Sumi pairs a serif clock and headings with clear app names, generous whitespace, and still pages instead of animated scrolling.

[Validation and performance notes](docs/VALIDATION.md). Screen captures are included with the local build deliverables.

## Everyday use

- **Home:** tap a pinned app to open it. Use **All apps** or swipe left to open the library. **Customize** manages your home and opens Settings.
- **Pages:** use the arrow buttons or swipe vertically. Complete rows are measured to fit the available space, including enlarged system text. A swipe changes at most one page and does not launch an app.
- **Library:** search original or custom app names. Swipe right or use Back to return. The keyboard opens only when you tap search.
- **App actions:** long-press an app to add/remove it from home, rename it, hide it, or open Android app information. Clear a custom name to restore the original.
- **Customize:** select Home to reorder or remove pinned apps, or Add apps to select more. Changes save immediately; an empty home is supported.
- **Hidden apps:** restore them through Customize → Settings → Hidden apps. Restoring makes an app visible in the library without repinning it.
- **Android Home:** returns to the first home page and dismisses launcher dialogs.

## Appearance and notifications

Text-only rows are the default. Customize → Settings → App icons enables grayscale installed-app icons throughout Sumi. Buttons use immediate static press feedback; launcher navigation and paging have no decorative animation.

Setting Sumi as the default home app is part of onboarding. Accessibility access is optional: it enables only the swipe-down notification shortcut on the clock. The launcher stays usable if access is declined or later revoked.

On BOOX, open the E Ink Center while using Sumi and choose App Optimization to adjust per-app display and refresh settings. Options vary by device and firmware. Sumi leaves refresh control to the device; physical ghosting and refresh quality must be checked on hardware. See [BOOX’s app optimization guide](https://booxsupport.zendesk.com/hc/en-us/articles/8569442137108-App-Optimization).

## Build and test

Requirements: JDK 17, Android SDK platform 36, Build Tools 36.0.0, and Android 11+ for device tests. Set `ANDROID_HOME` to your SDK installation or configure `local.properties` in Android Studio.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Instrumented tests require a connected Android device or emulator. They cover gestures, paging, search focus, empty home/editing, optional accessibility access, hidden-app recovery, renaming, and 200% text sizing. Unit tests cover preference compatibility, aliases, hiding/restoration, sorting, search, and page boundaries.

## Upgrade behavior

The package remains `com.sumilauncher` (minimum API 30, target/compile API 36). Version 2 preserves the existing DataStore file, pin encoding/order, and completed onboarding. Existing installations default to text-only. Custom names, hidden apps and their recovery labels, and icon visibility are additive preferences.

Temporarily unavailable pinned apps retain their place and name. Package replacement does not erase configuration. Saved package configuration is removed only after a confirmed uninstall broadcast; a missing launcher activity alone is not treated as an uninstall. Launch failures and save failures are surfaced without crashing the launcher.

An in-place APK upgrade requires the same signing certificate as the installed build. This project retains Android’s existing debug signing setup and does not include a release signing key.

## Implementation

Kotlin, Jetpack Compose, Material 3, AndroidX Navigation, and DataStore; no additional runtime framework or permission was added. Installed-app loading and sorting run off the UI thread. Optional icons load asynchronously into a bounded 4 MiB cache and are invalidated on package changes. The home clock updates at minute boundaries only while its navigation entry is resumed, and refreshes after time, timezone, date, locale, or lifecycle changes.

The launcher remains focused on launching and organizing apps. Widgets, calendar feeds, reading-service integrations, cloud sync, and proprietary display SDKs are outside this version.
