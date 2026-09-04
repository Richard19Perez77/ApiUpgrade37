package com.rick.apiupgrade37.ui.screens

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 *  Memory Management Enforcement
 *      strict enforcing per-app memory caps
 *      kill apps that exceed them
 *
 *  API 37 provides:
 *      Per-app anonymous + swap memory caps
 *      Memroy limiter
 *      Kills offenders
 *      Detailed exit reasons
 *
 *  Anonymous Memory
 *      Java heap
 *      Native heap
 *      Stack memory
 *      Bitmap pixel data
 *      Code cache
 *
 *  Reasons:
 *      LOW_MEMORY
 *      EXCESSIVE_RESOURCE_USAGE
 *      CRASH
 *      ANR
 *
 *  Memroy Limiter Exit Description
 *      AnonSwap
 *      GraphicBuffer
 *      NativeHeap
 *      JavaHeap
 *
 *  Real world prevention reasons:
 *      Memory Leak
 *      Large Bitmap
 *      Native Memory
 *
 *  Prevention:
 *      1. Use LeakCanary
 *      2. Enable R8 Full Mode
 *      3. Use Bitmap Pooling
 *      4. Use Profiling Triggers
 *
 *  ART imrprovements:
 *      lower memory footprint
 *      better performance
 *      faster gc pauses
 *      backported to API 31+ via Play System Updates
 *
 *  Android 17:
 *      hard caps prevent abuse
 *      offending apps get killed immediately
 *      system stays healthy for other apps
 *      clear diagnostics show why app died
 *      developers can fix memory issues proactively
 *
 *  API 37 - majory step forward
 *      hard per app caps
 *      memory limiter
 *      exit diagnostics
 *      better gc
 *      backported improvements
 *
 *  Needed for:
 *      lowend devices with linited RAM
 *      enterprise deployment
 *      kiosk devices
 *      multitasking scenarios
 *
 *  Developer Action Items:
 *      fix memory leaks with LeakCanary
 *      use R8 full mode for optimization
 *      monitory memory usage with profiling triggers
 *      check ApplicationExitInfo for Memory/Limiter kills
 *      implement bitmap pooling to reduce allocations
 *
 *
 *
 * - API 37: The OS enforces per-app anonymous+swap caps and may kill offenders.
 *      This screen reads ApplicationExitInfo; getDescription() may contain MemoryLimiter:AnonSwap.
 *      Pair with ProfilingTrigger.TRIGGER_TYPE_ANOMALY.
 *
 * - Pre-37: No AnonSwap memory limiter.
 *      You still had LMK / LOW_MEMORY exits; getHistoricalProcessExitReasons exists from API 30.
 *
 * - Need — you cannot opt out of the killer on Android 17 devices.
 *      Reading exit reasons is a nicety (diagnostics), not required for correctness.
 *
 */
@Composable
fun MemoryLimitsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // getHistoricalProcessExitReasons is a binder round-trip to ActivityManager. Running it
    // inside remember {} would block the main thread during composition, which is the kind
    // of thing that shows up as a dropped frame on entry and an ANR on a slow device.
    val report by produceState(initialValue = "Reading exit reasons…", context) {
        value = withContext(Dispatchers.IO) { exitReport(context) }
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

private fun exitReport(context: Context): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return "ApplicationExitInfo requires API 30+"
    val am = context.getSystemService(ActivityManager::class.java)
        ?: return "No ActivityManager on this device"
    val exits = am.getHistoricalProcessExitReasons(context.packageName, 0, 8)
    if (exits.isEmpty()) {
        return "No recorded exits yet. After a MemoryLimiter kill, " +
            "getDescription() may contain MemoryLimiter:AnonSwap."
    }
    return exits.joinToString("\n") { info ->
        "reason=${reasonName(info.reason)} desc=${info.description}"
    }
}

private fun reasonName(reason: Int): String = when (reason) {
    ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
    ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
    ApplicationExitInfo.REASON_CRASH -> "CRASH"
    ApplicationExitInfo.REASON_ANR -> "ANR"
    else -> reason.toString()
}
