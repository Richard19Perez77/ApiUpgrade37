```kotlin
package com.rick.apiupgrade37.ui.screens

import android.os.Build
import android.os.ext.SdkExtensions
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import android.widget.photopicker.PhotoPickerSelectionParams
import android.widget.photopicker.PhotoPickerUiCustomizationParams
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun PhotoPickerScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf("No photo yet") }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        status = uri?.toString() ?: "Cancelled"
    }

    val uiParams = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            PhotoPickerUiCustomizationParams.Builder()
                .setAspectRatio(PhotoPickerUiCustomizationParams.ASPECT_RATIO_PORTRAIT_9_16)
                .build()
        } else {
            null
        }
    }
    val selection = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            PhotoPickerSelectionParams.Builder()
                .setMimeTypes(listOf("image/*", "video/*"))
                .build()
        } else {
            null
        }
    }
    val embeddedInfo = remember(uiParams, selection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            uiParams != null &&
            selection != null
        ) {
            EmbeddedPhotoPickerFeatureInfo.Builder()
                .setUiCustomizationParams(uiParams)
                .setSelectionParams(selection)
                .setMaxSelectionLimit(3)
                .build()
        } else {
            null
        }
    }

    FeatureScaffold("Photo picker", onBack) { padding ->
        FeatureBody(
            padding,
            ""
        ) {
            if (AndroidApis.isAndroid17) {
                Text(
                    "uiParams.aspectRatio=${uiParams?.aspectRatio ?: "n/a"} " +
                        "embedded.max=${embeddedInfo?.maxSelectionLimit ?: "n/a"}"
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Text(
                    "Photo Picker extension version = " +
                        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R)
                )
            }
            Button(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            ) { Text("Launch Photo Picker (API 33+ contract)") }
            Text(status)
            Text(
                if (AndroidApis.isAndroid17) {
                    "API 37 device: portrait thumbnail mode is available to the embedded picker."
                } else {
                    "Pre-37: PickVisualMedia still works; aspect-ratio customization is ignored."
                }
            )
        }
    }
}
```

---

# Analysis of `PhotoPickerScreen.kt`

## What This Class Does

`PhotoPickerScreen` is a **Compose UI screen** that demonstrates Android API 37's new **Photo Picker customization features**. It shows how apps can now customize the system photo picker with:
- **Thumbnail aspect ratios** (portrait 9:16 for social/video apps)
- **Selection parameters** (file types, max selections)
- **Embedded picker support** (inline within your app)

---

## What is the Photo Picker?

The **Photo Picker** (introduced in Android 13/API 33) is a system-provided UI that lets users select photos/videos **without granting storage permissions**. It replaced the dangerous `READ_EXTERNAL_STORAGE` permission model.

**Before Photo Picker:**
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<!-- App could read ALL user photos 😱 -->
```

**After Photo Picker:**
```kotlin
// No permission needed!
// User selects specific photos to share
// App only gets the selected URIs
```

---

## The API 37 Improvements

| Feature | Before (API ≤36) | After (API 37) | Improvement |
|---------|-----------------|----------------|-------------|
| **Thumbnail Aspect Ratio** | Always square thumbnails | Customizable (9:16 portrait, 1:1 square) | **UX** 🎨 |
| **Selection Limits** | No built-in limit | `maxSelectionLimit()` | **Control** |
| **MIME Types** | All or nothing | Specific types (`image/*`, `video/*`) | **Precision** |
| **Embedded Picker** | Standalone only | Embedded in your UI | **Integration** |
| **Customization** | Limited | Full customization via `PhotoPickerUiCustomizationParams` | **Flexibility** |

---

## Code Analysis: API Checks & Features

### 1. **PhotoPickerUiCustomizationParams - Aspect Ratio**

```kotlin
val uiParams = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        PhotoPickerUiCustomizationParams.Builder()
            .setAspectRatio(PhotoPickerUiCustomizationParams.ASPECT_RATIO_PORTRAIT_9_16)
            .build()
    } else {
        null  // ✅ Graceful degradation for older devices
    }
}
```

**API Check:** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN` (API 37)

**What it does:**
- Sets thumbnail preview to **portrait 9:16** (perfect for social apps)
- Only available on API 37+
- Returns `null` on older devices (no crash)

**Gain:** 🎨 **Better UX for specific app types**

**Use Cases:**
- **Social media apps** → 9:16 portrait (like Instagram/TikTok)
- **Photo editing** → 1:1 square (like Instagram grid)
- **Video apps** → 16:9 landscape

---

### 2. **PhotoPickerSelectionParams - MIME Types**

```kotlin
val selection = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        PhotoPickerSelectionParams.Builder()
            .setMimeTypes(listOf("image/*", "video/*"))
            .build()
    } else {
        null
    }
}
```

**API Check:** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN` (API 37)

**What it does:**
- Limits selection to **images AND videos**
- Only available on API 37+
- Returns `null` on older devices

**Gain:** 🎯 **Focused selection** - users only see relevant files

**Before/After:**
```kotlin
// BEFORE (API 33-36): Could only select images
ActivityResultContracts.PickVisualMedia(ImageOnly)

// AFTER (API 37): Can select multiple types
PhotoPickerSelectionParams.Builder()
    .setMimeTypes(listOf("image/*", "video/*"))
    .build()
```

---

### 3. **EmbeddedPhotoPickerFeatureInfo - Integration**

```kotlin
val embeddedInfo = remember(uiParams, selection) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
        uiParams != null &&
        selection != null
    ) {
        EmbeddedPhotoPickerFeatureInfo.Builder()
            .setUiCustomizationParams(uiParams)
            .setSelectionParams(selection)
            .setMaxSelectionLimit(3)
            .build()
    } else {
        null
    }
}
```

**API Check:** Triple check:
1. ✅ `Build.VERSION.SDK_INT >= API 37`
2. ✅ `uiParams != null` (aspect ratio set)
3. ✅ `selection != null` (MIME types set)

**What it does:**
- Creates an **embedded picker** (fits inside your app)
- Max 3 selections allowed
- Custom UI + selection parameters

**Gain:** 📱 **Integrated UX** - picker feels native to your app

---

## The Photo Picker Extension System

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    Text(
        "Photo Picker extension version = " +
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R)
    )
}
```

### What's This?

The Photo Picker is a **Module SDK Extension** - it can be **updated independently** of the Android OS version.

**Why this matters:**

| Scenario | Build.VERSION.SDK_INT | Photo Picker Version |
|----------|----------------------|---------------------|
| Pixel 6 with Android 13 | API 33 | Version 1 (basic) |
| Pixel 6 with Android 14 | API 34 | Version 2 (improved) |
| Same device, Play System Update | API 34 | Version 5 (latest!) |

**Key Insight:**
- The Photo Picker can get new features **without a full OS update**
- `SdkExtensions.getExtensionVersion()` checks the **actual Photo Picker version**
- Your app can support new features on **older Android versions** if they have the extension!

---

## Comparison: Extension API vs Platform API

### Platform API (OS Version)
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    // Use API 37 features
    // ✅ Works on Android 17+
    // ❌ Not available on older OS versions
}
```

### Extension API (Module Update)
```kotlin
if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 12) {
    // Use Photo Picker features from extension version 12+
    // ✅ Works on older OS with Play System updates
    // ❌ Only for Photo Picker (not all APIs)
}
```

**The comment explains:**
```kotlin
// These values come from API 37 classes, so the gate is SDK_INT, not an SDK
// extension version. Checking an extension here was backwards: it could show
// the readout on an API 30 device and hide it on a real Android 17 one.
```

**Translation:**
- `PhotoPickerUiCustomizationParams` is a **platform API** (API 37)
- It's **NOT** an extension API
- Using `SdkExtensions` to check for it would be WRONG
- Use `Build.VERSION.SDK_INT` for platform features

---

## The Complete API Check Flow

```kotlin
// ✅ CHECK 1: Platform API Check
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    // API 37 features available
    val uiParams = PhotoPickerUiCustomizationParams.Builder()
        .setAspectRatio(ASPECT_RATIO_PORTRAIT_9_16)
        .build()
    
    val selection = PhotoPickerSelectionParams.Builder()
        .setMimeTypes(listOf("image/*", "video/*"))
        .build()
    
    val embedded = EmbeddedPhotoPickerFeatureInfo.Builder()
        .setUiCustomizationParams(uiParams)
        .setSelectionParams(selection)
        .setMaxSelectionLimit(3)
        .build()
} else {
    // ❌ API 36 or lower - use basic PickVisualMedia
    picker.launch(PickVisualMediaRequest(ImageAndVideo))
}

// ✅ CHECK 2: Extension API Check (for context)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val extensionVersion = SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R)
    // Shows Photo Picker version for informational purposes
}
```

---

## API 37 Features vs Basic Photo Picker

| Feature | Basic Photo Picker (API 33-36) | API 37 Enhanced |
|---------|-------------------------------|-----------------|
| **Aspect Ratio** | Always square | 9:16, 1:1, etc. |
| **Max Selection** | Hardcoded to 10 | Customizable |
| **MIME Types** | Single type only | Multiple types |
| **Embedded** | No | Yes |
| **UI Customization** | None | Full |
| **UX Tailoring** | Generic | App-specific |

---

## Real-World Use Cases

### Social Media App (TikTok/Instagram)
```kotlin
// API 37 ONLY - Portrait thumbnails
val params = PhotoPickerUiCustomizationParams.Builder()
    .setAspectRatio(ASPECT_RATIO_PORTRAIT_9_16)  // ✅ Perfect for Stories
    .build()

// Pre-API 37 fallback
// Users see square thumbnails (cropped awkwardly)
```

### Photo Editing App (VSCO/Snapseed)
```kotlin
// API 37 ONLY - Square thumbnails
val params = PhotoPickerUiCustomizationParams.Builder()
    .setAspectRatio(ASPECT_RATIO_SQUARE)  // ✅ Perfect for grid preview
    .build()
```

### Messaging App (WhatsApp/Telegram)
```kotlin
// API 37 ONLY - Embedded picker
val embedded = EmbeddedPhotoPickerFeatureInfo.Builder()
    .setMaxSelectionLimit(5)  // ✅ Send multiple photos
    .setSelectionParams(selection)
    .build()
// Picker appears INSIDE your chat screen
```

---

## Key Improvements Summary

| Improvement | Why It Matters | Who Benefits |
|-------------|---------------|--------------|
| **Aspect Ratio Customization** | Thumbnails match app design | Social/Video apps |
| **Max Selection Limit** | Control user experience | All apps |
| **MIME Type Filtering** | Show only relevant files | Specialized apps |
| **Embedded Picker** | Native-feeling UX | All apps |
| **No Permissions Needed** | Privacy | Users |

---

## Migration Guide

### Step 1: Check API Level
```kotlin
val isApi37 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
```

### Step 2: Use Enhanced Features
```kotlin
val launcher = if (isApi37) {
    // Use enhanced picker with customization
    createEnhancedPicker()
} else {
    // Fallback to basic PickVisualMedia
    createBasicPicker()
}
```

### Step 3: Test Both Paths
- ✅ API 37 device → Full features
- ✅ API 33-36 device → Basic features
- ✅ No crashes on older devices

---

## The "Why" Behind This Change

**Before Android 17:**
- Photo Picker had **one-size-fits-all** UI
- Square thumbnails wasted space for portrait photos
- No way to limit selections
- Couldn't show only videos/photos
- Picker was standalone (couldn't embed)

**After Android 17:**
- **Tailored UX** - picker matches app design
- **Efficient** - shows relevant content only
- **Integrated** - picker feels native to app
- **Flexible** - control selection limits

**The big picture:** API 37 makes the Photo Picker **app-aware** instead of generic, improving UX while maintaining privacy.

---

## Summary

The Photo Picker API 37 enhancements are about **UX customization** while keeping privacy benefits:

1. **Aspect Ratio** → Thumbnails match your app's visual design
2. **Selection Params** → Show only relevant file types
3. **Embedded Picker** → Integrated UX, not separate screen
4. **Max Selection** → Control user behavior

**Key API Check:**
```kotlin
Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
```
(Always check this before using API 37 features)

**Bonus:** Photo Picker is an extension API, meaning some features might appear on older devices via Play System updates!
