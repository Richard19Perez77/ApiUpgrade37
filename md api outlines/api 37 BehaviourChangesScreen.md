```kotlin
package com.rick.apiupgrade37.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun BehaviorChangesScreen(
    onBack: (() -> Unit)?,
    listPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val npu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_NEURAL_PROCESSING_UNIT)
    } else {
        // Pre-37: NNAPI via PackageManager.FEATURE_OPENGLES_EXTENSION_PACK / vendor extras.
        false
    }
    val body = @Composable { padding: PaddingValues ->
        FeatureBody(
            padding,
            "These fire only when targetSdk is 37+, even if the device already runs Android 17."
        ) {
            Text("Lock-free MessageQueue", style = MaterialTheme.typography.titleMedium)
            Text(
                "android.os.MessageQueue is lock-free when you target 37. Faster, but any app " +
                    "that reflected on private MessageQueue fields breaks. Tests should use " +
                    "TestLooperManager.peekWhen() / poll() instead of peeking mMessages."
            )
            Text("static final is actually final", style = MaterialTheme.typography.titleMedium)
            Text(
                "Reflection or JNI SetStatic*Field on a static final now throws " +
                    "IllegalAccessException / crashes. Do not patch Build.VERSION.SDK_INT in tests; " +
                    "use Robolectric shadows or a wrapper."
            )
            // BAD (API 37+):
            // Build.VERSION::class.java.getField("SDK_INT").apply { isAccessible = true }.setInt(null, 24)
            Text("Safer native DCL", style = MaterialTheme.typography.titleMedium)
            Text(
                "API 34 made dynamically loaded DEX/JAR read-only. Targeting 37 extends that to " +
                    "native .so files: System.load(path) throws UnsatisfiedLinkError unless the " +
                    "file is marked read-only (and preferably extracted to an immutable dir). " +
                    "Prefer System.loadLibrary() from jniLibs."
            )
            Text("SMS OTP delay", style = MaterialTheme.typography.titleMedium)
            Text(
                "Standard SMS OTPs are delayed 3 hours for apps targeting 37 that are not the " +
                    "default SMS / assistant / companion. Use SMS Retriever or User Consent APIs. " +
                    "WebOTP already delayed non-matching domains."
            )
            Text("Background audio", style = MaterialTheme.typography.titleMedium)
            Text(
                "Playback, audio focus, and volume APIs from the background are hardened. " +
                    "Alarms stay exempt. Use a while-in-use FGS of the correct type."
            )
            Text("NPU declaration", style = MaterialTheme.typography.titleMedium)
            Text(
                "FEATURE_NEURAL_PROCESSING_UNIT declared in the manifest (required=false). " +
                    "Device reports feature=$npu. Targeting 37 without the declaration can block " +
                    "LiteRT NPU / NNAPI / vendor NPU SDKs."
            )
            Text("Physical-keyboard secrets", style = MaterialTheme.typography.titleMedium)
            Text(
                "Password fields no longer flash the last typed character when a hardware " +
                    "keyboard is attached. Compose SecureTextField follows this in 1.12+."
            )
            Text("PQC", style = MaterialTheme.typography.titleMedium)
            Text(
                "Keystore can mint ML-DSA keys (JCA). APK Signature Scheme v3.2 is hybrid " +
                    "classical + ML-DSA. Play App Signing will offer rotation; self-managed keys " +
                    "need a NEW classical key paired with ML-DSA (cannot reuse the old one). " +
                    "apksigner in current build-tools performs the hybrid sign."
            )
            Text("deviceSdk=${Build.VERSION.SDK_INT} isAndroid17=${AndroidApis.isAndroid17}")
        }
    }
    // This screen is reachable two ways: as a top-level tab (onBack == null), where the tab bar owns the bottom inset, and as a catalog entry, where FeatureScaffold supplies its own bars. Hence, the two branches.
    if (onBack == null) {
        Scaffold { padding ->
            // Carry the horizontal insets through. In landscape, they hold the display cutout and the side navigation bar, and dropping them puts text under both.
            val direction = LocalLayoutDirection.current
            body(
                PaddingValues(
                    start = padding.calculateStartPadding(direction),
                    end = padding.calculateEndPadding(direction),
                    top = padding.calculateTopPadding(),
                    bottom = listPadding.calculateBottomPadding()
                )
            )
        }
    } else {
        FeatureScaffold("Target-37 behaviors", onBack, body)
    }
}

```

---

# Analysis of `BehaviorChangesScreen.kt`

## What This Class Does

`BehaviorChangesScreen` is a **Compose UI screen** that provides a **comprehensive overview** of all behavioral changes introduced in Android API 37 (Android 17). It serves as an educational/informational dashboard that lists each breaking change with explanations, while also demonstrating how to query the NPU feature availability.

---

## The Big Picture: API 37 Behavioral Changes

This screen documents **8 major behavioral changes** that occur when an app targets API 37, **even if the device is already running Android 17** (API 37). This is the "readiness checklist" for upgrading your app's `targetSdkVersion` to 37.

---

## The 8 Behavioral Changes

### 1. **Lock-free MessageQueue**
- **What Changed:** `android.os.MessageQueue` is now lock-free when targeting API 37
- **Why It Matters:** Faster message processing, but reflection on private fields breaks
- **Impact:** 🚀 Performance gain (automatic) + ⚠️ Testing breakage (if using reflection)
- **Action:** Use `TestLooperManager.peekWhen()`/`poll()` instead of reflecting on `mMessages`

### 2. **static final is Actually Final**
- **What Changed:** Reflection or JNI `SetStatic*Field` on `static final` throws/crashes
- **Why It Matters:** Security improvement - constants are truly immutable
- **Impact:** 🔒 Security gain + ⚠️ Tests that patch `Build.VERSION.SDK_INT` break
- **Action:** Use Robolectric shadows or dependency injection wrappers

### 3. **Safer Native DCL (System.load)**
- **What Changed:** `System.load(path)` on writable `.so` files throws `UnsatisfiedLinkError`
- **Why It Matters:** Prevents tampering with loaded native libraries
- **Impact:** 🔒 Security gain + ⚠️ Apps that extract `.so` files break
- **Action:** Use `System.loadLibrary()` from `jniLibs` or mark files read-only

### 4. **SMS OTP Delay**
- **What Changed:** Standard SMS OTPs delayed 3 hours (unless default SMS/assistant/companion)
- **Why It Matters:** Privacy protection - prevents background SMS reading
- **Impact:** 🔒 Privacy gain + ⚠️ OTP reading breaks
- **Action:** Use SMS Retriever API or User Consent API

### 5. **Background Audio Hardening**
- **What Changed:** Background playback, audio focus, and volume APIs are hardened
- **Why It Matters:** Prevents unwanted background audio, improves battery life
- **Impact:** 🔋 Battery gain + ⚠️ Background audio breaks
- **Action:** Use a correctly typed foreground service (FGS)

### 6. **NPU Declaration Requirement**
- **What Changed:** Apps must declare `FEATURE_NEURAL_PROCESSING_UNIT` in manifest
- **Why It Matters:** Transparency - users know when apps use neural hardware
- **Impact:** 🎯 Transparency gain + ⚠️ NPU access blocked without declaration
- **Action:** Add `<uses-feature>` to manifest

### 7. **Physical-Keyboard Secrets**
- **What Changed:** Password fields no longer flash the last typed character
- **Why It Matters:** Privacy - prevents shoulder-surfing on physical keyboards
- **Impact:** 🔒 Security gain (automatic, no action needed)
- **Action:** None - automatic improvement (Compose 1.12+ handles it)

### 8. **PQC (Post-Quantum Cryptography)**
- **What Changed:** Keystore can mint ML-DSA keys; APK Signature Scheme v3.2 is hybrid
- **Why It Matters:** Future-proofing against quantum computers
- **Impact:** 🔮 Future-proof gain + ⚠️ Self-managed keys need rotation
- **Action:** Generate new ML-DSA keys (can't reuse old classical keys)

---

## Code Analysis

### 1. **NPU Feature Detection**

```kotlin
val npu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_NEURAL_PROCESSING_UNIT)
} else {
    // Pre-37: NNAPI via PackageManager.FEATURE_OPENGLES_EXTENSION_PACK / vendor extras.
    false
}
```

**What it does:**
- Checks if device is running API 37+
- Queries whether the device has NPU hardware
- Returns `false` on older devices

**API Check:** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN` (API 37)

**Why This Matters:**
```xml
<!-- In manifest: -->
<uses-feature
    android:name="android.hardware.neural_processing_unit"
    android:required="false" />
```

```kotlin
// In code: Check if NPU is actually available
if (npu) {
    // Use LiteRT NPU delegate or NNAPI
    interpreter.useNnApiForInference()
} else {
    // Fallback to CPU
    interpreter.useCpuForInference()
}
```

---

### 2. **The Informational UI**

```kotlin
Text("Lock-free MessageQueue", style = MaterialTheme.typography.titleMedium)
Text(
    "android.os.MessageQueue is lock-free when you target 37. Faster, but any app " +
    "that reflected on private MessageQueue fields breaks. Tests should use " +
    "TestLooperManager.peekWhen() / poll() instead of peeking mMessages."
)
```

**What it shows:**
1. **Change Name** (title)
2. **Detailed Explanation** (body)
3. **Impact** (what breaks/improves)
4. **Action Required** (what to do)

---

### 3. **The Dual Layout Pattern**

```kotlin
if (onBack == null) {
    Scaffold { padding -> body(...) }  // Top-level tab
} else {
    FeatureScaffold("Target-37 behaviors", onBack, body)  // Catalog entry
}
```

**Why This Pattern:**
- **Top-level tab**: No back button, uses Scaffold for structure
- **Catalog entry**: Has back button, uses FeatureScaffold

**The result:** The same screen can be used in two different contexts!

---

## The "Fires Only When targetSdk is 37+" Concept

### Important Distinction:

| Scenario | Behavioral Changes Apply? |
|----------|---------------------------|
| App targets API 36, runs on Android 17 device | ❌ NO - Old behavior |
| App targets API 37, runs on Android 17 device | ✅ YES - New behavior |
| App targets API 37, runs on Android 16 device | ✅ YES (if device supports) |

**Why This Matters:**
```kotlin
// apps build.gradle
android {
    targetSdk = 37  // Setting this triggers ALL the changes!
}
```

**The key takeaway:** These changes don't depend on the device's Android version - they depend on your app's **targetSdkVersion**.

---

## Complete Behavioral Changes Checklist

### 🚀 Performance (Automatic Gains)
| Change | Benefit | Action |
|--------|---------|--------|
| Lock-free MessageQueue | Faster message processing | None (automatic) |

### 🔒 Security (Automatic Gains)
| Change | Benefit | Action |
|--------|---------|--------|
| Physical-keyboard secrets | Better password privacy | None (automatic) |
| PQC Signing | Quantum-resistant | Rotate keys if self-managed |

### ⚠️ Breaking Changes (Require Action)
| Change | Impact | Required Action |
|--------|--------|-----------------|
| static final protection | Reflection on Build.SDK_INT breaks | Use Robolectric/DI |
| Native DCL | System.load() on writable .so breaks | Use System.loadLibrary() |
| SMS OTP | OTP reading delayed 3 hours | Use SMS Retriever |
| Background audio | Background playback breaks | Use foreground service |
| NPU declaration | NPU access blocked | Add manifest declaration |

---

## The NPU Feature Check Explained

### Why Query NPU Feature:
```kotlin
val npu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_NEURAL_PROCESSING_UNIT)
} else {
    false
}
```

### When to Use This:
```kotlin
// In your ML/AI code
fun setupInference() {
    val interpreter = Interpreter(model)
    
    // Check if NPU is available before using it
    if (npuAvailable()) {
        // Use NPU for faster inference
        val delegate = NnApiDelegate()
        interpreter.setDelegate(delegate)
    } else {
        // Fallback to CPU
        interpreter.useCpuForInference()
    }
}
```

---

## Before vs After: SDK Targeting

### ❌ BAD (TARGET SDK 36):
```gradle
android {
    targetSdk = 36  // Old behavior
}
```
- MessageQueue still synchronized (slower)
- Can reflect on static finals (insecure)
- System.load() works on writable files (insecure)
- SMS OTPs accessible immediately (privacy issue)
- Background audio works without FGS (battery issue)
- NPU works without manifest declaration (untransparent)

### ✅ GOOD (TARGET SDK 37):
```gradle
android {
    targetSdk = 37  // New behavior
}
```
- MessageQueue lock-free (faster) 🚀
- static finals immutable (secure) 🔒
- System.load() requires read-only files (secure) 🔒
- SMS OTPs require consent (privacy) 🛡️
- Background audio requires FGS (battery) 🔋
- NPU requires manifest declaration (transparent) 🎯

---

## Key Improvements Summary

| Category | Number of Changes | Action Required |
|----------|------------------|-----------------|
| **Performance** | 1 | Optional (test fix) |
| **Security** | 3 | 2 require action |
| **Privacy** | 2 | Both require action |
| **Transparency** | 1 | Requires action |
| **Future-Proof** | 1 | Optional (key rotation) |

---

## The "Why" Behind These Changes

**Before Android 17 (targetSdk < 37):**
- Apps could **reflect** on internal fields (insecure)
- Native libraries could be **tampered** with (insecure)
- Apps could **read SMS** without consent (privacy issue)
- Background audio **drained battery** (battery issue)
- NPU usage was **hidden** (transparency issue)
- Quantum computers could **break** signatures (future issue)

**After Android 17 (targetSdk = 37):**
- Internal fields are **truly private** (secure)
- Native libraries are **protected** (secure)
- SMS OTPs require **user consent** (privacy)
- Background audio requires **user awareness** (battery)
- NPU usage is **transparent** (user choice)
- Signatures are **quantum-resistant** (future-proof)

---

## Summary

`BehaviorChangesScreen` serves as a **comprehensive checklist** for API 37 migration:

1. **Lock-free MessageQueue** → Faster, but tests break
2. **static final protection** → Secure, but reflection fails
3. **Native DCL protection** → Secure, but System.load() breaks
4. **SMS OTP delay** → Private, but need SMS Retriever
5. **Background audio** → Battery-friendly, but need FGS
6. **NPU declaration** → Transparent, but need manifest
7. **Physical-keyboard** → Secure (automatic)
8. **PQC Signing** → Future-proof (need rotation)

**The big picture:** This is the "upgrade tax" for targeting API 37. Most changes are:
- **Automatic security improvements** (little to no code changes)
- **Breaking changes** that require specific fixes
- **Privacy enhancements** that protect users

**Developer Action Items:**
- Review each change with your team
- Identify which changes affect your app
- Implement required fixes
- Update targetSdk to 37
- Test thoroughly on API 37 devices

**The result:** A more secure, private, and performant app! 🎉