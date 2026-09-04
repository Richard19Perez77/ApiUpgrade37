package com.rick.apiupgrade37.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * POST_NOTIFICATIONS became a runtime permission in API 33 (Tiramisu), so every Android 17
 * device needs an explicit user grant before NotificationManager.notify() shows anything.
 *
 * Declaring the permission in the manifest is not enough. Without this gate the Live Update
 * and MetricStyle buttons appear to work and silently post nothing, which looks like the
 * API 37 notification code is broken when it is not.
 *
 * Pre-33 there was no notification permission; notify() always displayed.
 */
class NotificationGate internal constructor(
    private val isGranted: () -> Boolean,
    private val request: () -> Unit
) {
    /**
     * Returns true when it is safe to post. When it returns false a permission prompt has
     * been shown, and the caller should simply do nothing this time round.
     */
    fun ensure(): Boolean {
        if (isGranted()) return true
        request()
        return false
    }
}

@Composable
fun rememberNotificationGate(): NotificationGate {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result -> granted = result }

    return remember(launcher) {
        NotificationGate(
            isGranted = { granted },
            request = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        )
    }
}
