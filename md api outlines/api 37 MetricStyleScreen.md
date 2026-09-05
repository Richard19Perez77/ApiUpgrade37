```kotlin
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

@Composable
fun MetricStyleScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val notifications = rememberNotificationGate()

    FeatureScaffold("MetricStyle", onBack) { padding ->
        FeatureBody(
            padding,
            ""
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    if (!AndroidApis.isAndroid17) return@Button
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
```

---

# Analysis of `MetricStyleScreen.kt`

## What This Class Does

`MetricStyleScreen` is a **Compose UI screen** that demonstrates Android API 37's new **`Notification.MetricStyle`** - a platform template for displaying metrics in notifications. It's designed for health apps, timers, travel, and other data-driven notifications.

---

## What is Notification.MetricStyle?

**MetricStyle** is a new notification template in API 37 that displays **key-value metrics** in a structured, platform-consistent way.

**Use Cases:**
- 🏃 **Health apps** → Heart rate, steps, calories
- ⏱️ **Timers** → Countdown, elapsed time
- ✈️ **Travel** → ETA, distance, speed
- 📊 **Fitness** → Workout metrics, progress
- 🚗 **Navigation** → Speed, distance to destination

---

## The API 37 Improvements

| Aspect | Before (API ≤36) | After (API 37) | Improvement |
|--------|-----------------|----------------|-------------|
| **Metric Display** | Custom RemoteViews | Platform template | **Consistency** 📱 |
| **Metrics Support** | Manual formatting | Structured `Metric` objects | **Simplicity** |
| **Semantic Styling** | Not available | Color-coded (INFO, SAFE, WARNING) | **Clarity** 🎨 |
| **Critical Metrics** | Manual highlighting | `setCriticalMetric()` | **Focus** |
| **Accessibility** | Limited | Built-in accessibility | **Inclusive** ♿ |

---

## The Problem Before API 37

### ❌ BAD (Pre-API 37):
```kotlin
// Apps had to build custom RemoteViews
val remoteViews = RemoteViews(context.packageName, R.layout.notification_metrics)

// Manual layout
remoteViews.setTextViewText(R.id.metric1_value, "72")
remoteViews.setTextViewText(R.id.metric1_label, "bpm")
remoteViews.setTextViewText(R.id.metric2_value, "On time")
remoteViews.setTextViewText(R.id.metric2_label, "ETA")

// Manual styling (inconsistent)
if (bpm > 100) {
    remoteViews.setTextColor(R.id.metric1_value, Color.RED)
}

// Problems:
// 1. Inconsistent across apps
// 2. Hard to maintain
// 3. Accessibility issues
// 4. No semantic meaning
// 5. Custom layout for every app
```

### ✅ GOOD (API 37):
```kotlin
// Platform-provided template
val style = Notification.MetricStyle()
    .addMetric(
        Notification.Metric(
            Notification.Metric.FixedInt(72, "bpm"),
            "Heart rate",
            Notification.SEMANTIC_STYLE_INFO
        )
    )
    .addMetric(
        Notification.Metric(
            Notification.Metric.FixedText("On time"),
            "ETA",
            Notification.SEMANTIC_STYLE_SAFE
        )
    )
    .setCriticalMetric(0)

// Benefits:
// 1. Consistent across apps
// 2. Easy to use
// 3. Built-in accessibility
// 4. Semantic meaning
// 5. Platform handles styling
```

---

## Code Analysis

### 1. **Creating a Metric**

```kotlin
val heart = Notification.Metric(
    Notification.Metric.FixedInt(72, "bpm"),  // Value + Unit
    "Heart rate",                              // Label
    Notification.SEMANTIC_STYLE_INFO           // Semantic style
)
```

**Metric Components:**
1. **Value**: `FixedInt` or `FixedText`
2. **Label**: Description of the metric
3. **Semantic Style**: Visual categorization

**Semantic Styles:**
```kotlin
SEMANTIC_STYLE_INFO    // Normal/neutral (blue/gray)
SEMANTIC_STYLE_SAFE    // Positive/safe (green)
SEMANTIC_STYLE_WARNING // Warning (yellow/orange)
SEMANTIC_STYLE_DANGER  // Danger/alert (red)
```

---

### 2. **Building the Style**

```kotlin
val style = Notification.MetricStyle()
    .addMetric(heart)      // Add first metric
    .addMetric(status)     // Add second metric
    .setCriticalMetric(0)  // Highlight first metric as critical
```

**What `setCriticalMetric()` Does:**
- Highlights the specified metric visually
- Makes it stand out from others
- Perfect for the most important data point

---

### 3. **Building and Posting the Notification**

```kotlin
val notification = Notification.Builder(context, ApiUpgrade37App.CHANNEL_METRICS)
    .setSmallIcon(R.drawable.ic_stat_api)
    .setContentTitle("Workout")    // Title shown above metrics
    .setStyle(style)               // Apply MetricStyle
    .build()

context.getSystemService(NotificationManager::class.java).notify(1702, notification)
```

---

## Metric Types

### 1. **FixedInt** - Numeric Values
```kotlin
// For numbers with units
Notification.Metric.FixedInt(72, "bpm")      // Heart rate
Notification.Metric.FixedInt(10000, "steps") // Step count
Notification.Metric.FixedInt(350, "cal")     // Calories
```

### 2. **FixedText** - Text Values
```kotlin
// For status or text-based metrics
Notification.Metric.FixedText("On time")     // Status
Notification.Metric.FixedText("Arriving")    // Progress
Notification.Metric.FixedText("Completed")   // Completion state
```

---

## Semantic Styles Explained

| Style | Color | Use Case | Example |
|-------|-------|----------|---------|
| `SEMANTIC_STYLE_INFO` | Blue/Gray | Neutral information | Heart rate, steps |
| `SEMANTIC_STYLE_SAFE` | Green | Positive/Safe | "On time", "Done" |
| `SEMANTIC_STYLE_WARNING` | Yellow | Warning | Battery low, late |
| `SEMANTIC_STYLE_DANGER` | Red | Danger/Critical | High heart rate, crash |

### Examples:

```kotlin
// Health App
SEMANTIC_STYLE_INFO → "72 bpm" (normal)
SEMANTIC_STYLE_DANGER → "150 bpm" (too high!)

// Travel App  
SEMANTIC_STYLE_SAFE → "On time" (good)
SEMANTIC_STYLE_WARNING → "Late by 10 min" (bad)

// Fitness App
SEMANTIC_STYLE_INFO → "10,000 steps" (normal)
SEMANTIC_STYLE_WARNING → "Battery 15%" (low)
```

---

## Real-World Examples

### 1. **Health/Fitness App**
```kotlin
val heartRate = Notification.Metric(
    Notification.Metric.FixedInt(72, "bpm"),
    "Heart Rate",
    Notification.SEMANTIC_STYLE_INFO
)

val steps = Notification.Metric(
    Notification.Metric.FixedInt(10000, "steps"),
    "Today's Steps",
    Notification.SEMANTIC_STYLE_INFO
)

val calories = Notification.Metric(
    Notification.Metric.FixedInt(350, "cal"),
    "Calories Burned",
    Notification.SEMANTIC_STYLE_INFO
)

val style = Notification.MetricStyle()
    .addMetric(heartRate)
    .addMetric(steps)
    .addMetric(calories)
    .setCriticalMetric(0)  // Highlight heart rate

// Renders:
// ❤️ Heart Rate     [72 bpm]    ← Highlighted
// 👣 Steps          [10,000]
// 🔥 Calories       [350 cal]
```

### 2. **Navigation/Travel App**
```kotlin
val eta = Notification.Metric(
    Notification.Metric.FixedText("15 min"),
    "ETA",
    Notification.SEMANTIC_STYLE_SAFE  // Green = on track
)

val distance = Notification.Metric(
    Notification.Metric.FixedInt(5, "km"),
    "Distance Remaining",
    Notification.SEMANTIC_STYLE_INFO
)

val speed = Notification.Metric(
    Notification.Metric.FixedInt(45, "km/h"),
    "Current Speed",
    Notification.SEMANTIC_STYLE_INFO
)

// Renders:
// 🚗 ETA               [15 min] (green)
// 📍 Distance Remaining [5 km]
// 🏎️ Current Speed      [45 km/h]
```

### 3. **Timer/Stopwatch App**
```kotlin
val elapsed = Notification.Metric(
    Notification.Metric.FixedText("12:34"),
    "Elapsed Time",
    Notification.SEMANTIC_STYLE_INFO
)

val laps = Notification.Metric(
    Notification.Metric.FixedInt(5, "laps"),
    "Laps Completed",
    Notification.SEMANTIC_STYLE_INFO
)

val best = Notification.Metric(
    Notification.Metric.FixedText("02:15"),
    "Best Lap",
    Notification.SEMANTIC_STYLE_SAFE
)

// Renders:
// ⏱️ Elapsed Time   [12:34]
// 🔄 Laps Completed [5 laps]
// 🏆 Best Lap       [02:15] (green)
```

---

## Before vs After: User Experience

### ❌ BEFORE (Custom RemoteViews):
```
Workout
━━━━━━━━━━━━━━━━━━━━
❤️ Heart Rate: 72 bpm
👣 Steps: 10,000
🔥 Calories: 350 cal
━━━━━━━━━━━━━━━━━━━━
[Action] [Action]

Problems:
- Inconsistent styling across apps
- Custom layouts don't adapt well
- Accessibility issues common
- Hard for users to scan
```

### ✅ AFTER (MetricStyle):
```
Workout
━━━━━━━━━━━━━━━━━━━━
❤️ Heart Rate     [72 bpm]   ← Highlighted
👣 Steps          [10,000]
🔥 Calories       [350 cal]
━━━━━━━━━━━━━━━━━━━━
[Action] [Action]

Benefits:
- Consistent across apps
- Platform-styled (adaptive)
- Built-in accessibility
- Easy to scan
- Semantic colors
```

---

## The API Check Pattern

```kotlin
Button(
    enabled = AndroidApis.isAndroid17,  // ✅ CHECK 1: Enable on API 37+
    onClick = {
        // ✅ CHECK 2: Double-check in onClick
        if (!AndroidApis.isAndroid17) return@Button
        
        // ✅ CHECK 3: Notification permission (API 33+)
        if (!notifications.ensure()) return@Button
        
        // ✅ CHECK 4: Create and post notification
        // ...
    }
) { Text("Post MetricStyle notification") }
```

**Why Multiple Checks:**
1. **API 37** → MetricStyle exists
2. **POST_NOTIFICATIONS** → Required since API 33
3. **Double-check** → Defense in depth

---

## Notification Permission (API 33+)

```kotlin
// Since Android 13 (API 33), apps must request POST_NOTIFICATIONS
// This screen uses rememberNotificationGate() to handle it

val notifications = rememberNotificationGate()
// User must grant permission
// Without it, notify() is silent no-op
```

**Permission Flow:**
```
User clicks "Post MetricStyle notification"
         ↓
   Check POST_NOTIFICATIONS permission
         ↓
    ┌─────┴─────┐
    │           │
  Granted    Not Granted
    │           │
    ↓           ↓
  Post      Request Permission
  Metric       ↓
  Style     User Grants
             ↓
          Post MetricStyle
```

---

## Benefits of MetricStyle

| Benefit | Why It Matters |
|---------|---------------|
| **Consistency** | Users learn to read notifications faster |
| **Platform-Standard** | No need for custom layouts |
| **Accessibility** | Built-in TalkBack support |
| **Semantic Colors** | Users can quickly assess status |
| **Easy to Use** | Simple API, less code |
| **Adaptive** | Works on all screen sizes |

---

## The "Why" Behind This Change

**Before Android 17:**
- Health, fitness, and travel apps all had **custom notification layouts**
- **Inconsistent UX** across apps
- **Hard to scan** because each app looked different
- **Accessibility** was often broken
- **Semantic meaning** was lost (colors varied per app)

**After Android 17:**
- **Unified template** for metrics
- **Consistent UX** across all apps
- **Easy to scan** with clear value/label pairs
- **Built-in accessibility** (TalkBack reads metrics)
- **Semantic styling** shows status at a glance

---

## Summary

API 37's `Notification.MetricStyle` is a **major UX improvement** for data-driven notifications:

1. **Platform template** → Consistent across apps
2. **Structured metrics** → Value + Label + Style
3. **Semantic colors** → Status at a glance
4. **Critical highlighting** → Important metrics stand out
5. **Accessibility built-in** → Inclusive by default
6. **Easy to implement** → Simple builder API

**The big picture:** Android is standardizing common notification patterns. Just as `BigTextStyle` and `InboxStyle` standardized text notifications, `MetricStyle` standardizes data/metric notifications. This leads to:
- **Better UX** for users
- **Less code** for developers  
- **Better accessibility** for all users
- **Platform consistency** across all apps

**Developer Action Items:**
- Replace custom metric RemoteViews with `MetricStyle`
- Use semantic styles for status indication
- Highlight critical metrics with `setCriticalMetric()`
- Ensure POST_NOTIFICATIONS permission is handled
- Test on API 37+ devices