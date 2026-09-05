```kotlin
package com.rick.apiupgrade37.ui.screens

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CameraMediaScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val report by produceState(initialValue = "Querying cameras…", context) {
        value = withContext(Dispatchers.IO) { describeCameras(context) }
    }

    FeatureScaffold("Camera & media", onBack) { padding ->
        FeatureBody(
            padding,
            ""
        ) {
            Text(report)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                Text("RAW14 constant = ${ImageFormat.RAW14}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                Text("VVC mime = ${MediaFormat.MIMETYPE_VIDEO_VVC}")
            }
            Text(
                "CQ encode (API 37): MediaRecorder().setVideoEncodingQuality(/* quality */ 80)\n" +
                        "Pre-37: setVideoEncodingBitRate(bitrate) only. Quality overload: " +
                        if (AndroidApis.isAndroid17) "available" else "compile-only"
            )
            // Touch the VideoEncoder table so you can jump-to-declaration in Studio.
            Text("Encoders: H264=${MediaRecorder.VideoEncoder.H264} HEVC=${MediaRecorder.VideoEncoder.HEVC}")
        }
    }
}

private fun describeCameras(context: Context): String = try {
    describeCamerasOrThrow(context)
} catch (e: CameraAccessException) {
    "Camera service unavailable: ${e.reason}"
} catch (e: IllegalArgumentException) {
    "Camera query rejected: ${e.message}"
}

private fun describeCamerasOrThrow(context: Context): String {
    val cm = context.getSystemService(CameraManager::class.java)
        ?: return "No CameraManager on this device"
    return cm.cameraIdList.joinToString("\n") { id ->
        val chars = cm.getCameraCharacteristics(id)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            when (chars.get(CameraCharacteristics.INFO_DEVICE_TYPE)) {
                CameraMetadata.INFO_DEVICE_TYPE_BUILT_IN -> "BUILT_IN"
                CameraMetadata.INFO_DEVICE_TYPE_EXTERNAL -> "EXTERNAL (USB)"
                CameraMetadata.INFO_DEVICE_TYPE_VIRTUAL -> "VIRTUAL"
                else -> "UNKNOWN"
            }
        } else {
            // Pre-37: chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            "INFO_DEVICE_TYPE requires API 37"
        }
        val facing = chars.get(CameraCharacteristics.LENS_FACING)
        "camera $id facing=$facing type=$type"
    }.ifEmpty { "No cameras" }
}
```

---

# Analysis of `CameraMediaScreen.kt`

## What This Class Does

`CameraMediaScreen` is a **Compose UI screen** that demonstrates Android API 37's new **camera and media codec enhancements**. It shows new APIs for:
- **Camera device types** (built-in, USB, virtual)
- **RAW image formats** (14-bit Bayer)
- **Video codecs** (H.266/VVC)
- **Encoding quality** (constant-quality encoding)

---

## What Are the API 37 Improvements?

| Feature | Before (API ≤36) | After (API 37) | Improvement |
|---------|-----------------|----------------|-------------|
| **Camera Device Type** | Only hardware level | Built-in/USB/Virtual | **Clarity** 📷 |
| **RAW Format** | RAW10/RAW12 only | RAW14 (14-bit) | **Quality** 🎨 |
| **Video Codec** | HEVC/AV1 | VVC (H.266) | **Efficiency** 📹 |
| **Encoding Quality** | Bitrate only | Quality-based | **Simplicity** ⚡ |
| **HDR Metadata** | Limited | Eclipsa HDR | **Quality** 🌈 |

---

## 1. Camera Device Types (`INFO_DEVICE_TYPE`)

### The Problem Before API 37:
```kotlin
// Pre-API 37: Only had hardware level
val level = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
// Returns: LEGACY, LIMITED, FULL, or LEVEL_3
// 
// Problems:
// - Doesn't tell you if it's built-in or external
// - Can't distinguish USB webcams from internal cameras
// - No virtual camera support
```

### The API 37 Solution:
```kotlin
// API 37: New device type enum
val type = chars.get(CameraCharacteristics.INFO_DEVICE_TYPE)
when (type) {
    INFO_DEVICE_TYPE_BUILT_IN -> "Built-in camera"
    INFO_DEVICE_TYPE_EXTERNAL -> "USB webcam"
    INFO_DEVICE_TYPE_VIRTUAL -> "Virtual camera (software)"
    else -> "Unknown type"
}
```

**Why This Matters:**
- **Built-in**: Front/back cameras (standard)
- **External**: USB webcams (desktop mode)
- **Virtual**: Software cameras (screen sharing)

---

## 2. RAW14 Image Format (`ImageFormat.RAW14`)

### Evolution of RAW Formats:

| Format | Bit Depth | Quality | Use Case |
|--------|-----------|---------|----------|
| **RAW10** | 10-bit | Basic | Low-end devices |
| **RAW12** | 12-bit | Good | Mid-range devices |
| **RAW14** (API 37) | 14-bit | Excellent | Professional photography |

### Why RAW14 Matters:

**Before API 37:**
```kotlin
// Only RAW10/RAW12 available
val imageFormat = ImageFormat.RAW10
// 10-bit color depth = limited dynamic range
// Professional photographers need more
```

**After API 37:**
```kotlin
// RAW14 available
val imageFormat = ImageFormat.RAW14
// 14-bit color depth = 16,384 shades per channel!
// 4x more color information than 12-bit
// Better for editing and post-processing
```

**Real-World Benefit:**
```
Before (RAW10/12):        After (RAW14):
   Limited colors           Rich colors
   Less detail              More detail
   Banding artifacts        Smooth gradients
   Hard to edit             Easy to edit
```

---

## 3. VVC (H.266) Codec (`MediaFormat.MIMETYPE_VIDEO_VVC`)

### Evolution of Video Codecs:

| Codec | Bitrate Savings | Quality | API Level |
|-------|----------------|---------|-----------|
| **H.264/AVC** | Baseline | Good | API 16+ |
| **H.265/HEVC** | ~50% savings | Better | API 21+ |
| **AV1** | ~60% savings | Excellent | API 33+ |
| **H.266/VVC** (API 37) | ~50% vs HEVC (~75% vs H.264) | Excellent | API 37 |

### Why VVC Matters:

**Before API 37:**
```kotlin
// Only H.264, H.265, AV1
val mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC
// Good, but not optimal for 4K/8K video
// Large file sizes, slower streaming
```

**After API 37:**
```kotlin
// VVC (H.266) available
val mimeType = MediaFormat.MIMETYPE_VIDEO_VVC
// 50% smaller than HEVC!
// 4K/8K streaming becomes practical
// Better quality at same bitrate
```

**Real-World Impact:**
```
Before (HEVC):            After (VVC):
  100 MB 4K video          50 MB 4K video
  = Same quality!          = Half the size!
  
  Slower streaming         Faster streaming
  More storage used        Less storage used
  More bandwidth           Less bandwidth
```

---

## 4. Quality-Based Encoding (`setVideoEncodingQuality()`)

### The Problem Before API 37:

```kotlin
// Before: Had to set bitrate manually
val recorder = MediaRecorder()
recorder.setVideoEncodingBitRate(5_000_000) // 5 Mbps
// 
// Problems:
// - Have to guess the right bitrate
// - Different devices need different bitrates
// - Wrong bitrate = poor quality or huge files
// - Trial and error to get right
```

### The API 37 Solution:

```kotlin
// After: Set quality level (0-100)
val recorder = MediaRecorder()
recorder.setVideoEncodingQuality(80) // 80% quality
// 
// Benefits:
// - Device handles the bitrate automatically
// - Quality-based instead of bitrate-based
// - Easier to understand
// - Works optimally on each device
```

**Quality Scale:**
```kotlin
0-20:  Low quality (small files)
21-40: Medium-low (balanced)
41-60: Medium (default)
61-80: High quality (good)
81-100: Maximum quality (large files)
```

---

## CameraX Compatibility Note

```kotlin
// Critical note for CameraX users:
// CameraX 1.5.2 / 1.6.0+ required on Android 17 devices
// Without update, dynamic-range mode can crash!
```

### Why This Matters:
```kotlin
// Before (CameraX < 1.5.2):
val cameraProvider = ProcessCameraProvider.getInstance(context)
// Dynamic range mode + API 37 = CRASH! 💥

// After (CameraX >= 1.5.2):
val cameraProvider = ProcessCameraProvider.getInstance(context)
// Works perfectly on API 37 ✅
```

---

## Code Analysis

### 1. **ProduceState for Camera Enumeration**

```kotlin
val report by produceState(initialValue = "Querying cameras…", context) {
    value = withContext(Dispatchers.IO) { describeCameras(context) }
}
```

**What it does:**
- Enumerates cameras **off the UI thread**
- Camera service can be slow (Binder calls)
- Prevents UI freezing

**Why Dispatchers.IO:**
```kotlin
// CameraManager.cameraIdList() can be SLOW
// It talks to the camera service over Binder
// Could take 100-500ms on first access
// Must NOT be on main thread!
```

---

### 2. **Error Handling**

```kotlin
private fun describeCameras(context: Context): String = try {
    describeCamerasOrThrow(context)
} catch (e: CameraAccessException) {
    "Camera service unavailable: ${e.reason}"
} catch (e: IllegalArgumentException) {
    "Camera query rejected: ${e.message}"
}
```

**Why This Matters:**
- Emulators often have no camera
- Another app may hold the camera
- Camera service can be unavailable
- Graceful failure = no crashes

---

### 3. **Device Type Detection**

```kotlin
val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    when (chars.get(CameraCharacteristics.INFO_DEVICE_TYPE)) {
        CameraMetadata.INFO_DEVICE_TYPE_BUILT_IN -> "BUILT_IN"
        CameraMetadata.INFO_DEVICE_TYPE_EXTERNAL -> "EXTERNAL (USB)"
        CameraMetadata.INFO_DEVICE_TYPE_VIRTUAL -> "VIRTUAL"
        else -> "UNKNOWN"
    }
} else {
    "INFO_DEVICE_TYPE requires API 37"
}
```

**API Check:** `Build.VERSION.SDK_INT >= API 37`

**Fallback:** Clear message on older devices

---

## Use Cases for Each Feature

### 1. **USB Webcam Support (External Camera)**
```kotlin
// API 37: Apps can distinguish USB webcams
if (deviceType == INFO_DEVICE_TYPE_EXTERNAL) {
    // This is a USB webcam!
    // Show webcam-specific UI
    // Enable desktop-style video conferencing
}
```

### 2. **Professional Photography (RAW14)**
```kotlin
// API 37: Professional RAW capture
val imageFormat = ImageFormat.RAW14
// 14-bit color depth for pro editing
// Used by professional camera apps
```

### 3. **8K Video Recording (VVC)**
```kotlin
// API 37: VVC for 8K video
val mimeType = MediaFormat.MIMETYPE_VIDEO_VVC
// Record 8K video with manageable file sizes
// Streaming 8K becomes practical
```

### 4. **Constant Quality Encoding**
```kotlin
// API 37: Set quality, not bitrate
recorder.setVideoEncodingQuality(80)
// Easier for developers
// Better quality optimization
// Works across devices
```

---

## Evolution of Camera APIs

| API Level | Camera API | Key Features |
|-----------|-----------|--------------|
| **API 21** | Camera2 | Full manual control |
| **API 24** | RAW10/12 | Raw image capture |
| **API 33** | AV1 Codec | New video codec |
| **API 37** | RAW14, VVC, Device Type | Professional features |

---

## Real-World Impact

### Video Streaming Apps:
```
Before: HEVC/AV1 streaming
After: VVC streaming
→ 50% bandwidth savings
→ Better quality at same bitrate
→ More users can stream 4K
```

### Camera Apps:
```
Before: RAW10/12 capture
After: RAW14 capture
→ More color information
→ Better editing flexibility
→ Professional-grade photos
```

### Video Conferencing:
```
Before: No device type distinction
After: USB webcam detection
→ Better desktop experience
→ Webcam settings/controls
→ Multi-camera support
```

---

## Key Improvements Summary

| Feature | What It Does | Who Benefits |
|---------|-------------|--------------|
| **INFO_DEVICE_TYPE** | Identifies camera type (built-in/USB/virtual) | Video conferencing apps |
| **RAW14** | 14-bit raw image capture | Professional camera apps |
| **VVC Codec** | H.266 video encoding | Video streaming apps |
| **Quality Encoding** | Quality-based instead of bitrate | All video apps |
| **CameraX Update** | Compatibility fix | Camera app developers |

---

## The "Why" Behind This Change

**Before Android 17:**
- **Camera types** indistinguishable (no USB/webcam detection)
- **RAW format** limited to 10-12 bits
- **Video codec** stuck at HEVC/AV1
- **Encoding** required manual bitrate tuning
- **CameraX** had compatibility issues

**After Android 17:**
- **External cameras** (USB) are identifiable
- **RAW14** provides professional color depth
- **VVC** enables 50% better compression
- **Quality encoding** simplifies video recording
- **CameraX** has official support

---

## Summary

API 37's camera and media enhancements represent **professional-grade** improvements:

1. **Camera Device Type** → Detect built-in, USB, or virtual cameras
2. **RAW14** → 14-bit color depth for pro photography
3. **VVC Codec** → 50% smaller video files (H.266)
4. **Quality Encoding** → Set quality, not bitrate
5. **CameraX Compatibility** → Official API 37 support

**The big picture:** Android is becoming a **professional creative platform**. With RAW14, VVC, and external camera support, Android devices can now compete with professional cameras and video equipment.

**Developer Action Items:**
- Update CameraX to 1.5.2+ for API 37 support
- Use `INFO_DEVICE_TYPE` for USB camera support
- Consider VVC for video-intensive apps
- Switch to quality-based encoding for simplicity