package com.rick.apiupgrade37.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun AlarmListenerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    FeatureScaffold("Idle alarm listener", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 adds setExactAndAllowWhileIdle(type, trigger, tag, executor, OnAlarmListener). " +
                "Use it instead of a sticky WakeLock + looping handler for short idle-window work " +
                "(socket keepalive, sync tick).\n\n" +
                "Pre-37 allow-while-idle required a PendingIntent (BroadcastReceiver). " +
                "The listener form avoids a manifest receiver and the extra process wake from an implicit broadcast."
        ) {
            Button(
                onClick = {
                    val am = context.getSystemService(AlarmManager::class.java)
                    val trigger = SystemClock.elapsedRealtime() + 8_000
                    if (AndroidApis.isAndroid17) {
                        am.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            trigger,
                            "api37-demo",
                            ContextCompat.getMainExecutor(context)
                        ) {
                            Toast.makeText(context, "OnAlarmListener fired", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val pi = PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent("com.rick.apiupgrade37.DEMO_ALARM"),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pi)
                    }
                }
            ) { Text("Schedule 8s allow-while-idle alarm") }
        }
    }
}
