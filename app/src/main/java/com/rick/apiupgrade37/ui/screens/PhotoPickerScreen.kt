package com.rick.apiupgrade37.ui.screens

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
        PhotoPickerUiCustomizationParams.Builder()
            .setAspectRatio(PhotoPickerUiCustomizationParams.ASPECT_RATIO_PORTRAIT_9_16)
            .build()
    }
    val selection = remember {
        PhotoPickerSelectionParams.Builder()
            .setMimeTypes(listOf("image/*", "video/*"))
            .build()
    }
    val embeddedInfo = remember {
        EmbeddedPhotoPickerFeatureInfo.Builder()
            .setUiCustomizationParams(uiParams)
            .setSelectionParams(selection)
            .setMaxSelectionLimit(3)
            .build()
    }

    FeatureScaffold("Photo picker", onBack) { padding ->
        FeatureBody(
            padding,
            "The system Photo Picker arrived in API 33 as the replacement for broad " +
                "READ_EXTERNAL_STORAGE. API 37 adds PhotoPickerUiCustomizationParams so " +
                "thumbnails can be portrait 9:16 (social/video apps) or square.\n\n" +
                "ActivityResultContracts.PickVisualMedia() is still the right launch path for " +
                "a standalone picker. The new params attach to the embedded picker via " +
                "EmbeddedPhotoPickerFeatureInfo (SurfaceControlViewHost) — built below so you " +
                "can inspect the objects; wiring the surface is OEM/system UI."
        ) {
            Text("uiParams.aspectRatio=${uiParams.aspectRatio} embedded.max=${embeddedInfo.maxSelectionLimit}")
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
