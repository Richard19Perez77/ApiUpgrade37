package com.rick.apiupgrade37.ui.screens

import android.app.Notification
import android.app.NotificationManager
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.ApiUpgrade37App
import com.rick.apiupgrade37.R
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import com.rick.apiupgrade37.ui.rememberNotificationGate

/**
 * - API 37: Notification.MetricStyle + Notification.Metric (value, label, optional
 *   SEMANTIC_STYLE_*) as a platform template.
 * - Pre-37: BigText / Inbox / custom RemoteViews for health, timer, or travel readouts.
 * - Nicety — old styles still post. POST_NOTIFICATIONS is required on 17 devices, but
 *   that is API 33.
 */
@Composable
fun MetricStyleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val notifications = rememberNotificationGate()

    FeatureScaffold("MetricStyle", onBack) { padding ->
        FeatureBody(
            padding,
            "Notification.MetricStyle (API 37) is a template for health, timers, stopwatch, " +
                "and travel metrics. Each Metric has a value + label and optional semantic style."
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    // `enabled` is not a gate lint understands, so the guard is repeated here.
                    if (!AndroidApis.isAndroid17) return@Button
                    // API 33+: without a POST_NOTIFICATIONS grant, notify() is a silent no-op.
                    if (!notifications.ensure()) return@Button
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
