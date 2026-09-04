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

/**
 * - API 37: CameraCharacteristics.INFO_DEVICE_TYPE (built-in / USB / virtual),
 *   ImageFormat.RAW14, MediaFormat.MIMETYPE_VIDEO_VVC, and
 *   MediaRecorder.setVideoEncodingQuality() for constant-quality encode.
 * - Pre-37: INFO_SUPPORTED_HARDWARE_LEVEL only; RAW10/RAW12; HEVC/AV1; bitrate via
 *   setVideoEncodingBitRate().
 * - Nicety — new capture/codec knobs. Need if you use CameraX on Android 17:
 *   upgrade to 1.5.2 / 1.6.0+ or a dynamic-range mode can crash.
 */
@Composable
fun CameraMediaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // Enumerating cameras talks to the camera service over binder and can block for a
    // noticeable time on first access, so keep it off the composition thread.
    val report by produceState(initialValue = "Querying cameras…", context) {
        value = withContext(Dispatchers.IO) { describeCameras(context) }
    }

    FeatureScaffold("Camera & media", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 camera/media additions:\n" +
                "• CameraCharacteristics.INFO_DEVICE_TYPE — built-in / USB / virtual\n" +
                "• ImageFormat.RAW14 — 14-bit Bayer\n" +
                "• MediaFormat.MIMETYPE_VIDEO_VVC (H.266) for OEM codecs\n" +
                "• MediaRecorder.setVideoEncodingQuality() — constant-quality encode\n" +
                "• Extended HE-AAC software encoder + Eclipsa HDR metadata (platform)\n" +
                "• CameraX 1.5.2 / 1.6.0+ required on 17 devices (dynamic-range crash otherwise)\n\n" +
                "Pre-37: Camera2 INFO_SUPPORTED_HARDWARE_LEVEL only; RAW10/RAW12; HEVC/AV1."
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

// Both cameraIdList and getCameraCharacteristics throw CameraAccessException when the
// camera service is unavailable, which is routine on emulators without a configured
// camera and on devices where another app holds the camera. Uncaught, it takes the
// screen down on open.
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
