# Android API inventory (API 37 / Android 17)

Snapshot of `compileSdk` / `targetSdk` / `minSdk` across projects under `AndroidStudioProjects`, as of **4 Sep 2026**.

**Rule of thumb:** `compileSdk` unlocks platform APIs at build time. `targetSdk` opts into that release’s **runtime behavior**. Bumping to 37 does not mean the app calls Android 17 APIs.

Most of the “API 37” apps here are **toolchain-current** (AGP 9.3.x, Kotlin 2.2–2.4, Compose, `minSdk` 24). Only a few exercise modern **platform contracts** (edge-to-edge, Bluetooth 12 permissions, typed foreground services). Google’s `platform-samples` folder is the actual newest-API catalog.

---

## Counts (first-party app projects)

| targetSdk | Count | Projects |
|-----------|------:|----------|
| **37** | 14 | TouchMoveCompose, MusicPlayerConstant, BluetoothMusicPlayer, cursor_service, FinnStock, AnimeDB, SportsBetting, MangaPaging, RoomPractice, CardWar, prac1, CodingPractice, Test1, Articles |
| **36** | 2 | Examples, ColorCatch |
| **35** | 1 | IdolGame |
| **34** | 1 | ble-starter-android |
| **33** | 1 | KotlinTouchMove |

Excluded from the count: `spring-boot-learning`, `rick` (non-Android). `Examples` is one project with many modules, all at compile/target 36. `bluetooth/platform-samples` compiles 37 but most modules still **target 35**. `bluetooth/connectivity-samples` are historical (28–33).

---

## What “newest Android” actually means here

### Collected gains of the API 37 cohort

- **AGP 9.3.x DSL:** `compileSdk { version = release(37) }` instead of `compileSdk = 36`. Several modules use `optimization { }` for release instead of `isMinifyEnabled`.
- **Compose compiler plugin:** `org.jetbrains.kotlin.plugin.compose` instead of `composeOptions.kotlinCompilerExtensionVersion`.
- **Language / floor:** Java 11 (some 17). `minSdk` 24. Newest templates (e.g. TouchMoveCompose) use androidx.core 1.19, activity 1.13, lifecycle 2.11, Compose BOM ~2026.02.
- **Runtime contract:**
  - Target **35+** already forces edge-to-edge on Android 15+.
  - Target **36+** drops the theme opt-out.
  - Target **37** also drops large-screen orientation / resizability opt-outs (`sw >= 600dp`) and Android 17 behaviors (local network, lock-free `MessageQueue`, `static final` reflection ban, Bluetooth RFCOMM `read`, ECH).

### Who actually specializes in new platform APIs

| Project | What is specialized |
|---------|---------------------|
| **TouchMoveCompose** | Comments + `enableEdgeToEdge()` for the 35–37 display / large-screen contract. Best “why 37” notes in the repo. |
| **MusicPlayerConstant** | `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `foregroundServiceType="mediaPlayback"`, `POST_NOTIFICATIONS` — required since 33/34, still the modern media pattern on 37. |
| **BluetoothMusicPlayer** / **cursor_service** | `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` with `neverForLocation`; legacy BT perms `maxSdkVersion="30"`. |
| **platform-samples** | Every module `compileSdk = 37` so new symbols resolve. Host app and **live-updates** `targetSdk = 37`. Other samples still target 35 (quicksettings 36). That is how Google teaches APIs without forcing every sample onto the newest behavior set. |

The rest of the 37 apps are **toolchain only**: same numbers, no Android 17-specific calls.

If the goal is to *learn* API 37: study **TouchMoveCompose** (behavior comments), **MusicPlayerConstant** / **BluetoothMusicPlayer** (permissions + FGS), and **platform-samples** (API surface). The other target-37 apps are good AGP 9 templates, not Android 17 feature demos.

---

## Your apps already on 37

| Project | compile / target / min | AGP / Kotlin | DSL | Stack | What is specialized |
|---------|------------------------|--------------|-----|-------|---------------------|
| TouchMoveCompose | 37 / 37 / 24 | 9.3.2 / 2.2.10 | `release(37)` | Compose | Documents Android 17 e2e + large-screen contract |
| MusicPlayerConstant | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose + FGS | mediaPlayback FGS + POST_NOTIFICATIONS |
| BluetoothMusicPlayer | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose + BLE | Split BT12 perms |
| cursor_service | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose + BLE | Same BT12 split |
| FinnStock | 37 / 37 / 24 | 9.3.2 / 2.2.10 | `release(37)` | Compose + Hilt + network | Toolchain only |
| AnimeDB | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose + serialization | Toolchain only |
| SportsBetting | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose + serialization | Toolchain only |
| MangaPaging | 37 / 37 / 24 | 9.3.2 / 2.2.10 | `release(37)` | Compose + paging | Toolchain only |
| RoomPractice | 37 / 37 / 24 | 9.3.2 / 2.2.10 | `release(37)` | Compose + Room | Toolchain only |
| CardWar | 37 / 37 / 24 | 9.3.2 / 2.4.10 | `release(37)` | Compose | Toolchain only |
| prac1 | 37 / 37 / 24 | 9.3.1 / 2.4.10 | `release(37)` | Compose | Toolchain only |
| CodingPractice | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose | Toolchain only |
| Test1 | 37 / 37 / 24 | 9.3.1 / 2.2.10 | `release(37)` | Compose | Toolchain only |
| Articles | 37 / 37 / 24 | 9.3.1 / 2.4.10 | **mixed** integer / `release(37)` | Compose + Hilt + Retrofit | App module still uses `compileSdk = 37`; core/feature use `release(37)` |

Source: each `app/build.gradle.kts` and `gradle/libs.versions.toml`.

---

## Projects below 37

| Project | compile / target / min | AGP | Stack | Gap |
|---------|------------------------|-----|-------|-----|
| Examples | 36 / 36 / 27 | 9.3.0 | Compose / Views mix | 36 → 37 |
| ColorCatch | 36 / 36 / 21 | 8.13.2 | Views + RxJava 1 | 36 → 37 |
| IdolGame | 35 / 35 / 26 | 8.7.3 | Views / canvas | 35 → 37 |
| ble-starter-android | 34 / 34 / 21 | Groovy (Punch Through) | Views + BLE | 34 → 37 |
| KotlinTouchMove | 33 / 33 / 24 | 8.1.0 | Compose BOM 2023.03 + compiler 1.4.3 | 33 → 37 |
| platform-samples (most modules) | 37 / 35 / 23 | 9.2.1 | Google API catalog | target 35 while compiling 37 |
| connectivity-samples | 28–33 / 28–33 / 14–31 | pre-catalog Groovy | Classic Views BLE/Wi-Fi | historical, not a 37 upgrade path |

---

## What older targets do differently (refactor when raising to 37)

An upgrade is **cumulative**: 33 → 37 includes every behavior change from 34, 35, and 36 as well as 37. Tooling (AGP 9 + `compileSdk { version = release(37) }`) is separate from behavior.

### From 36 (Examples, ColorCatch)

Android 17 behaviors only on Examples (AGP already 9.3). ColorCatch also needs tooling.

- Bump `compileSdk` / `targetSdk` to 37. Examples already calls `enableEdgeToEdge()` and uses AGP 9.3.0 with integer `compileSdk = 36` — switch to `release(37)` to match the new apps.
- ColorCatch is Views + Groovy AGP 8.13.2 + RxJava 1 + `minSdk` 21: raise AGP, handle mandatory edge-to-edge insets on the game surface.
- **Examples/contentproviders** still uses `READ`/`WRITE_EXTERNAL_STORAGE` (capped `maxSdk`) and `MediaStore.Images.Media.DATA` path queries. On 33+ that should be `READ_MEDIA_*` / Photo Picker / SAF tree URIs — already required before 37, still broken if you only bump the number.
- **Android 17-specific:** declare `ACCESS_LOCAL_NETWORK` if you talk to LAN devices; stop writing `static final` fields via reflection; do not reflect private `MessageQueue` fields; RFCOMM `BluetoothSocket.read()` returns `-1` when closed; cleartext HTTP is not a free pass — use network security config for local `http://10.0.2.2`.

### From 35 (IdolGame)

Plus Android 16 large-screen rules; portrait lock ignored on tablets.

- `MainActivity` and `Database4Activity` set `screenOrientation` portrait / `fullSensor`. At target **36+** those locks are ignored on large screens (`sw >= 600dp`). Layout must work in landscape/resizable windows.
- The 35→36 edge-to-edge theme opt-out is gone — pad for system bars.
- Then apply the 37 items above.
- AGP 8.7.3 must move to 9.x to match the new projects.

### From 34 (ble-starter-android)

BLE permissions already modern; remaining work is display + toolchain.

- Bluetooth `SCAN`/`CONNECT` vs legacy location is already coded.
- Remaining: `enableEdgeToEdge()` + insets, orientation on large screens, target 35–37 behaviors, AGP/Kotlin/ViewBinding-era dependencies (appcompat 1.6, ktx 1.12).
- `minSdk` 21 vs your 37 apps’ 24.
- FGS types only if you add a connected-device service later (`platform-samples` BLE server already uses `FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE`).

### From 33 (KotlinTouchMove) — largest first-party jump

This is not an SDK bump. Full modernization.

- Replace `composeOptions.kotlinCompilerExtensionVersion` 1.4.3 and Compose BOM 2023.03 with the Kotlin Compose plugin + current BOM.
- Raise Java 8 → 11+.
- Then walk 34 (FGS types, partial media), 35 (edge-to-edge), 36 (no orientation opt-out), 37 (local network / `MessageQueue` / reflection).
- Pair it with **TouchMoveCompose** — same feature, already on the 37 contract.

### Google connectivity-samples (28–33)

Treat as **history**, not an upgrade queue.

- Missing `android:exported` discipline on some eras, `compileSdkVersion` 28 Groovy, `BLUETOOTH` without `SCAN`/`CONNECT`, scoped storage, notification channels, 16 KB page size for any NDK.
- Use **platform-samples** BLE/companion modules (`compileSdk` 37) if the goal is current Bluetooth APIs.

---

## Behavior stack you inherit by raising targetSdk

| When target reaches | You opt into | Hits which of your older apps |
|---------------------|--------------|-------------------------------|
| **33** | `POST_NOTIFICATIONS`; `READ_MEDIA_*` instead of `READ_EXTERNAL_STORAGE`; nearby-device BT perms | KotlinTouchMove (already 33). Examples storage code still written for the older model. |
| **34** | Typed foreground services; partial photo access; stricter exact-alarm / export rules | MusicPlayerConstant already has `mediaPlayback` type. ble-starter if you add FGS. |
| **35** | Edge-to-edge required on Android 15+; 16 KB page size (Play, native `.so`) | IdolGame, ColorCatch, KotlinTouchMove, ble-starter. TouchMoveCompose already calls `enableEdgeToEdge()`. |
| **36** | No edge-to-edge opt-out; large-screen orientation / resizability opt-outs dropped | IdolGame portrait lock. ble-starter portrait lock. |
| **37** | `ACCESS_LOCAL_NETWORK`; lock-free `MessageQueue`; `static final` sealed; RFCOMM `read` −1; ECH; SMS OTP on standard SMS | All remaining &lt; 37. Network apps (FinnStock / AnimeDB / Articles / Examples) if they probe LAN or use cleartext. |

---

## Note: platform-samples compile 37 ≠ target 37

Almost every Google sample in `bluetooth/platform-samples` compiles against API 37 so new symbols resolve, but `defaultConfig.targetSdk` is still **35** (quicksettings **36**, live-updates and the host app **37**).

If you copy a sample into one of your target-37 apps, you pick up the APIs **and** your stricter behavior set. Test orientation, insets, and permissions on an API 37 emulator, not only the sample’s original target.

---

## Official references

- [Set up the Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk)
- [Behavior changes: apps targeting Android 17+](https://developer.android.com/about/versions/17/behavior-changes-17)
