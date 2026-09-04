package com.rick.apiupgrade37.ui.screens

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun MemoryLimitsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val report = remember {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            "ApplicationExitInfo requires API 30+"
        } else {
            val am = context.getSystemService(ActivityManager::class.java)
            val exits = am.getHistoricalProcessExitReasons(context.packageName, 0, 8)
            if (exits.isEmpty()) {
                "No recorded exits yet. After a MemoryLimiter kill, getDescription() may contain MemoryLimiter:AnonSwap."
            } else {
                exits.joinToString("\n") { info ->
                    "reason=${reasonName(info.reason)} desc=${info.description}"
                }
            }
        }
    }

    FeatureScaffold("Memory limits", onBack) { padding ->
        FeatureBody(
            padding,
            "Android 17 enforces per-app anonymous+swap caps based on device RAM and will " +
                "kill offenders. Pair this with R8 full mode, LeakCanary in Studio, and " +
                "ProfilingTrigger.TRIGGER_TYPE_ANOMALY.\n\n" +
                "ART also runs more frequent young-gen collections (Play system updates back to API 31).\n\n" +
                "Do not try to 'test' the killer by allocating until OOM in a production path; " +
                "read historical exit reasons instead."
        ) {
            Text(report)
        }
    }
}

private fun reasonName(reason: Int): String = when (reason) {
    ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
    ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
    ApplicationExitInfo.REASON_CRASH -> "CRASH"
    ApplicationExitInfo.REASON_ANR -> "ANR"
    else -> reason.toString()
}
