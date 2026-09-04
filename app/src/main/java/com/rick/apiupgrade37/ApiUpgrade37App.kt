package com.rick.apiupgrade37

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.ProfilingManager
import android.os.ProfilingTrigger
import android.util.Log
import androidx.annotation.RequiresApi
import com.rick.apiupgrade37.core.AndroidApis
import java.util.concurrent.Executors

/**
 * Application entry used to register API 37 profiling triggers as early as possible.
 *
 * ProfilingManager itself shipped in API 35. Android 17 adds:
 * [ProfilingTrigger.TRIGGER_TYPE_COLD_START], [ProfilingTrigger.TRIGGER_TYPE_OOM],
 * [ProfilingTrigger.TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE], [ProfilingTrigger.TRIGGER_TYPE_ANOMALY].
 */
class ApiUpgrade37App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        if (AndroidApis.isAndroid17) {
            registerApi37ProfilingTriggers()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_LIVE,
                getString(R.string.notification_channel_live),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_METRICS,
                getString(R.string.notification_channel_metrics),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    @RequiresApi(AndroidApis.ANDROID_17)
    private fun registerApi37ProfilingTriggers() {
        val profiling = getSystemService(ProfilingManager::class.java) ?: return
        val triggers = listOf(
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_ANOMALY).build(),
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_OOM).build(),
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_COLD_START).build(),
            ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE).build(),
        )
        profiling.addProfilingTriggers(triggers)
        profiling.registerForAllProfilingResults(Executors.newSingleThreadExecutor()) { result ->
            Log.i(TAG, "profile error=${result.errorCode} path=${result.resultFilePath}")
        }
    }

    companion object {
        const val CHANNEL_LIVE = "live_updates"
        const val CHANNEL_METRICS = "metrics"
        private const val TAG = "ApiUpgrade37"
    }
}
