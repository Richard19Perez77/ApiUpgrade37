# API 37 Lab

Android Studio starter turned into a **hands-on catalog of Android 17 (API 37 / `CINNAMON_BUN`)**.

The app targets **SDK 37**, compiles against platform `android-37.0`, and keeps **minSdk 24** so every sample is wrapped in version checks. Patterns that used to work on 24–36 are left in comments next to the new code.

## What to open first

| Doc / screen | Why |
| --- | --- |
| [OVERVIEW.md](OVERVIEW.md) | Feature map, behavior changes, migration table |
| In-app **Catalog** tab | Runnable demos |
| **Adaptive** tab | Large-screen / bubble / desktop-window layout (`BoxWithConstraints`) |
| **Behaviors** tab | Target-37 breaking changes (MessageQueue, `static final`, DCL, SMS OTP, NPU) |

## Requirements

- Android Studio with **API 37** platform (`platforms/android-37.0`)
- Device or emulator running **Android 17** for APIs that are not stubbed
- JDK 11+ (the wrapper uses the Studio JBR)

```bash
./gradlew :app:assembleDebug
```

On Windows: `.\gradlew.bat :app:assembleDebug`

## Project map

```
app/src/main
├── AndroidManifest.xml          target 37 rules, ACCESS_LOCAL_NETWORK, NPU feature
├── res/xml/network_security_config.xml   ECH + Certificate Transparency
└── java/com/rick/apiupgrade37
    ├── ApiUpgrade37App.kt       ProfilingManager API 37 triggers
    ├── MainActivity.kt          Handoff / Continue On + edge-to-edge
    ├── core/AndroidApis.kt      CINNAMON_BUN helpers
    ├── jobs/DebugSampleJobService.kt
    └── ui/screens/*             One screen per platform area
```

## How samples are written

1. **New API 37 path runs** when `Build.VERSION.SDK_INT >= VERSION_CODES.CINNAMON_BUN`.
2. **Older path is commented or branched**, with the API level that introduced it.
3. Manifest attributes that **stop working on large screens** when you target 37 are documented in XML comments, not used.

Official references:

- https://developer.android.com/about/versions/17/features
- https://developer.android.com/about/versions/17/behavior-changes-17
- https://developer.android.com/about/versions/17/summary
