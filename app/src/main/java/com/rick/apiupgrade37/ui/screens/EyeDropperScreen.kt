package com.rick.apiupgrade37.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 * - API 37: Intent.ACTION_OPEN_EYE_DROPPER opens a system sampler and returns
 *   Intent.EXTRA_COLOR (packed ARGB).
 *
 * - Pre-37: No platform eyedropper. Apps captured the screen (MediaProjection /
 *   screenshots) just to read one pixel.
 *
 * - Nicety — optional unless you were capturing the display only to sample a color.
 */
@Composable
fun EyeDropperScreen(onBack: () -> Unit) {
    var color by remember { mutableIntStateOf(Color.GRAY) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                color = result.data?.getIntExtra(Intent.EXTRA_COLOR, Color.BLACK) ?: Color.BLACK
            }
        }
    }

    FeatureScaffold("Eyedropper", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37: Intent.ACTION_OPEN_EYE_DROPPER lets the user sample any on-screen pixel " +
                "through a system UI. That replaces the old (privacy-hostile) approach of " +
                "MediaProjection / READ_FRAME_BUFFER / taking a screenshot just to read a color.\n\n" +
                "Result extra: Intent.EXTRA_COLOR (packed ARGB int)."
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(ComposeColor(color.toLong() and 0xFFFFFFFFL))
            )
            Text(String.format("#%08X", color))
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    // `enabled` is not a gate lint understands, so the guard is repeated here.
                    if (!AndroidApis.isAndroid17) return@Button
                    launcher.launch(Intent(Intent.ACTION_OPEN_EYE_DROPPER))
                }
            ) {
                Text(if (AndroidApis.isAndroid17) "Open system eyedropper" else "Requires API 37 device")
            }
        }
    }
}
