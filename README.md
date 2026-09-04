# API 37 Lab

A runnable catalog of the APIs that are new, or newly enforced, in Android 17 (API level 37, dessert code `CINNAMON_BUN`).

Every screen in the app demonstrates one platform area. The live code always takes the API 37 path; the way the same thing was done on API 24–36 is kept beside it as a comment, so you can read both without switching branches.

If you only read one other file, read [OVERVIEW.md](OVERVIEW.md). It is the feature-by-feature tour of what changed in Android 17.

## Toolchain

| Setting | Value |
| --- | --- |
| `compileSdk` / `targetSdk` | 37 |
| `minSdk` | 24 |
| Android Gradle Plugin | 9.3.2 |
| Gradle | 9.5.0 |
| JDK that runs the build | 25 |
| Java language level for app code | 11 |

Studio's offer to move Gradle past 9.5.0 is optional; AGP 9.3 works with it as shipped.

The two Java numbers are unrelated and often confused. The JDK that *runs* Gradle is pinned to 25 by `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=25`), which also carries download URLs so Gradle can fetch that JDK itself if the machine lacks it — you do not need to install one to build. The `11` is the `compileOptions` language level the *app's own* bytecode targets, set in `app/build.gradle.kts`. Changing one does not change the other. Run `gradlew --version` to see which JDK the daemon actually picked.

You also need the API 37 platform installed (`platforms/android-37.0`). Several demos only produce real output on an Android 17 device or emulator; on older images they show an explanatory message instead.

```bash
# Windows
.\gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

## Finding your way around the app

The app opens on three tabs:

- **Catalog** lists every demo. Tapping a card opens that screen.
- **Adaptive** shows how the layout reflows, which is the change most likely to break an existing app once it targets 37.
- **Behaviors** is a written summary of the target-37 changes that have no UI, such as the lock-free `MessageQueue` and frozen `static final` fields.

## Conventions used in the samples

Version checks go through `AndroidApis.isAndroid17`, which is annotated with `@ChecksSdkIntAtLeast`. That annotation is what tells lint the property is an API 37 gate, so a plain `if` is enough:

```kotlin
if (AndroidApis.isAndroid17) {
    // API 37 call
} else {
    // Pre-37: Intent.ACTION_PICK, READ_CONTACTS, and so on
}
```

Two mistakes are easy to make here, and both were made while building this project.

**Do not put `@RequiresApi` on `onCreate`.** The annotation only informs lint; it does not stop the platform from calling the method on API 24. Reserve it for methods the system invokes only on 37 and later, such as `onHandoffActivityDataRequested`. Guard everything else with `isAndroid17` inside the method body.

**Do not accept the `TODO("VERSION.SDK_INT < …")` quick fix.** Studio offers it when it cannot prove a version check is in place, but `TODO()` throws `NotImplementedError` at runtime. Write the old API as a comment instead, or return a sensible fallback value. If Studio suggests a second `SDK_INT` check *inside* a block already guarded by `isAndroid17`, decline it — the `@ChecksSdkIntAtLeast` annotation covers it.

There is one repetition that looks like that mistake but is not. Several screens disable a button on old devices and then check the version again in the click handler:

```kotlin
Button(
    enabled = AndroidApis.isAndroid17,
    onClick = {
        // `enabled` is not a gate lint understands, so the guard is repeated here.
        if (!AndroidApis.isAndroid17) return@Button
        someApi37Call()
    }
)
```

`enabled = false` stops the user reaching the call, but it is a runtime property of a different composable, so neither lint nor the compiler can use it to prove the call is safe. The guard inside `onClick` is what makes the lambda verifiably API 37 only. Keep both, and write both with `isAndroid17` rather than a raw `Build.VERSION.SDK_INT` comparison.

Manifest attributes that stop working on large screens under target 37, such as `screenOrientation` and `resizeableActivity="false"`, appear only as comments in `AndroidManifest.xml`. They are documented, never relied on.

Two more rules apply across the screens:

**Declaring a permission is not holding it.** Anything that posts a notification goes through `rememberNotificationGate()` in `ui/NotificationGate.kt`, which checks `POST_NOTIFICATIONS` and prompts when it is missing. Skipping it does not throw — `notify()` just does nothing on API 33 and later, which reads as broken API 37 notification code. The exact-alarm demo does the equivalent with `canScheduleExactAlarms()`.

**Keep platform IPC off the composition thread.** Calls like `getHistoricalProcessExitReasons` and `CameraManager.cameraIdList` are binder round-trips. `MemoryLimitsScreen` and `CameraMediaScreen` run them with `produceState` and `Dispatchers.IO` and show a placeholder while they load, rather than blocking inside `remember { }`.

## Repository layout

```
ApiUpgrade37/
├── OVERVIEW.md
├── README.md
└── app/src/main
    ├── AndroidManifest.xml                    ACCESS_LOCAL_NETWORK, NPU feature, adaptive notes
    ├── res/xml/network_security_config.xml    ECH and Certificate Transparency
    ├── res/xml/data_extraction_rules.xml      Backup rules for API 31+
    └── java/com/rick/apiupgrade37
        ├── ApiUpgrade37App.kt                 ProfilingManager triggers
        ├── MainActivity.kt                    Edge-to-edge and Handoff
        ├── core/AndroidApis.kt                CINNAMON_BUN gate and @ChecksSdkIntAtLeast
        ├── jobs/DebugSampleJobService.kt      Job used by the JobScheduler demo
        └── ui/
            ├── Api37App.kt                    Tab chrome and routing
            ├── Catalog.kt                     The catalog rows
            ├── FeatureScaffold.kt             Shared top bar and body for every demo
            ├── NotificationGate.kt            POST_NOTIFICATIONS runtime grant
            └── screens/                       One file per demo
```

## Catalog entries and their source files

| Card in the app | Source file |
| --- | --- |
| Adaptive layouts and windowing | `AdaptiveLayoutsScreen.kt` |
| System contacts picker | `ContactsPickerScreen.kt` |
| System eyedropper | `EyeDropperScreen.kt` |
| Local network permission | `LocalNetworkScreen.kt` |
| Photo picker aspect ratio | `PhotoPickerScreen.kt` |
| Advanced Protection Mode | `AdvancedProtectionScreen.kt` (an API 36 feature, kept for context) |
| ECH and Certificate Transparency | `NetworkSecurityScreen.kt` |
| ProfilingManager triggers | `ProfilingScreen.kt` |
| JobScheduler debug stats | `JobSchedulerScreen.kt` |
| Allow-while-idle alarm listener | `AlarmListenerScreen.kt` |
| Memory limiter and exit info | `MemoryLimitsScreen.kt` |
| Live Update semantic colors | `LiveUpdateScreen.kt` |
| MetricStyle notifications | `MetricStyleScreen.kt` |
| Camera and media | `CameraMediaScreen.kt` |
| BLE hearing aids | `HearingAidScreen.kt` |
| UWB downlink TDoA | `UwbRangingScreen.kt` |
| AppFunctions | `AppFunctionsScreen.kt` |
| CJKV IME accessibility | `AccessibilityImeScreen.kt` |
| Target-37 behavior changes | `BehaviorChangesScreen.kt` |

## A note on the navigation code

The tab bar is built from wrapping `FilterChip`s and the two-pane layout from `BoxWithConstraints`, which keeps the project on the stock Compose BOM with no extra dependencies. A production app should use `NavigationSuiteScaffold` from `material3-adaptive-navigation-suite`, which switches between a bottom bar and a navigation rail on its own. The comments in `Api37App.kt` and `AdaptiveLayoutsScreen.kt` say the same thing where the code lives.

## Official documentation

- [Features and APIs](https://developer.android.com/about/versions/17/features)
- [Behavior changes for apps targeting Android 17](https://developer.android.com/about/versions/17/behavior-changes-17)
- [Full list of Android 17 changes](https://developer.android.com/about/versions/17/summary)
