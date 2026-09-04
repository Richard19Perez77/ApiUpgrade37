# Android 17 (API 37) overview

Dessert code: **`Build.VERSION_CODES.CINNAMON_BUN`**. This lab **compiles and targets 37**, `minSdk` **24**.

Platform: [Features](https://developer.android.com/about/versions/17/features) · [Apps targeting 17](https://developer.android.com/about/versions/17/behavior-changes-17) · [Full list](https://developer.android.com/about/versions/17/summary)

How to read the samples is in [README.md](README.md) (`AndroidApis.isAndroid17`, `@RequiresApi`, never `TODO()` else-branches).

---

## Adaptive-first (largest product change)

Once `targetSdk` is 37, **large screens (`sw > 600dp`)** — including a phone in **desktop / connected-display mode** — **ignore**:

- `android:screenOrientation`
- `Activity.setRequestedOrientation()`
- `android:resizeableActivity="false"`
- `minAspectRatio` / `maxAspectRatio`

**Games** (Play app category) stay exempt. Everyone else must reflow.

Android 17 also expands multitasking:

| Surface | What your UI must survive |
| --- | --- |
| App Bubbles | Any app can become a tiny floating window from a long-press on the launcher icon |
| Bubble Bar | Tablet/foldable taskbar dock for those bubbles |
| Desktop interactive PiP | Unlike classic PiP, the window stays **interactive** |

**This lab:** `BoxWithConstraints` (600.dp two-pane) in `AdaptiveLayoutsScreen`. **Production:** `WindowSizeClass` + `NavigationSuiteScaffold`. **Don't:** lock portrait or ship a single phone `layout/`.

Activity recreation: keyboard, `keyboardHidden`, navigation, touchscreen, and `colorMode` **no longer restart** the activity by default; you get `onConfigurationChanged()`. Opt back in with `android:recreateOnConfigChanges` (and do **not** also list those flags in `configChanges`).

See `MainActivity`, `AdaptiveLayoutsScreen`, and comments in `AndroidManifest.xml`.

---

## Privacy

| API 37 | Replaces | Screen |
| --- | --- | --- |
| `ContactsPickerSessionContract.ACTION_PICK_CONTACTS` | Broad `READ_CONTACTS` | Contacts |
| `Intent.ACTION_OPEN_EYE_DROPPER` + `Intent.EXTRA_COLOR` | Screenshot / MediaProjection to sample a pixel | Eyedropper |
| `ACCESS_LOCAL_NETWORK` (NEARBY_DEVICES group) | Silent LAN sockets with only `INTERNET` | Local network |
| `PhotoPickerUiCustomizationParams` (9:16 / 1:1) | App-owned gallery grid | Photo picker |
| System location button (session-precise) | Permanent fine location for a one-shot | (documented only) |
| Encrypted Client Hello (`<domainEncryption mode="opportunistic\|enabled\|disabled"/>`) | Clear SNI on TLS ClientHello | ECH + CT |
| Certificate Transparency **default on** | Opt-in CT on API 36 | `network_security_config.xml` |
| Hardware-keyboard password fields hide last character | Last-char peek | Behaviors |

SMS OTPs are delayed **three hours** for apps targeting 37 that are not default SMS / assistant / companion. Use **SMS Retriever** or **User Consent**.

---

## Performance & runtime

- **Lock-free `MessageQueue`** when you target 37. Do not reflect on private queue fields; tests use `TestLooperManager.peekWhen()` / `poll()`.
- **`static final` is frozen.** Reflection or JNI mutation throws / crashes. Do not patch `SDK_INT` in unit tests.
- **MemoryLimiter** kills apps over device-RAM-based anon+swap. `ApplicationExitInfo.getDescription()` can contain `MemoryLimiter:AnonSwap`. Register `ProfilingTrigger.TRIGGER_TYPE_ANOMALY` for a heap dump *before* death (`ApiUpgrade37App`).
- New triggers: `TRIGGER_TYPE_COLD_START`, `TRIGGER_TYPE_OOM`, `TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE`.
- **`JobScheduler.getPendingJobReasonStats(jobId)`** → `Map<reason, Duration>` (API 34 had `getPendingJobReason`; API 36 added history).
- **`AlarmManager.setExactAndAllowWhileIdle(..., Executor, OnAlarmListener)`** — drop sticky wakelocks for short idle work. Pre-37 used a `PendingIntent`.
- ART **young-gen GC** (also via Play system updates back to API 31).
- Custom notification **RemoteViews** memory limits close the URI bypass when targeting 37.
- Native **dynamic code loading**: `System.load` requires a **read-only** `.so` (DEX/JAR already had this from API 34).

---

## UX / notifications / continuity

- **Handoff / Continue On:** `setHandoffEnabled(true, params)` in `onCreate` (gated), then `@RequiresApi(CINNAMON_BUN) onHandoffActivityDataRequested()` returning `HandoffActivityData` (extras ≤ ~50KB, optional `fallbackUri` or `createWebHandoff`). Do **not** annotate `onCreate` with `@RequiresApi`.
- **Live Update semantic colors:** `SEMANTIC_STYLE_SAFE | CAUTION | DANGER | INFO` via `Notification.createSemanticStyleAnnotation`, plus `setRequestPromotedOngoing(true)`.
- **`Notification.MetricStyle`** for heart-rate / timer / travel tiles.
- **`STREAM_ASSISTANT` / `MODE_ASSISTANT_CONVERSATION`** so assistant volume is not media volume (`USAGE_ASSISTANT` exists from API 26).
- **CJKV IME a11y:** `AccessibilityEvent.setTextChangeTypes(...)`.

---

## Media, camera, ranging

- `CameraCharacteristics.INFO_DEVICE_TYPE` → built-in / USB / virtual (pre-37: `INFO_SUPPORTED_HARDWARE_LEVEL`)
- `ImageFormat.RAW14`
- `MediaFormat.MIMETYPE_VIDEO_VVC` (H.266)
- `MediaRecorder.setVideoEncodingQuality` (constant quality; pre-37: bitrate only)
- `AudioDeviceInfo.TYPE_BLE_HEARING_AID`
- UWB **DL-TDoA** on `RangingManager` (this lab queries capabilities only)
- CameraX **1.5.2 or 1.6.0+** on Android 17 (dynamic-range crash otherwise)
- Constrained **satellite** networks (also in 16 QPR2)

---

## Security & intelligence

- **AdvancedProtectionManager** — user opt-in: no sideload, USB data off, Play Protect required. Hide high-risk features when enabled.
- **ML-DSA** in Keystore; **APK Signature Scheme v3.2** hybrid (new classical key + PQC; cannot reuse the old signing key).
- **AppFunctions / Android MCP** — `AppFunctionManager.registerAppFunction` in this lab; production often uses Jetpack `@AppFunction` + KDoc.
- Declare **`FEATURE_NEURAL_PROCESSING_UNIT`** (`required="false"`) if you use LiteRT NPU / vendor NPU / NNAPI while targeting 37.

---

## Version cheat sheet

| API | Dessert | In this lab |
| --- | --- | --- |
| 24 | N | `minSdk` |
| 26 | O | Notification channels, `USAGE_ASSISTANT` |
| 31 | S | Exact-alarm policy, `GenericDocument` |
| 33 | Tiramisu | Photo Picker, `POST_NOTIFICATIONS` |
| 34 | UpsideDownCake | Safer DEX DCL, `getPendingJobReason` |
| 35 | VanillaIceCream | `ProfilingManager` |
| 36 | Baklava | CT opt-in, `getPendingJobReasonsHistory`, `RangingManager` |
| **37** | **Cinnamon Bun** | Everything else in this document |

---

## File → topic

| File | Topic |
| --- | --- |
| `core/AndroidApis.kt` | `CINNAMON_BUN` + `@ChecksSdkIntAtLeast` |
| `AndroidManifest.xml` | Local network, NPU, ignored orientation attrs, `recreateOnConfigChanges` |
| `network_security_config.xml` | `<domainEncryption>` + `<certificateTransparency>` |
| `data_extraction_rules.xml` / `backup_rules.xml` | Backup includes/excludes |
| `ApiUpgrade37App.kt` | Profiling triggers (`@RequiresApi` on the **private** helper, not `onCreate`) |
| `MainActivity.kt` | Handoff override + ungated `onCreate` |
| `ui/screens/*.kt` | One demo per catalog row (see README) |
