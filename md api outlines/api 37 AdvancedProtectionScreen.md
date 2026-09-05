```kotlin
package com.rick.apiupgrade37.ui.screens

import android.content.Context
import android.os.Build
import android.security.advancedprotection.AdvancedProtectionManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun AdvancedProtectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(readEnabled(context)) }

    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            // Pre-36: no platform switch to observe. Apps inferred risk from DevicePolicyManager or their own settings.
            return@DisposableEffect onDispose { }
        }
        val mgr = context.getSystemService(AdvancedProtectionManager::class.java)
            ?: return@DisposableEffect onDispose { }
        val cb = AdvancedProtectionManager.Callback { value -> enabled = value }
        mgr.registerAdvancedProtectionCallback(ContextCompat.getMainExecutor(context), cb)
        onDispose { mgr.unregisterAdvancedProtectionCallback(cb) }
    }

    FeatureScaffold("Advanced Protection", onBack) { padding ->
        FeatureBody(
            padding,
            ""
        ) {
            Text("AAPM enabled = $enabled")
        }
    }
}

private fun readEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
    return context.getSystemService(AdvancedProtectionManager::class.java)
        ?.isAdvancedProtectionEnabled == true
}

```

---

# Analysis of `AdvancedProtectionScreen.kt`

## What This Class Does

`AdvancedProtectionScreen` is a **Compose UI screen** that demonstrates how apps can query and monitor **Android Advanced Protection Mode (AAPM)** status. This is a **security hardening feature** that users can enable for enhanced protection.

---

## What is Android Advanced Protection Mode (AAPM)?

**AAPM** is a user-opt-in security profile that provides **enterprise-grade protection** for high-risk users:

- 🛡️ **Block sideloading** → Prevents installing apps from unknown sources
- 🔌 **Restrict USB data** → Prevents data transfer via USB
- 🛡️ **Force Play Protect** → Requires Google Play Protect scanning
- 🔒 **Enhanced app verification** → Stricter security checks
- 🛡️ **Phishing protection** → Better warning for suspicious sites

**Who It's For:**
- 📱 **Journalists** → Protection against spyware
- 🏛️ **Government officials** → State-level threats
- 💼 **Business executives** → Corporate espionage
- 🔐 **High-risk individuals** → Targeted attacks

---

## The API 36 Feature (Not API 37!)

### Key Insight:
```kotlin
/**
 * Advanced Protection Mode is one of the few entries in this lab that is NOT an API 37
 * feature: AdvancedProtectionManager shipped in Android 16 (API 36, BAKLAVA). Gating it
 * behind isAndroid17 would report "off" on an Android 16 device where the user has
 * actually turned it on, which is the wrong way to fail for a security signal.
 */
```

**Important:** This feature is from **API 36 (Android 16/Baklava)** , not API 37!

---

## Why This Matters for API 37 Upgrade

### The Mistake Some Developers Make:
```kotlin
// ❌ WRONG: Assuming AAPM is an API 37 feature
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    // Query AAPM
} else {
    // Report "unavailable"
}
// This shows "off" on Android 16 devices where AAPM is ON! 😱
```

### The Correct Approach:
```kotlin
// ✅ CORRECT: Check API 36 (BAKLAVA)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
    // Query AAPM
} else {
    // Pre-API 36: Feature not available
}
```

---

## Code Analysis

### 1. **Reading Initial State**

```kotlin
var enabled by remember { mutableStateOf(readEnabled(context)) }

private fun readEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
    return context.getSystemService(AdvancedProtectionManager::class.java)
        ?.isAdvancedProtectionEnabled == true
}
```

**What it does:**
- Checks API level (BAKLAVA = API 36)
- Gets `AdvancedProtectionManager` service
- Reads current AAPM status
- Returns `false` if unavailable

**API Check:** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA` (API 36)

---

### 2. **Monitoring State Changes**

```kotlin
DisposableEffect(Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
        // Pre-36: no platform switch to observe
        return@DisposableEffect onDispose { }
    }
    
    val mgr = context.getSystemService(AdvancedProtectionManager::class.java)
        ?: return@DisposableEffect onDispose { }
    
    val cb = AdvancedProtectionManager.Callback { value -> 
        enabled = value  // Update state when AAPM changes
    }
    
    mgr.registerAdvancedProtectionCallback(
        ContextCompat.getMainExecutor(context), 
        cb
    )
    
    onDispose { 
        mgr.unregisterAdvancedProtectionCallback(cb) 
    }
}
```

**What it does:**
1. **Registers a callback** to listen for changes
2. **Updates state** when AAPM is toggled
3. **Unregisters** when screen is disposed
4. **Runs on main thread** (UI-safe)

**Callback Flow:**
```
User turns on AAPM in Settings
         ↓
AdvancedProtectionManager Callback triggered
         ↓
enabled = true (state updated)
         ↓
UI recomposes → Shows "AAPM enabled = true" ✅
```

---

## What Apps Should Do When AAPM is Enabled

### 1. **Hide High-Risk Flows**

```kotlin
// Custom APK installer
if (advancedProtectionEnabled) {
    // Hide the custom installer UI
    // AAPM prevents sideloading anyway
    installApkButton.isVisible = false
    showMessage("APK installation is disabled in Advanced Protection Mode")
}
```

### 2. **Disable USB File Transfer**

```kotlin
// USB file transfer options
if (advancedProtectionEnabled) {
    // AAPM restricts USB data
    usbTransferOption.isEnabled = false
    showMessage("USB file transfer is restricted in Advanced Protection Mode")
}
```

### 3. **Hide Debug Overlays**

```kotlin
// Developer/debug features
if (advancedProtectionEnabled) {
    // AAPM blocks debug overlays
    debugOverlay.isVisible = false
    developerTools.isVisible = false
}
```

### 4. **Show Security Status**

```kotlin
// Security indicator
if (advancedProtectionEnabled) {
    statusBadge = "🛡️ Advanced Protection: ON"
    statusBadgeColor = Color.Green
} else {
    statusBadge = "⚠️ Advanced Protection: OFF"
    statusBadgeColor = Color.Red
}
```

---

## Real-World Use Cases

### 1. **Banking App**
```kotlin
class BankingApp {
    fun showSecurityStatus() {
        val isAdvancedProtection = advancedProtectionManager.isAdvancedProtectionEnabled
        
        if (isAdvancedProtection) {
            showSecurityBadge("✅ Extra protection active")
            allowLargeTransfers = true  // More trust
        } else {
            showSecurityBadge("⚠️ Recommended: Enable Advanced Protection")
            showLargeTransferWarning()
        }
    }
}
```

### 2. **Secure Messaging App**
```kotlin
class SecureMessenger {
    fun checkSecurity() {
        if (advancedProtectionEnabled) {
            // Hide screenshot/screen recording warning
            // User is already protected
            showEncryptionBadge("🛡️ Protected by Advanced Protection")
        } else {
            // Show recommendation
            showRecommendation("Enable Advanced Protection for extra security")
        }
    }
}
```

### 3. **Enterprise App**
```kotlin
class EnterpriseApp {
    fun enforceSecurity() {
        if (!advancedProtectionEnabled) {
            // Block sensitive operations
            showError("Advanced Protection required for this feature")
            lockApp()
        }
    }
}
```

---

## Permission Requirement

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.QUERY_ADVANCED_PROTECTION_MODE" />
```

**Why This Permission:**
- Required to query AAPM status
- Normal permission (no user prompt)
- Protects user privacy

---

## Before vs After: Security Awareness

### ❌ BEFORE (No AAPM API):
```kotlin
// Apps couldn't detect AAPM status
// Had to infer from other signals:
// - DevicePolicyManager flags
// - Custom security checks
// - Inconsistent results
// - User confusion
```

### ✅ AFTER (API 36+):
```kotlin
// Single, reliable API
val isEnabled = advancedProtectionManager.isAdvancedProtectionEnabled

// Consistent across devices
// Official platform signal
// Easy to integrate
// Clear security status
```

---

## The "Why" Behind This Feature

**Before API 36:**
- No unified way to detect enhanced security
- Apps had to guess or use multiple signals
- Inconsistent behavior across devices
- High-risk users lacked app support

**After API 36:**
- **Single source of truth** for security status
- **Apps can adapt** to user's security needs
- **High-risk users** get better app support
- **Consistent** across all Android devices

---

## Key Takeaways for API 37 Upgrade

| Lesson | Explanation |
|--------|-------------|
| **Not All Features Are API 37** | AAPM is API 36, not API 37 |
| **Check Correct Version** | Use `BAKLAVA` (API 36), not `CINNAMON_BUN` (API 37) |
| **Don't Assume** | Features from older APIs still exist on newer devices |
| **Listen for Changes** | Use callbacks for real-time updates |
| **Hide High-Risk Flows** | Respect user's security choice |

---

## Summary

`AdvancedProtectionScreen` teaches an important lesson about **API version awareness**:

1. **AAPM is API 36** (Android 16/Baklava), not API 37
2. **Query with correct version** → `Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA`
3. **Don't gate behind API 37** → You'll incorrectly report "off" on Android 16 devices
4. **Monitor changes** → Use `registerAdvancedProtectionCallback()`
5. **Respect user choice** → Hide high-risk features when AAPM is enabled

**The big picture:** API 37 upgrade isn't just about new features; it's also about **correctly using existing APIs** from older versions. Many features introduced in API 36 (like AAPM) are still relevant and must be handled properly alongside API 37 features.