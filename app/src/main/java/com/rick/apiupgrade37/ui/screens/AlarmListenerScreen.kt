package com.rick.apiupgrade37.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun AlarmListenerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Nothing scheduled yet") }

    // ACTION_REQUEST_SCHEDULE_EXACT_ALARM hands the user to a settings page, so the grant
    // lands while this screen is stopped. The platform guidance is to re-check on resume;
    // otherwise the UI keeps claiming the permission is missing after the user granted it.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                val am = context.getSystemService(AlarmManager::class.java)
                if (!am.canScheduleExactAlarms()) {
                    status = "Exact alarms still not permitted"
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FeatureScaffold("Idle alarm listener", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 adds setExactAndAllowWhileIdle(type, trigger, tag, executor, OnAlarmListener). " +
                "Use it instead of a sticky WakeLock + looping handler for short idle-window work " +
                "(socket keepalive, sync tick).\n\n" +
                "Pre-37 allow-while-idle required a PendingIntent (BroadcastReceiver). " +
                "The listener form avoids a manifest receiver and the extra process wake from an implicit broadcast.\n\n" +
                "Declaring SCHEDULE_EXACT_ALARM is NOT enough. It has been a special app access " +
                "since API 31, and on Android 14+ devices it is not pre-granted to apps " +
                "targeting API 33+ unless they are clocks or calendars. Call " +
                "canScheduleExactAlarms() first and send the user to " +
                "ACTION_REQUEST_SCHEDULE_EXACT_ALARM, or every exact-alarm call throws " +
                "SecurityException.\n\n" +
                "The grant happens on a settings page, so this screen re-checks on resume. " +
                "Keep the try/catch as well: the permission can be revoked between the check " +
                "and the call."
        ) {
            Text(status)
            Button(
                onClick = {
                    val am = context.getSystemService(AlarmManager::class.java)
                    val trigger = SystemClock.elapsedRealtime() + 8_000

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !am.canScheduleExactAlarms()
                    ) {
                        status = "Exact alarms not permitted — opening system settings"
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData("package:${context.packageName}".toUri())
                        )
                        return@Button
                    }

                    try {
                        if (AndroidApis.isAndroid17) {
                            am.setExactAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                trigger,
                                "api37-demo",
                                ContextCompat.getMainExecutor(context)
                            ) {
                                Toast.makeText(context, "OnAlarmListener fired", Toast.LENGTH_SHORT).show()
                            }
                            status = "Scheduled via OnAlarmListener — no receiver needed"
                        } else {
                            // Pre-37 the only allow-while-idle form took a PendingIntent, which
                            // means a real BroadcastReceiver. This demo has none registered, so
                            // nothing observable happens on older devices; it is here to show
                            // the shape of the call you are replacing.
                            val pi = PendingIntent.getBroadcast(
                                context,
                                0,
                                Intent("com.rick.apiupgrade37.DEMO_ALARM").setPackage(context.packageName),
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            am.setExactAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                trigger,
                                pi
                            )
                            status = "Scheduled via PendingIntent (pre-37 form)"
                        }
                    } catch (e: SecurityException) {
                        // Reachable if the grant is revoked between the check and the call.
                        status = "SecurityException: ${e.message}"
                    }
                }
            ) { Text("Schedule 8s allow-while-idle alarm") }
        }
    }
}
