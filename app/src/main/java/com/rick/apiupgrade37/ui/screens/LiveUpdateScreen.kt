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
 *      // todo fill in details
 *
 *      package com.rick.apiupgrade37.ui.screens
 *
 * import android.app.Notification
 * import android.app.NotificationManager
 * import android.os.Build
 * import android.text.SpannableStringBuilder
 * import androidx.compose.material3.Button
 * import androidx.compose.material3.Text
 * import androidx.compose.runtime.Composable
 * import androidx.compose.ui.platform.LocalContext
 * import androidx.core.app.NotificationCompat
 * import com.rick.apiupgrade37.ApiUpgrade37App
 * import com.rick.apiupgrade37.R
 * import com.rick.apiupgrade37.ui.FeatureBody
 * import com.rick.apiupgrade37.ui.FeatureScaffold
 * import com.rick.apiupgrade37.ui.rememberNotificationGate
 *
 * @Composable
 * fun LiveUpdateScreen(onBack: () -> Unit) {
 *     val context = LocalContext.current
 *     val notifications = rememberNotificationGate()
 *
 *     FeatureScaffold("Live Update colors", onBack) { padding ->
 *         FeatureBody(
 *             padding,
 *             ""
 *         ) {
 *             Button(
 *                 onClick = {
 *                     // API 33+: without a POST_NOTIFICATIONS grant, notify() is a silent no-op.
 *                     if (!notifications.ensure()) return@Button
 *                     val nm = context.getSystemService(NotificationManager::class.java)
 *                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
 *                         val text = SpannableStringBuilder()
 *                             .append("NONE ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_UNSPECIFIED), 0)
 *                             .append("INFO ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO), 0)
 *                             .append("SAFE ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_SAFE), 0)
 *                             .append("CAUTION ", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_CAUTION), 0)
 *                             .append("DANGER", Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_DANGER), 0)
 *                         val notification = Notification.Builder(context, ApiUpgrade37App.CHANNEL_LIVE)
 *                             .setSmallIcon(R.drawable.ic_stat_api)
 *                             .setContentTitle("Semantic Live Update")
 *                             .setContentText(text)
 *                             .setOngoing(true)
 *                             .setRequestPromotedOngoing(true)
 *                             .build()
 *                         nm.notify(1701, notification)
 *                     } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *                         val notification = NotificationCompat.Builder(context, ApiUpgrade37App.CHANNEL_LIVE)
 *                             .setSmallIcon(R.drawable.ic_stat_api)
 *                             .setContentTitle("Legacy ongoing")
 *                             .setContentText("Semantic annotations require API 37")
 *                             .setOngoing(true)
 *                             .build()
 *                         nm.notify(1701, notification)
 *                     }
 *                 }
 *             ) { Text("Post semantic Live Update") }
 *         }
 *     }
 * }
 *
 *
 * Analysis of LiveUpdateScreen.kt
 * What This Class Does
 * LiveUpdateScreen is a Compose UI screen that demonstrates Android API 37's new semantic color annotations for notifications. It shows how apps can now apply universally understood colors (green = safe, red = danger, etc.) to text in notifications and promote ongoing notifications.
 *
 * What are Semantic Live Updates?
 * Live Updates are ongoing notifications that show real-time information like:
 *
 * 🚗 Ride sharing (driver ETA, car location)
 * 🏃 Fitness tracking (pace, heart rate, distance)
 * 🍔 Food delivery (order status, driver location)
 * 🎵 Media playback (now playing, progress)
 * 🚗 Navigation (directions, ETA)
 * The Problem Before API 37:
 *
 * All text was plain black/white
 * No visual distinction between statuses
 * Hard to quickly understand the state
 * No universal color language
 * The API 37 Solution:
 *
 * Semantic colors for text spans
 * Green = SAFE ✅, Orange = CAUTION ⚠️, Red = DANGER ❌, Blue = INFO ℹ️
 * Universal meaning across all apps
 * Better at-a-glance understanding
 * The API 37 Improvements
 * Feature	Before (API ≤36)	After (API 37)	Improvement
 * Text Colors	Black/white only	Semantic colors (green, red, etc.)	Clarity 🎨
 * Meaning	No visual indicators	Color = status	At-a-Glance 👁️
 * Promoted Ongoing	Could be hidden	Request to promote	Visibility 📌
 * RemoteViews Limits	Loose (URI bypasses)	Strict caps	Security 🔒
 * Universal Language	App-specific	Platform-wide	Consistency
 * Semantic Styles Explained
 * Style	Color	Meaning	Example Use
 * SEMANTIC_STYLE_UNSPECIFIED	Default	No specific meaning	Plain text
 * SEMANTIC_STYLE_INFO	Blue	Information	"Driver assigned"
 * SEMANTIC_STYLE_SAFE	Green	Safe/Good	"On time", "Completed"
 * SEMANTIC_STYLE_CAUTION	Yellow/Orange	Caution	"Delayed 5 min"
 * SEMANTIC_STYLE_DANGER	Red	Danger/Alert	"Cancelled", "Emergency"
 * Code Analysis
 * 1. Creating Semantic Annotations
 * val text = SpannableStringBuilder()
 *     .append("NONE ", Notification.createSemanticStyleAnnotation(
 *         Notification.SEMANTIC_STYLE_UNSPECIFIED
 *     ), 0)
 *     .append("INFO ", Notification.createSemanticStyleAnnotation(
 *         Notification.SEMANTIC_STYLE_INFO
 *     ), 0)
 *     .append("SAFE ", Notification.createSemanticStyleAnnotation(
 *         Notification.SEMANTIC_STYLE_SAFE
 *     ), 0)
 *     .append("CAUTION ", Notification.createSemanticStyleAnnotation(
 *         Notification.SEMANTIC_STYLE_CAUTION
 *     ), 0)
 *     .append("DANGER", Notification.createSemanticStyleAnnotation(
 *         Notification.SEMANTIC_STYLE_DANGER
 *     ), 0)
 *
 * What it does:
 *
 * Creates a SpannableStringBuilder
 * Applies semantic annotations to text spans
 * Each word gets a different color
 * Demonstrates all available styles
 * Visual Result:
 *
 * NONE INFO SAFE CAUTION DANGER
 *   ^    ^    ^     ^       ^
 * Gray Blue Green Orange   Red
 *
 * 2. Promoted Ongoing Notifications
 * .setOngoing(true)                    // Keep notification persistent
 * .setRequestPromotedOngoing(true)     // Request promotion to top
 *
 * What setRequestPromotedOngoing(true) Does:
 *
 * Requests the system to promote this notification
 * Makes it more visible in the notification shade
 * Particularly important for Live Updates
 * System decides if it promotes (battery/power considerations)
 * Before/After:
 *
 * Before (API 36):
 * Live Update notification was buried with others
 * User might not see it
 *
 * After (API 37):
 * Live Update notification appears at top
 * User sees it immediately ✅
 *
 * 3. API Version Handling
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
 *     // API 37: Full semantic support
 *     // Create notification with semantic colors
 * } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *     // API 26-36: Basic ongoing notification
 *     // No semantic colors
 *     notification = NotificationCompat.Builder(...)
 *         .setContentText("Semantic annotations require API 37")
 *         .setOngoing(true)
 *         .build()
 * }
 *
 * Why Multiple Branches:
 *
 * API 37+ → Full semantic colors
 * API 26-36 → Basic ongoing notification (fallback)
 * API 25- → Not supported (notification channels not available)
 * Real-World Use Cases
 * 1. Ride Sharing App
 * // Driver is approaching
 * val status = SpannableStringBuilder()
 *     .append("Driver ",
 *         Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO))
 *     .append("is 2 min away",
 *         Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_SAFE))
 *
 * // Result: "Driver is 2 min away" (Info + Green)
 *
 * 2. Fitness Tracking
 * // Heart rate monitoring
 * val status = SpannableStringBuilder()
 *     .append("Heart Rate: ",
 *         Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO))
 *     .append("72 bpm",
 *         if (heartRate > 100)
 *             Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_DANGER)
 *         else
 *             Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_SAFE))
 *
 * // Result: "Heart Rate: 72 bpm" (Green = safe)
 * // Result: "Heart Rate: 150 bpm" (Red = danger)
 *
 * 3. Food Delivery
 * // Order status
 * val status = SpannableStringBuilder()
 *     .append("Order ",
 *         Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO))
 *     .append(getStatusText(status),
 *         Notification.createSemanticStyleAnnotation(statusColor))
 *
 * // Status: "Preparing" = SAFE (green)
 * // Status: "Delayed" = CAUTION (orange)
 * // Status: "Cancelled" = DANGER (red)
 *
 * 4. Navigation
 * // ETA status
 * val status = SpannableStringBuilder()
 *     .append("ETA: ",
 *         Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_INFO))
 *     .append("15 min",
 *         if (isOnTime)
 *             Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_SAFE)
 *         else
 *             Notification.createSemanticStyleAnnotation(Notification.SEMANTIC_STYLE_CAUTION))
 *
 * // Result: "ETA: 15 min" (Green = on time)
 * // Result: "ETA: 25 min" (Orange = delayed)
 *
 * Before vs After: User Experience
 * ❌ BEFORE (API 36):
 * 🚗 Ride Sharing
 * Driver is 5 min away
 * ------------------
 * [View] [Cancel]
 *
 * Problems:
 * - All text same color
 * - Can't quickly tell status
 * - Hard to scan in a hurry
 * - No visual hierarchy
 *
 * ✅ AFTER (API 37):
 * 🚗 Ride Sharing
 * Driver is 5 min away
 * ------------------
 * [View] [Cancel]
 *
 * Benefits:
 * - "Driver" = blue (info)
 * - "5 min away" = green (safe)
 * - Quick status at a glance
 * - Universal color language
 *
 * Stricter RemoteViews Limits
 * What Changed:
 * // Before (API 36):
 * // Custom RemoteViews had looser limits
 * // Could use URI-based resources
 * // More memory for custom layouts
 *
 * // After (API 37):
 * // Custom RemoteViews are memory-capped
 * // URI loopholes closed
 * // Must use standard views
 * // Platform styles preferred
 *
 * Why This Matters:
 * // ❌ BAD (API 37): Custom RemoteViews with large images
 * val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification)
 * remoteViews.setImageViewUri(R.id.image, largeImageUri)  // May be killed!
 *
 * // ✅ GOOD (API 37): Platform styles
 * val style = Notification.MetricStyle()  // Platform-provided
 * // More efficient, less memory, better security
 *
 * The Complete Notification Builder
 * val notification = Notification.Builder(context, CHANNEL_LIVE)
 *     .setSmallIcon(R.drawable.ic_stat_api)
 *     .setContentTitle("Semantic Live Update")
 *     .setContentText(semanticText)
 *     .setOngoing(true)                    // Persistent notification
 *     .setRequestPromotedOngoing(true)     // Request promotion to top
 *     .build()
 *
 * // System decides if promoted based on:
 * // 1. Ongoing status
 * // 2. User engagement
 * // 3. System resources
 * // 4. Battery state
 * // 5. Notification importance
 *
 * Semantic Colors in Other Notification Styles
 * ProgressStyle
 * // ProgressStyle can also use semantic colors
 * val progressStyle = Notification.ProgressStyle()
 *     .addPoint(
 *         ProgressPoint(50, "Halfway", SEMANTIC_STYLE_INFO)
 *     )
 *     .addPoint(
 *         ProgressPoint(100, "Complete", SEMANTIC_STYLE_SAFE)
 *     )
 *
 * MetricStyle
 * // MetricStyle uses semantic styles
 * val metric = Notification.Metric(
 *     Notification.Metric.FixedInt(72, "bpm"),
 *     "Heart Rate",
 *     SEMANTIC_STYLE_INFO  // Blue
 * )
 *
 * Key Improvements Summary
 * Improvement	What It Does	Who Benefits
 * Semantic Colors	Color-coded text spans	All users (quick status)
 * Universal Meaning	Same colors across apps	All users (consistency)
 * Promoted Ongoing	More visible notifications	Live Update apps
 * Stricter RemoteViews	Better memory management	System performance
 * Platform Styles	Preferred over custom	Developers
 * The "Why" Behind This Change
 * Before Android 17:
 *
 * All notification text was monochrome
 * Status couldn't be communicated visually
 * Ongoing notifications were easily hidden
 * Custom RemoteViews could abuse memory
 * Users had to read every word to understand
 * After Android 17:
 *
 * Semantic colors show status instantly
 * Universal color language (green = safe, red = danger)
 * Promoted ongoing are more visible
 * Stricter limits improve system health
 * Users can glance and know the status
 * Summary
 * API 37's Live Update improvements are about visual communication:
 *
 * Semantic Colors → Text with meaning (green = safe, red = danger)
 * Universal Language → Same colors across all apps
 * Promoted Ongoing → Important notifications are visible
 * Stricter Limits → Better system health
 * Platform Preferences → Easier for developers
 * The big picture: Android is making notifications more human-friendly by adding color coding. Just as traffic lights use red/yellow/green universally, notification text can now use the same visual language:
 *
 * 🟢 Green = Safe/Good/On Time
 * 🟡 Yellow = Caution/Delayed/Warning
 * 🔴 Red = Danger/Cancelled/Alert
 * 🔵 Blue = Information/Neutral
 * Developer Action Items:
 *
 * Use semantic styles for status updates
 * Apply appropriate colors based on state
 * Use setRequestPromotedOngoing(true) for Live Updates
 * Prefer platform styles over custom RemoteViews
 * Test with various status scenarios
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
