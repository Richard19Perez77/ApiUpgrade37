package com.rick.apiupgrade37.ui.screens

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import android.text.SpannableStringBuilder
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import com.rick.apiupgrade37.ApiUpgrade37App
import com.rick.apiupgrade37.R
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import com.rick.apiupgrade37.ui.rememberNotificationGate

/**
 *
 *  Semantic Color Annotations
 *
 *
 * - API 37: Notification.createSemanticStyleAnnotation (SAFE / CAUTION / DANGER / INFO) plus setRequestPromotedOngoing(true).
 *      Custom RemoteViews are memory-capped more tightly.
 *
 * - Pre-37: Ongoing notifications without semantic colors.
 *      Custom RemoteViews were looser (URI-based bypasses still worked).
 *
 * - Mixed — semantic colors and promoted ongoing are niceties.
 *      Stricter RemoteViews limits at target 37 are a need if you still use custom layouts.
 *
 */
@Composable
fun LiveUpdateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val notifications = rememberNotificationGate()

    FeatureScaffold("Live Update colors", onBack) { padding ->
        FeatureBody(
            padding,
            "Live Updates gain semantic colors with universal meaning: SAFE (green), " +
                "CAUTION (orange), DANGER (red), INFO (blue). Apply them with " +
                "Notification.createSemanticStyleAnnotation on spans, or setSemanticStyle on " +
                "ProgressStyle points/segments.\n\n" +
                "setRequestPromotedOngoing(true) asks the system to keep this as a promoted " +
                "ongoing Live Update.\n\n" +
                "Custom RemoteViews notifications are memory-capped more strictly when targeting 37 " +
                "(including URI loopholes). Prefer platform styles."
        ) {
            Button(
                onClick = {
                    // API 33+: without a POST_NOTIFICATIONS grant, notify() is a silent no-op.
                    if (!notifications.ensure()) return@Button
                    val nm = context.getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                        val text = SpannableStringBuilder()
                            .append("NONE ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_UNSPECIFIED), 0)
                            .append("INFO ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO), 0)
                            .append("SAFE ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_SAFE), 0)
                            .append("CAUTION ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_CAUTION), 0)
                            .append("DANGER", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_DANGER), 0)
                        val notification = Notification.Builder(context, ApiUpgrade37App.CHANNEL_LIVE)
                            .setSmallIcon(R.drawable.ic_stat_api)
                            .setContentTitle("Semantic Live Update")
                            .setContentText(text)
                            .setOngoing(true)
                            .setRequestPromotedOngoing(true)
                            .build()
                        nm.notify(1701, notification)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notification = NotificationCompat.Builder(context, ApiUpgrade37App.CHANNEL_LIVE)
                            .setSmallIcon(R.drawable.ic_stat_api)
                            .setContentTitle("Legacy ongoing")
                            .setContentText("Semantic annotations require API 37")
                            .setOngoing(true)
                            .build()
                        nm.notify(1701, notification)
                    }
                }
            ) { Text("Post semantic Live Update") }
        }
    }
}
