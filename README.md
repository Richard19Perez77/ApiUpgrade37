# API 37 Lab

Hands-on catalog of **Android 17 (API 37 / `Build.VERSION_CODES.CINNAMON_BUN`)**.

| Gradle | Value |
| --- | --- |
| `compileSdk` | 37 |
| `targetSdk` | 37 |
| `minSdk` | 24 |

Live code is the API 37 path. Older 24–36 patterns are **comments** next to it, not `TODO()` stubs.

## Start here

1. [OVERVIEW.md](OVERVIEW.md) — what changed in 17 and how to migrate.
2. Run the app → **Catalog**, **Adaptive**, **Behaviors**.
3. Open the matching file under `app/src/main/java/com/rick/apiupgrade37/ui/screens/`.

## Build

Need the **API 37** platform (`platforms/android-37.0`) and JDK 11+. An Android 17 emulator or Pixel is required for APIs that do not exist on older images.

```bash
# Windows
.\gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

## How samples are written

Use `AndroidApis.isAndroid17` (annotated with `@ChecksSdkIntAtLeast`). Lint then treats the `if` as an API 37 gate.

```kotlin
if (AndroidApis.isAndroid17) {
    // New API 37 call
} else {
    // Pre-37: Intent.ACTION_PICK / READ_CONTACTS / …
}
```

| Do | Don't |
| --- | --- |
| `@RequiresApi(CINNAMON_BUN)` on methods the **platform only calls on 37+** (example: `onHandoffActivityDataRequested`) | `@RequiresApi` on `Activity.onCreate` / `Application.onCreate` — those still run on API 24 |
| Comment the old API in the `else` (or omit the branch) | Accept Studio's `TODO("VERSION.SDK_INT < …")` — that throws at runtime |
| Comment ignored manifest attrs (`screenOrientation`, `resizeableActivity=false`) | Rely on orientation locks once you target 37 on large screens |

If Studio offers a second `SDK_INT >= CINNAMON_BUN` check **inside** an `isAndroid17` block, decline it.

## Layout of this repo

```
ApiUpgrade37/
├── OVERVIEW.md
├── README.md
└── app/src/main
    ├── AndroidManifest.xml              ACCESS_LOCAL_NETWORK, NPU feature, adaptive comments
    ├── res/xml/network_security_config.xml   ECH + Certificate Transparency
    ├── res/xml/data_extraction_rules.xml     API 31+ backup
    └── java/com/rick/apiupgrade37
        ├── ApiUpgrade37App.kt           ProfilingManager triggers
        ├── MainActivity.kt              edge-to-edge + Handoff
        ├── core/AndroidApis.kt          CINNAMON_BUN + ChecksSdkIntAtLeast
        ├── jobs/DebugSampleJobService.kt
        └── ui/
            ├── Catalog.kt               Catalog rows
            ├── Api37App.kt              Catalog / Adaptive / Behaviors
            └── screens/                 One demo per catalog item
```

## Catalog → source

| In-app card | File |
| --- | --- |
| Adaptive layouts | `AdaptiveLayoutsScreen.kt` |
| Contacts picker | `ContactsPickerScreen.kt` |
| Eyedropper | `EyeDropperScreen.kt` |
| Local network | `LocalNetworkScreen.kt` |
| Photo picker | `PhotoPickerScreen.kt` |
| Advanced Protection | `AdvancedProtectionScreen.kt` |
| ECH + CT | `NetworkSecurityScreen.kt` |
| Profiling triggers | `ProfilingScreen.kt` |
| JobScheduler stats | `JobSchedulerScreen.kt` |
| Idle alarm listener | `AlarmListenerScreen.kt` |
| Memory limiter | `MemoryLimitsScreen.kt` |
| Live Update colors | `LiveUpdateScreen.kt` |
| MetricStyle | `MetricStyleScreen.kt` |
| Camera & media | `CameraMediaScreen.kt` |
| Hearing aids | `HearingAidScreen.kt` |
| UWB DL-TDoA | `UwbRangingScreen.kt` |
| AppFunctions | `AppFunctionsScreen.kt` |
| CJKV IME a11y | `AccessibilityImeScreen.kt` |
| Target-37 behaviors | `BehaviorChangesScreen.kt` |

This lab uses `BoxWithConstraints` + wrapping chips so it stays on the default Compose BOM. Production apps should use `NavigationSuiteScaffold` (material3-adaptive-navigation-suite) for bottom bar ↔ rail.

## Docs

- https://developer.android.com/about/versions/17/features
- https://developer.android.com/about/versions/17/behavior-changes-17
- https://developer.android.com/about/versions/17/summary
