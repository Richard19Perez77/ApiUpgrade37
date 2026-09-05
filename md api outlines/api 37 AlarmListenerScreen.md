```kotlin
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
            ""
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
                        status = "SecurityException: ${e.message}"
                    }
                }
            ) { Text("Schedule 8s allow-while-idle alarm") }
        }
    }
}
```

---

# Analysis of `AlarmListenerScreen.kt`

## What This Class Does

`AlarmListenerScreen` is a **Compose UI screen** that demonstrates Android API 37's new **`setExactAndAllowWhileIdle()` with an `OnAlarmListener` callback**. It shows how apps can now schedule exact alarms that fire **in-process** without needing a `BroadcastReceiver` or `PendingIntent`.

---

## What are Exact Alarms?

**Exact alarms** are Android's mechanism for scheduling precise operations at specific times. They're used for:
- **Timely notifications** (reminders, alarms)
- **Sync operations** (periodic data updates)
- **Keepalive pings** (maintaining connections)
- **Scheduled tasks** (backup, cleanup)

**The Challenge:**
- Android restricts exact alarms to save battery
- Apps need `SCHEDULE_EXACT_ALARM` permission (special access)
- Users must grant this permission manually

---

## The API 37 Improvement: OnAlarmListener

| Aspect | Before (API ≤36) | After (API 37) | Improvement |
|--------|-----------------|----------------|-------------|
| **Alarm Delivery** | `PendingIntent` → `BroadcastReceiver` | Direct `OnAlarmListener` callback | **Efficiency** 🚀 |
| **Process Wake** | Broadcast receiver wakes full process | In-process callback only | **Battery** 🔋 |
| **Manifest Entry** | Need `<receiver>` declaration | No manifest entry needed | **Cleaner** |
| **Intent Filters** | Need to handle intents | No intent handling | **Simpler** |
| **State Management** | Receiver is stateless | Direct access to app state | **Easier** |

---

## Before vs After: Code Comparison

### ❌ BEFORE (API 36 and lower):
```kotlin
// Step 1: Register BroadcastReceiver in AndroidManifest.xml
<receiver android:name=".AlarmReceiver" />

// Step 2: Create receiver class
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Do work - but this runs in a separate process wake
        Toast.makeText(context, "Alarm fired!", Toast.LENGTH_SHORT).show()
    }
}

// Step 3: Schedule alarm
val intent = Intent(context, AlarmReceiver::class.java)
val pi = PendingIntent.getBroadcast(
    context,
    0,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.ELAPSED_REALTIME_WAKEUP,
    trigger,
    pi  // Must use PendingIntent
)

// Problems:
// 1. Need manifest receiver
// 2. Wakes entire process
// 3. Separate broadcast handling
// 4. Intent confusion
```

### ✅ AFTER (API 37):
```kotlin
// Step 1: No manifest entry needed!

// Step 2: Schedule with direct callback
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.ELAPSED_REALTIME_WAKEUP,
    trigger,
    "api37-demo",  // Optional tag
    ContextCompat.getMainExecutor(context)  // Where to run
) {
    // In-process callback!
    Toast.makeText(context, "OnAlarmListener fired", Toast.LENGTH_SHORT).show()
    // Direct access to app state, no intent parsing
}

// Benefits:
// 1. No manifest receiver
// 2. In-process only
// 3. Direct callback
// 4. Simpler code
```

---

## The Permission Challenge (API 31+)

### Why Permission is Required

Starting from **Android 12 (API 31)**:
- Exact alarms require `SCHEDULE_EXACT_ALARM` permission
- It's a **special app access** (like overlay permission)
- Users must grant it manually
- Not granted by default to most apps

### The Permission Flow

```kotlin
// Step 1: Check if you have permission
val am = context.getSystemService(AlarmManager::class.java)
if (!am.canScheduleExactAlarms()) {
    // Step 2: Request permission
    context.startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData("package:${context.packageName}".toUri())
    )
    // User will be taken to settings
    return
}

// Step 3: Schedule the alarm
try {
    am.setExactAndAllowWhileIdle(/* ... */)
} catch (e: SecurityException) {
    // Permission may have been revoked between check and call
    // Handle gracefully
}
```

---

## The Lifecycle Observer Pattern

```kotlin
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
```

### Why This is Needed

**The Problem:**
1. User clicks "Schedule" button
2. App checks permission → no permission
3. App opens settings (user goes to settings)
4. User grants permission
5. User returns to app
6. App shows old "no permission" status ❌

**The Solution:**
- Re-check permission on **`ON_RESUME`** (when user returns)
- Update status automatically
- No stale UI state

**The Flow:**
```
User clicks → No permission → Open settings
                                    ↓
                           User grants permission
                                    ↓
                     User returns to app → ON_RESUME
                                    ↓
                         Re-check permission ✅
                                    ↓
                         Update status to "Granted"
```

---

## Code Analysis: API Checks

### 1. **Platform Version Check**
```kotlin
if (AndroidApis.isAndroid17) {
    // Use new API 37 OnAlarmListener
    am.setExactAndAllowWhileIdle(/* ... */, executor, listener)
} else {
    // Use old PendingIntent method
    am.setExactAndAllowWhileIdle(/* ... */, pi)
}
```

**Why:** `OnAlarmListener` parameter only exists on API 37+

---

### 2. **Permission Check (API 31+)**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
    !am.canScheduleExactAlarms()
) {
    // Request permission
}
```

**Why:** `canScheduleExactAlarms()` only exists on API 31+

---

### 3. **Try-Catch for Revoked Permission**
```kotlin
try {
    am.setExactAndAllowWhileIdle(/* ... */)
} catch (e: SecurityException) {
    status = "SecurityException: ${e.message}"
}
```

**Why:** Permission can be revoked between check and call

---

## The Complete Permission Journey

```kotlin
fun scheduleAlarm() {
    val am = context.getSystemService(AlarmManager::class.java)
    val trigger = SystemClock.elapsedRealtime() + 8000 // 8 seconds

    // ✅ CHECK 1: API Level (for exact alarm permission)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // ✅ CHECK 2: Permission granted?
        if (!am.canScheduleExactAlarms()) {
            // ❌ No permission → Open settings
            status = "Exact alarms not permitted — opening system settings"
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData("package:${context.packageName}".toUri())
            )
            return
        }
    }

    try {
        // ✅ CHECK 3: Use API 37 feature if available
        if (AndroidApis.isAndroid17) {
            // 🎉 NEW: OnAlarmListener
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
            // 🔄 OLD: PendingIntent (still works)
            val pi = PendingIntent.getBroadcast(/* ... */)
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                trigger,
                pi
            )
            status = "Scheduled via PendingIntent (pre-37 form)"
        }
    } catch (e: SecurityException) {
        // ✅ CHECK 4: Permission revoked unexpectedly
        status = "SecurityException: ${e.message}"
    }
}
```

---

## Real-World Use Cases

### 1. **Socket Keepalive**
```kotlin
// API 37: Keep connection alive with minimal overhead
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.ELAPSED_REALTIME_WAKEUP,
    SystemClock.elapsedRealtime() + 300_000, // 5 minutes
    "keepalive"
) {
    socket.sendKeepAlive()  // Direct in-process call
}
```

### 2. **Sync Tick**
```kotlin
// API 37: Periodic sync without receiver overhead
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.ELAPSED_REALTIME_WAKEUP,
    SystemClock.elapsedRealtime() + 3600_000, // 1 hour
    "sync"
) {
    syncData()  // No BroadcastReceiver needed
}
```

### 3. **Scheduled Notification**
```kotlin
// API 37: Timely notifications
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.ELAPSED_REALTIME_WAKEUP,
    SystemClock.elapsedRealtime() + 120_000, // 2 minutes
    "notification"
) {
    showNotification("Time's up!")
}
```

---

## Key Improvements Summary

| Feature | Pre-API 37 | API 37 | Gain |
|---------|-----------|--------|------|
| **Alarm Delivery** | BroadcastReceiver | OnAlarmListener | 🚀 Direct callback |
| **Manifest Entry** | Required | Not needed | 🧹 Cleaner code |
| **Process Wake** | Full process | In-process only | 🔋 Better battery |
| **Intent Handling** | Required | Not needed | 🎯 Simpler logic |
| **State Access** | Via extras | Direct access | 💪 Easier code |

---

## The "Why" Behind This Change

**Before Android 17:**
- Every exact alarm required a `PendingIntent`
- Had to declare a `BroadcastReceiver` in manifest
- Broadcast wakes entire process (expensive)
- Receiver can't easily access app state
- More boilerplate code

**After Android 17:**
- Direct `OnAlarmListener` callback
- No manifest receiver needed
- In-process only (no extra wake)
- Direct access to app state
- Cleaner code

---

## Permission Flow Diagram

```
User wants to schedule exact alarm
         ↓
   Check permission
         ↓
    ┌────┴────┐
    │         │
   Granted   Not Granted
    │         │
    ↓         ↓
Schedule   Open Settings
alarm         ↓
    │    User Grants
    │    Permission
    │         │
    └────┬────┘
         ↓
   Schedule alarm
         ↓
  (Permission can be
   revoked anytime)
         ↓
   Exception caught ✅
```

---

## Summary

API 37's `setExactAndAllowWhileIdle()` with `OnAlarmListener` is a **major improvement** for scheduled tasks:

1. **Cleaner API** → No `PendingIntent` or `BroadcastReceiver`
2. **More Efficient** → In-process callback only
3. **Better Battery** → No full process wake
4. **Easier Code** → Direct access to app state

**The security aspect:** Exact alarms still require special permission (`SCHEDULE_EXACT_ALARM`), which users must grant manually. This screen handles the full flow:

✅ Check permission
✅ Request permission (open settings)
✅ Re-check on resume
✅ Try-catch for revoked permission
✅ Fallback for older devices

**The big picture:** API 37 makes exact alarms more developer-friendly while maintaining the battery-saving restrictions introduced in recent Android versions.
