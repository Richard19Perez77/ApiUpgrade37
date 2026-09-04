# Android 17 (API 37) overview

Dessert code: **`Build.VERSION_CODES.CINNAMON_BUN`**. Compile and target **37**. This lab is a commented tour of what changed versus Android 16 (API 36 / `BAKLAVA`) and earlier.

Platform docs: [Features](https://developer.android.com/about/versions/17/features) · [Target 37 behaviors](https://developer.android.com/about/versions/17/behavior-changes-17) · [Full list](https://developer.android.com/about/versions/17/summary)

---

## Adaptive-first (the biggest product change)

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

**Do this:** `WindowSizeClass` + `NavigationSuiteScaffold` (this app). **Don't:** lock portrait or ship a single `layout/` XML.

Activity recreation also changed. Keyboard, `keyboardHidden`, navigation, touchscreen, and `colorMode` **no longer restart** the activity by default; you get `onConfigurationChanged()`. Opt back in with `android:recreateOnConfigChanges` (and do not also list those flags in `configChanges`).

See `MainActivity`, `AdaptiveLayoutsScreen`, and comments in `AndroidManifest.xml`.

---

## Privacy

| API 37 | Replaces |
| --- | --- |
| `ContactsPickerSessionContract.ACTION_PICK_CONTACTS` | Broad `READ_CONTACTS` |
| `Intent.ACTION_OPEN_EYE_DROPPER` + `Intent.EXTRA_COLOR` | Screenshot / MediaProjection just to sample a pixel |
| `ACCESS_LOCAL_NETWORK` (NEARBY_DEVICES group) | Silent LAN sockets with only `INTERNET` |
| `PhotoPickerUiCustomizationParams` (9:16 / 1:1) | App-owned gallery grid |
| System location button (session-precise) | Permanent fine location for a one-shot |
| Encrypted Client Hello (`<domainEncryption>`) | Clear SNI on TLS ClientHello |
| Certificate Transparency **default on** | Opt-in CT on API 36 |
| Hardware-keyboard password fields hide last character | Last-char peek |

SMS OTPs are delayed **three hours** for apps targeting 37 that are not default SMS / assistant / companion. Use **SMS Retriever** or **User Consent**.

---

## Performance & runtime

- **Lock-free `MessageQueue`** if you target 37. Do not reflect on private queue fields; use `TestLooperManager.peekWhen()` / `poll()`.
- **`static final` is frozen.** Reflection or JNI mutation throws / crashes. Stop patching `SDK_INT` in unit tests.
- **MemoryLimiter** kills apps over device-RAM-based anon+swap. `ApplicationExitInfo.getDescription()` can contain `MemoryLimiter:AnonSwap`. Register `ProfilingTrigger.TRIGGER_TYPE_ANOMALY` for a heap dump *before* death.
- New triggers: `TRIGGER_TYPE_COLD_START`, `TRIGGER_TYPE_OOM`, `TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE`.
- **JobScheduler.getPendingJobReasonStats(jobId)** → `Map<reason, Duration>`.
- **`AlarmManager.setExactAndAllowWhileIdle(..., Executor, OnAlarmListener)`** — drop sticky wakelocks for short idle work.
- ART **young-gen GC** (also via Play system updates back to API 31).
- Custom notification **RemoteViews** memory limits close the URI bypass when targeting 37.
- Native **dynamic code loading**: `System.load` requires a **read-only** `.so` (DEX/JAR already had this from API 34).

---

## UX / notifications / continuity

- **Handoff / Continue On:** `setHandoffEnabled(true, params)` + `onHandoffActivityDataRequested()` returning `HandoffActivityData` (extras ≤ ~50KB, optional `fallbackUri` or `createWebHandoff`).
- **Live Update semantic colors:** `SEMANTIC_STYLE_SAFE | CAUTION | DANGER | INFO` via `Notification.createSemanticStyleAnnotation`.
- **`Notification.MetricStyle`** for heart-rate / timer / travel tiles.
- **`STREAM_ASSISTANT` / `MODE_ASSISTANT_CONVERSATION`** so assistant volume is not media volume.
- **CJKV IME a11y:** `AccessibilityEvent.setTextChangeTypes(...)`.

---

## Media, camera, ranging

- `CameraCharacteristics.INFO_DEVICE_TYPE` → built-in / USB / virtual
- `ImageFormat.RAW14`
- `MediaFormat.MIMETYPE_VIDEO_VVC` (H.266)
- `MediaRecorder.setVideoEncodingQuality` (constant quality)
- `AudioDeviceInfo.TYPE_BLE_HEARING_AID`
- UWB **DL-TDoA** on `RangingManager`
- CameraX **1.5.2 or 1.6.0+** on Android 17 (dynamic-range crash otherwise)
- Constrained **satellite** networks (also in 16 QPR2)

---

## Security & intelligence

- **AdvancedProtectionManager** — user opt-in: no sideload, USB data off, Play Protect required. Hide high-risk features when enabled.
- **ML-DSA** in Keystore; **APK Signature Scheme v3.2** hybrid (new classical key + PQC; cannot reuse the old signing key).
- **AppFunctions / Android MCP** — register app tools for on-device agents (`AppFunctionManager` or Jetpack `@AppFunction`).
- Declare **`FEATURE_NEURAL_PROCESSING_UNIT`** if you use LiteRT NPU / vendor NPU / NNAPI while targeting 37.

---

## Version cheat sheet

| API | Dessert | This lab |
| --- | --- | --- |
| 24 | N | minSdk |
| 31 | S | Photo picker precursors, exact alarms |
| 33 | Tiramisu | Photo Picker, POST_NOTIFICATIONS |
| 34 | UpsideDownCake | Safer DEX DCL, `getPendingJobReason` |
| 35 | VanillaIceCream | ProfilingManager |
| 36 | Baklava | CT opt-in, pending-job history |
| **37** | **Cinnamon Bun** | Everything in this document |

---

## File → topic

| File | Topic |
| --- | --- |
| `AndroidManifest.xml` | Local network, NPU, ignored orientation attrs, `recreateOnConfigChanges` |
| `network_security_config.xml` | ECH + CT |
| `ApiUpgrade37App.kt` | Profiling triggers |
| `MainActivity.kt` | Handoff |
| `ui/screens/*.kt` | One demo per row of the in-app catalog |
