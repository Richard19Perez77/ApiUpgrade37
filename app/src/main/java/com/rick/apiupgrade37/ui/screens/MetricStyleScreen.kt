package com.rick.apiupgrade37.ui.screens

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.ApiUpgrade37App
import com.rick.apiupgrade37.R
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun MetricStyleScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    FeatureScaffold("MetricStyle", onBack) { padding ->
        FeatureBody(
            padding,
            "Notification.MetricStyle (API 37) is a template for health, timers, stopwatch, " +
                "and travel metrics. Each Metric has a value + label and optional semantic style."
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) return@Button
                    val heart = Notification.Metric(
                        Notification.Metric.FixedInt(72, "bpm"),
                        "Heart rate",
                        Notification.SEMANTIC_STYLE_INFO
                    )
                    val status = Notification.Metric(
                        Notification.Metric.FixedText("On time"),
                        "ETA",
                        Notification.SEMANTIC_STYLE_SAFE
                    )
                    val style = Notification.MetricStyle()
                        .addMetric(heart)
                        .addMetric(status)
                        .setCriticalMetric(0)
                    val notification = Notification.Builder(context, ApiUpgrade37App.CHANNEL_METRICS)
                        .setSmallIcon(R.drawable.ic_stat_api)
                        .setContentTitle("Workout")
                        .setStyle(style)
                        .build()
                    context.getSystemService(NotificationManager::class.java).notify(1702, notification)
                }
            ) { Text("Post MetricStyle notification") }
        }
    }
}
