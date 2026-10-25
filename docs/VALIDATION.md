# Sumi 2.0 — validation and delivery

Validated on 25 October 2026 against the original `main` commit `a0e7edde352f0b061da2f10b1f0f9b46822b3367`.

## Build and automated tests

- Debug APK assembly: passed.
- JVM unit tests: **20 passed, 0 failures**.
- Android Compose/instrumented tests: **12 passed, 0 failures**.
- Android lint: passed with **0 errors**. Remaining warnings concern dependency update suggestions, the existing portrait orientation/resources, and a KTX style suggestion.
- `git diff --check`: passed.

Commands used for the final application build:

```sh
./gradlew --offline testDebugUnitTest connectedDebugAndroidTest lintDebug assembleDebug
```

The initial Android test-runner dependency was downloaded with an online Gradle run. No additional runtime framework or permission was added. The full Material icon catalogue was replaced with its core package, which contains all icons used by Sumi.

## Upgrade and end-to-end checks

A fresh original 1.0 build was configured with Calendar, Camera, Chrome, Clock, and Contacts, then upgraded using `adb install -r` to 2.0. **Pinned order and completed onboarding were preserved, with text-only rows as the default.**

The following checks passed in the real application, using only the isolated emulator:

- Rename persists across process restart.
- Library search finds an alias using the original name.
- Hiding removes home entry; restoration does not repin.
- Blank rename restores the original name.
- Reordering works across page boundaries.
- Temporarily unavailable apps retain their pin and fail safely.
- Android Home returns to page one from a multi-page home.
- Home resumes after sleep and wake.
- Launching an app and returning with Android Home works.
- Revoked notification access offers optional setup without blocking home.

The grayscale-icon toggle and Android’s 200% text setting were also checked on screen. Screenshots capture the actual app, with only built-in emulator apps and test aliases.

## Performance observations

Environment: Android 11 / API 30, arm64 emulator 37.2.12, 824 × 1648 pixels, logical density 320, SwiftShader software graphics. Both versions used the same emulator, five pinned apps, and the existing debug signing configuration. Build toolchain: JDK 17, Android platform/build tools 36.

| Measurement | Original | Sumi 2.0 |
| --- | ---: | ---: |
| Debug APK size | 65,111,517 bytes (62.10 MiB) | 33,981,701 bytes (32.41 MiB) |
| Process-cold first-frame startup, median of 6 runs | 512 ms | 378.5 ms |
| Frames rendered during 5 library open/close cycles | 478 | 58 |
| Frames rendered during an 8-second idle home sample | Not sampled | 0 |

The APK is **47.8% smaller**. In this sample, first-frame startup was **26.1% faster**, and library navigation rendered **87.9% fewer frames** after removing animated transitions and default icon loading.

Startup measurements use `am force-stop` followed by `am start -W`; `TotalTime` measures the first drawn frame, not full interactivity or a device boot. Navigation measurements use the same five left/right swipe cycles, with `dumpsys gfxinfo` reset immediately before each sample. The idle sample stayed between minute boundaries. Software-renderer frame timing is not a physical e-ink refresh measurement; individual jank percentages remain high in this emulator and do not establish Palma smoothness.

Raw samples:

- Original startup: 570, 506, 487, 518, 493, 524 ms.
- Sumi 2.0 startup: 395, 364, 360, 384, 378, 379 ms.
- Original navigation: 478 frames; 332 reported janky frames; 13 slow-UI-thread frames.
- Sumi 2.0 navigation: 58 frames; 37 reported janky frames; 10 slow-UI-thread frames.
- Sumi 2.0 idle home: 0 frames in 8 seconds.

The clock’s lifecycle gating is implemented with `repeatOnLifecycle(RESUMED)`, so its minute timer and time-change receiver stop when the home navigation entry is no longer resumed. Installed-app enumeration and display sorting run off the UI thread. Optional icons load asynchronously into a bounded 4 MiB cache, with package-change invalidation.

## Hardware acceptance and installation

A physical BOOX Palma was not connected. Check readability, ghosting, refresh settings, page changes, and sleep/wake on the target Palma before release. BOOX controls display refresh; the launcher does not install a proprietary display SDK or simulate a full refresh.

The delivered APK is a **debug build**, package `com.sumilauncher`, version `2.0.0` / code `2`, minimum Android 11. An in-place upgrade requires the same signing certificate as the installed app. The existing debug signing setup was retained; no release signing key is included.
