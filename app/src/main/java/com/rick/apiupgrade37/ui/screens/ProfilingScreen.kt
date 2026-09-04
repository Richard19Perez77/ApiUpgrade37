package com.rick.apiupgrade37.ui.screens

import android.os.Build
import android.os.ProfilingManager
import android.os.ProfilingTrigger
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 *
 *  Automatic cold start startup analysis tracing.
 *  OOM auto heap dump for perfect memory leak analysis.
 *  CPU kill was impossible to detect, now automatic, can catch binder and spam/memory issues.
 *  Anomalies like binder, spam and memory issues are possible to detect.
 *
 * - API 37: ProfilingManager.addProfilingTriggers registers COLD_START, OOM, KILL_EXCESSIVE_CPU_USAGE, and ANOMALY.
 *      This is so the system can dump traces/heaps without a manual requestProfiling() call.
 *
 * - Pre-37: ProfilingManager existed from API 35 for on-demand captures.
 *      Before that: Debug.dumpHprofData() or manual traces.
 *
 * - Nicety — debug/ops.
 *      Useful next to the Android 17 memory killer, not required to run.
 *
 */
@Composable
fun ProfilingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val present = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        AndroidApis.isAndroid17 &&
            context.getSystemService(ProfilingManager::class.java) != null
    } else {
        // Pre-35: Debug.dumpHprofData() / manual traces. ProfilingManager starts at API 35.
        false
    }

    FeatureScaffold("Profiling triggers", onBack) { padding ->
        FeatureBody(
            padding,
            "ProfilingManager (API 35) gained Android 17 system triggers. " +
                "ApiUpgrade37App registers them at process start so cold-start traces can fire.\n\n" +
                "TRIGGER_TYPE_COLD_START — stack sample + system trace\n" +
                "TRIGGER_TYPE_OOM — Java heap dump (your UncaughtExceptionHandler MUST call the default handler)\n" +
                "TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE — stack sample before a CPU kill\n" +
                "TRIGGER_TYPE_ANOMALY — heap dump / binder spam profile before MemoryLimiter kills you\n\n" +
                "Older approach: Debug.dumpHprofData() from a signal, or manual " +
                "ProfilingManager.requestProfiling() (still valid for on-demand captures)."
        ) {
            Text("Triggers registered in Application: $present")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                Text(
                    "Trigger constants: " +
                        "OOM=${ProfilingTrigger.TRIGGER_TYPE_OOM} " +
                        "ANOMALY=${ProfilingTrigger.TRIGGER_TYPE_ANOMALY} " +
                        "COLD=${ProfilingTrigger.TRIGGER_TYPE_COLD_START}"
                )
            }
        }
    }
}
