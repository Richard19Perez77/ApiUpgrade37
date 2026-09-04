# What changed in Android 17 (API 37)

Android 17 is API level 37, dessert code `CINNAMON_BUN`. This document walks through the changes area by area and points at the file in this project that demonstrates each one.

Two kinds of change are mixed together below, and the difference matters:

- Changes that apply **as soon as the device runs Android 17**, whatever your `targetSdk`.
- Changes that only apply **once you set `targetSdk = 37`**. These are the ones that break working apps at upgrade time, and they are collected in the Behaviors tab of the app.

The conventions used in the sample code (`AndroidApis.isAndroid17`, where `@RequiresApi` belongs, why there are no `TODO()` stubs) are described in [README.md](README.md).

Official sources: [Features and APIs](https://developer.android.com/about/versions/17/features), [behavior changes for apps targeting 17](https://developer.android.com/about/versions/17/behavior-changes-17), and the [full change list](https://developer.android.com/about/versions/17/summary).

---

## Adaptive layouts

This is the largest change and the most likely to affect an existing app.

Once you target 37, large screens — anything with a smallest width above 600dp, including a phone driving a connected display in desktop mode — ignore the constraints apps have historically used to stay in portrait:

- `android:screenOrientation` in the manifest
- `Activity.setRequestedOrientation()` at runtime
- `android:resizeableActivity="false"`
- `minAspectRatio` and `maxAspectRatio`

Games, identified by their Play app category, remain exempt. Every other app has to reflow to whatever window size it is given.

Android 17 also adds windowing surfaces your layout has to survive:

| Surface | What it means for your UI |
| --- | --- |
| App Bubbles | The user can turn any app into a small floating window by long-pressing its launcher icon |
| Bubble Bar | A dock in the tablet and foldable taskbar for organising those bubbles |
| Desktop interactive Picture-in-Picture | Unlike the classic read-only PiP, the pinned window still accepts input |

Activity recreation changed as well. Configuration changes for keyboard, `keyboardHidden`, navigation, touchscreen, and `colorMode` no longer restart the activity by default; they arrive at `onConfigurationChanged()` instead. If your app genuinely depends on a restart to reload resources, opt back in with the new `android:recreateOnConfigChanges` attribute, and be careful not to also list those flags in `configChanges` — `configChanges` wins.

In this project: `AdaptiveLayoutsScreen.kt`, `MainActivity.onConfigurationChanged`, and the commented attributes in `AndroidManifest.xml`.

---

## Privacy

The theme is replacing broad, permanent permissions with system-rendered pickers that grant access to exactly what the user chose, for that session only.

| New in API 37 | What it replaces | Demo screen |
| --- | --- | --- |
| `ContactsPickerSessionContract.ACTION_PICK_CONTACTS` | Holding `READ_CONTACTS` to read the whole address book | Contacts picker |
| `Intent.ACTION_OPEN_EYE_DROPPER`, returning `Intent.EXTRA_COLOR` | Screen capture or MediaProjection just to sample one pixel | Eyedropper |
| `ACCESS_LOCAL_NETWORK`, part of the NEARBY_DEVICES group | Reaching LAN devices with nothing but `INTERNET` | Local network |
| `PhotoPickerUiCustomizationParams` for 9:16 or 1:1 thumbnails | An app-built gallery grid | Photo picker |
| Encrypted Client Hello, configured with `<domainEncryption>` | A cleartext SNI in the TLS handshake | ECH and CT |
| Certificate Transparency on by default | Opting in per-domain, as you had to on API 36 | ECH and CT |

Two more privacy changes have no demo screen. A system-rendered location button grants precise location for the current session only, and password fields no longer echo the last typed character when a hardware keyboard is attached.

SMS one-time passwords are now delayed by three hours for apps targeting 37 that are not the default SMS app, the assistant, or a connected companion app. If you read OTPs, move to the SMS Retriever or SMS User Consent APIs.

---

## Performance and runtime

Several of these are silent: nothing warns you at build time, and the failure only appears at runtime after you bump `targetSdk`.

- `android.os.MessageQueue` becomes lock-free. It is faster, but any code that reflects on the queue's private fields breaks. Instrumentation tests should use `TestLooperManager.peekWhen()` and `poll()` instead; `ExampleInstrumentedTest.kt` carries the same warning.
- `static final` fields can no longer be modified. Reflection now throws `IllegalAccessException`, and the JNI `SetStatic*Field` family crashes the process. This rules out the common trick of patching `Build.VERSION.SDK_INT` in unit tests; use Robolectric shadows or your own wrapper. `ExampleUnitTest.kt` shows the idiom that stops working and why `AndroidApis` is the first step toward a testable one.
- The system enforces per-app memory limits based on total device RAM and terminates processes that exceed them. When that happens, `ApplicationExitInfo.getDescription()` may contain `MemoryLimiter:AnonSwap` — treat it as a diagnostic hint rather than a stable contract to parse.
- `ProfilingManager`, which arrived in API 35, gains four triggers: `TRIGGER_TYPE_COLD_START`, `TRIGGER_TYPE_OOM`, `TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE`, and `TRIGGER_TYPE_ANOMALY`. The anomaly trigger is the useful one for memory limits, because it can hand you a heap dump before the system kills the process. Note that `TRIGGER_TYPE_OOM` only works if your uncaught exception handler calls through to the default one.
- `JobScheduler.getPendingJobReasonStats(jobId)` returns a map of pending reason to cumulative `Duration`, folding together `getPendingJobReason` from API 34 and the reason history added in API 36.
- `AlarmManager.setExactAndAllowWhileIdle` gains an overload taking an `Executor` and an `OnAlarmListener` instead of a `PendingIntent`. It suits apps that were holding a wake lock to run a short periodic task, such as a socket keepalive. The exact-alarm permission rules are unchanged, so check `canScheduleExactAlarms()` before calling either form; see the traps section.
- ART adds generational garbage collection, with frequent young-generation sweeps in place of full-heap scans. This also reaches API 31 and above through Play system updates.
- Custom notification views are held to stricter memory limits under target 37, closing a bypass that used URIs.
- Safer dynamic code loading extends to native libraries. A `.so` passed to `System.load` must be marked read-only or the call throws `UnsatisfiedLinkError`. DEX and JAR files have had this requirement since API 34.

In this project: `ApiUpgrade37App.kt`, `ProfilingScreen.kt`, `JobSchedulerScreen.kt`, `AlarmListenerScreen.kt`, `MemoryLimitsScreen.kt`, and `BehaviorChangesScreen.kt`.

---

## Notifications, audio, and continuity

Handoff, branded Continue On, lets a user start a task on one device and pick it up on another. Enable it per activity by calling `setHandoffEnabled(true, params)` when the screen is ready to be handed off, then override `onHandoffActivityDataRequested()` to return a `HandoffActivityData`. Extras travel in a `PersistableBundle` and must stay under roughly 50KB. You can set a `fallbackUri` for devices without your app installed, or use `HandoffActivityData.createWebHandoff()` for a web-only handoff.

Live Updates gain semantic colours with fixed meanings — `SEMANTIC_STYLE_SAFE`, `CAUTION`, `DANGER`, and `INFO` — applied to spans through `Notification.createSemanticStyleAnnotation()`, or to progress points and segments through `setSemanticStyle()`. Pair them with `setRequestPromotedOngoing(true)` to ask the system to promote the notification.

`Notification.MetricStyle` is a new template for health, fitness, timer, and travel readouts, where each metric carries a value, a label, and an optional semantic style.

On the audio side, the assistant now has its own volume stream, `STREAM_ASSISTANT`, so assistant playback is no longer tied to media volume, and assistant-role apps can enter `MODE_ASSISTANT_CONVERSATION`. `AudioDeviceInfo.TYPE_BLE_HEARING_AID` finally distinguishes Bluetooth LE hearing aids from ordinary LE headsets. Note that `USAGE_ASSISTANT` itself is old, dating to API 26.

For accessibility, `AccessibilityEvent.setTextChangeTypes()` lets an IME tell a screen reader whether CJKV text is still being composed, has had a conversion candidate selected, or has been committed.

None of the notification work above reaches the screen without a `POST_NOTIFICATIONS` grant, which has been a runtime permission since API 33 and is therefore required on every Android 17 device. Both notification samples call `rememberNotificationGate()` first; see the traps section below.

In this project: `MainActivity.kt`, `LiveUpdateScreen.kt`, `MetricStyleScreen.kt`, `HearingAidScreen.kt`, and `AccessibilityImeScreen.kt`.

---

## Camera and media

- `CameraCharacteristics.INFO_DEVICE_TYPE` reports whether a camera is built in, an external USB webcam, or virtual. Before 37 the closest signal was `INFO_SUPPORTED_HARDWARE_LEVEL`.
- `ImageFormat.RAW14` adds a 14-bit Bayer format for professional capture.
- `MediaFormat.MIMETYPE_VIDEO_VVC` lets device makers expose H.266 codecs.
- `MediaRecorder.setVideoEncodingQuality()` configures constant-quality encoding; previously you could only set a bitrate.
- Ultra-wideband gains downlink TDoA ranging through `RangingManager`, which locates a device against several anchors by comparing signal arrival times.
- If you use CameraX on Android 17, move to 1.5.2 or 1.6.0 and later to avoid a crash related to an added dynamic range mode.

In this project: `CameraMediaScreen.kt` and `UwbRangingScreen.kt`. The UWB screen only queries capabilities, since starting a session needs FiRa configuration bytes from real anchors.

---

## Security and on-device intelligence

Advanced Protection Mode is a single switch the user turns on to harden the device: no sideloading, restricted USB data, mandatory Play Protect scanning. Apps query it through `AdvancedProtectionManager` and should hide risky features — custom installers, USB file transfer, debug overlays — while it is enabled. Note that this one is an Android 16 API, not an Android 17 one; gate it on `BAKLAVA`, because checking for `CINNAMON_BUN` would report "off" on a protected Android 16 device. Reading it requires `QUERY_ADVANCED_PROTECTION_MODE`.

For post-quantum readiness, Keystore can generate ML-DSA keys through the standard JCA APIs, and APK Signature Scheme v3.2 pairs a classical signature with an ML-DSA one. If you manage your own signing keys, note that you must generate a new classical key to pair with the PQC key; the existing one cannot be reused.

AppFunctions is the platform side of Android MCP: apps register capabilities that on-device agents can discover and call. This project uses the platform `AppFunctionManager.registerAppFunction`, while most production apps will use the Jetpack library, where an `@AppFunction` annotation and KDoc generate the equivalent plumbing.

Finally, apps targeting 37 that talk to the NPU — through the LiteRT NPU delegate, a vendor SDK, or the deprecated NNAPI — must declare `FEATURE_NEURAL_PROCESSING_UNIT` in the manifest or risk being blocked.

In this project: `AdvancedProtectionScreen.kt`, `AppFunctionsScreen.kt`, and the `uses-feature` entry in `AndroidManifest.xml`.

---

## API levels referenced in this project

| API | Dessert | Why it appears here |
| --- | --- | --- |
| 24 | Nougat | The project's `minSdk` |
| 26 | Oreo | Notification channels, `USAGE_ASSISTANT` |
| 31 | S | Exact alarm policy, `GenericDocument` |
| 33 | Tiramisu | Photo Picker, `POST_NOTIFICATIONS` |
| 34 | Upside Down Cake | Safer DEX loading, `getPendingJobReason` |
| 35 | Vanilla Ice Cream | `ProfilingManager` |
| 36 | Baklava | Opt-in Certificate Transparency, pending job history, `RangingManager`, `AdvancedProtectionManager` |
| 37 | Cinnamon Bun | Everything else in this document |

---

## Traps worth remembering

These all cost time while building this project.

**A manifest permission is not a granted permission.** Two of them bite on any Android 17 device, because both predate 17 and are easy to assume are handled. `POST_NOTIFICATIONS` has been a runtime permission since API 33, so `NotificationManager.notify()` silently does nothing until the user grants it — the notification demos look broken when the notification code is fine. `SCHEDULE_EXACT_ALARM` has been a special app access since API 31, and on devices running Android 14 or later it is no longer pre-granted to apps targeting API 33 or later, unless the app is a clock or calendar or otherwise exempt. Declaring it and calling `setExact…` anyway throws `SecurityException`. Call `AlarmManager.canScheduleExactAlarms()` first and route the user through `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` when it returns false. Because that sends the user to a settings page, re-check on resume rather than assuming the grant, and keep the `try`/`catch`: the permission can be revoked between your check and your call. Apps that can tolerate imprecision should degrade to `setWindow()` instead of asking.

**`@RequiresApi` does not gate anything at runtime.** Putting it on `Activity.onCreate` does not stop the method being called on API 24; it only silences lint and hides the real problem. Use a runtime check, and save the annotation for callbacks the platform genuinely invokes only on 37 and later.

**The `DnsResolver.query` overload** that returns an `HttpsEndpoint` takes query *flags* in its third parameter, not a record type. Pass `FLAG_EMPTY` there, or one of `FLAG_NO_CACHE_LOOKUP`, `FLAG_NO_CACHE_STORE`, and `FLAG_NO_RETRY`. The HTTPS record type is implied by the callback type. `TYPE_HTTPS` belongs to the older overload that returns a list of `InetAddress`, and passing it as a flag compiles but asks for something quite different.

---

## Where each topic lives

| File | Topic |
| --- | --- |
| `core/AndroidApis.kt` | The `CINNAMON_BUN` gate and `@ChecksSdkIntAtLeast` |
| `AndroidManifest.xml` | Local network permission, NPU feature, ignored orientation attributes, `recreateOnConfigChanges` |
| `res/xml/network_security_config.xml` | `<domainEncryption>` and `<certificateTransparency>` |
| `res/xml/data_extraction_rules.xml`, `backup_rules.xml` | Backup includes and excludes |
| `ApiUpgrade37App.kt` | Profiling triggers, with `@RequiresApi` on the private helper rather than `onCreate` |
| `MainActivity.kt` | Handoff override alongside an ungated `onCreate` |
| `ui/NotificationGate.kt` | The `POST_NOTIFICATIONS` runtime grant both notification samples need |
| `ui/screens/*.kt` | One file per catalog entry; the mapping is in the README |
| `app/src/test/…/ExampleUnitTest.kt` | Why `static final` being frozen breaks SDK_INT patching |
| `app/src/androidTest/…/ExampleInstrumentedTest.kt` | Checking a version gate against a real device |
| `app/build.gradle.kts` | AGP 9 `compileSdk { }` and `optimization { }` syntax |
