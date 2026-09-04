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
            // Pre-37: PickVisualMedia / ACTION_PICK_IMAGES without aspect-ratio extras.
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
            "The system Photo Picker arrived in API 33 as the replacement for broad " +
                "READ_EXTERNAL_STORAGE. API 37 adds PhotoPickerUiCustomizationParams so " +
                "thumbnails can be portrait 9:16 (social/video apps) or square.\n\n" +
                "ActivityResultContracts.PickVisualMedia() is still the right launch path for " +
                "a standalone picker. The new params attach to the embedded picker via " +
                "EmbeddedPhotoPickerFeatureInfo (SurfaceControlViewHost)."
        ) {
            // These values come from API 37 classes, so the gate is SDK_INT, not an SDK
            // extension version. Checking an extension here was backwards: it could show
            // the readout on an API 30 device and hide it on a real Android 17 one.
            if (AndroidApis.isAndroid17) {
                Text(
                    "uiParams.aspectRatio=${uiParams?.aspectRatio ?: "n/a"} " +
                        "embedded.max=${embeddedInfo?.maxSelectionLimit ?: "n/a"}"
                )
            }
            // SDK extensions are the right tool for a different question: whether the
            // *photo picker itself* is present, since it also ships to older releases
            // through an extension rather than a platform version bump.
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
