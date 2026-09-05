```kotlin
package com.rick.apiupgrade37.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun AdaptiveLayoutsScreen(
    onBack: (() -> Unit)?,
    listPadding: PaddingValues = PaddingValues()
) {
    val body = @Composable { padding: PaddingValues ->
        FeatureBody(
            padding = padding,
            intro = ""
        ) {

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val width = maxWidth
                val twoPane = width >= 600.dp
                Column(Modifier.fillMaxWidth()) {
                    Text("maxWidth = $width  twoPane=$twoPane  (also updates in App Bubbles and interactive desktop PiP)")
                    if (!twoPane) {
                        Pane("List pane")
                        Pane("Detail pane (stacked under list on compact)")
                    } else {
                        Row {
                            Column(Modifier.weight(1f)) { Pane("List pane") }
                            Column(Modifier.weight(1f)) { Pane("Detail pane") }
                        }
                    }
                }
            }
            Text(
                "System windowing that is new or expanded in Android 17:\n" +
                        "• App Bubbles — user long-presses any app icon; your UI must survive tiny widths.\n" +
                        "• Bubble Bar — tablet/foldable taskbar dock for those bubbles.\n" +
                        "• Desktop interactive PiP — unlike the old read-only PiP, input still works.\n\n" +
                        "Pre-37 (commented patterns you should stop relying on):\n" +
                        "// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)\n" +
                        "// manifest: android:screenOrientation=\"portrait\"\n" +
                        "// manifest: android:resizeableActivity=\"false\"",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    if (onBack == null) {
        Scaffold { padding ->

            val direction = LocalLayoutDirection.current
            body(
                PaddingValues(
                    start = padding.calculateStartPadding(direction),
                    end = padding.calculateEndPadding(direction),
                    top = padding.calculateTopPadding(),
                    bottom = listPadding.calculateBottomPadding()
                )
            )
        }
    } else {
        FeatureScaffold("Adaptive layouts", onBack, body)
    }
}

@Composable
private fun Pane(label: String) {
    Card(Modifier
        .padding(4.dp)
        .fillMaxWidth()) {
        Text(
            label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

# Analysis of `AdaptiveLayoutsScreen.kt`

## What This Class Does

`AdaptiveLayoutsScreen` is a **Compose UI screen** that demonstrates Android API 37's **adaptive layout requirements**. It shows how apps must now handle **dynamic window sizes** because API 37 removes orientation/resizability locks on large screens (sw > 600dp).

---

## What is the API 37 Change?

**The Problem Before API 37:**
- Apps could **lock** their orientation (portrait/landscape)
- Apps could set **fixed aspect ratios**
- Apps could be **non-resizable** (`resizeableActivity="false"`)
- Apps could assume **fixed window dimensions**

**The API 37 Change:**
- On **large screens** (sw > 600dp), all these locks are **IGNORED**
- Windows can be **resized freely**
- Orientation can **change dynamically**
- Apps must **adapt** to any window size

---

## The API 37 Improvements

| Aspect | Before (API ≤36) | After (API 37) | Improvement |
|--------|-----------------|----------------|-------------|
| **Orientation Lock** | Enforced | Ignored on large screens | **Flexibility** 📱 |
| **Resizability** | Could be disabled | Forced on large screens | **Freedom** |
| **Aspect Ratio** | Could be fixed | Ignored | **Adaptability** |
| **Configuration Changes** | Activity recreated | `onConfigurationChanged()` | **Performance** 🚀 |
| **Window Modes** | Limited | App Bubbles, Desktop PiP | **Multi-tasking** |

---

## What's Being Ignored in API 37?

### 1. **Screen Orientation Locks**
```xml
<!-- BEFORE: This worked on all devices -->
<activity
    android:name=".MyActivity"
    android:screenOrientation="portrait" />

<!-- AFTER (API 37 on large screens): 
     This is IGNORED! Activity can rotate. -->
```

### 2. **Requested Orientation**
```kotlin
// BEFORE: This forced portrait mode
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

// AFTER (API 37 on large screens):
// This is IGNORED! User can rotate freely.
```

### 3. **Resizability**
```xml
<!-- BEFORE: This prevented resizing -->
<activity
    android:name=".MyActivity"
    android:resizeableActivity="false" />

<!-- AFTER (API 37 on large screens):
     This is IGNORED! App is always resizable. -->
```

### 4. **Aspect Ratio**
```xml
<!-- BEFORE: This forced a specific aspect ratio -->
<activity
    android:name=".MyActivity"
    android:minAspectRatio="1.5"
    android:maxAspectRatio="2.0" />

<!-- AFTER (API 37 on large screens):
     These are IGNORED! Window can be any shape. -->
```

---

## New Window Modes in Android 17

### 1. **App Bubbles**
```
User long-presses any app icon
         ↓
    Bubble appears (floating window)
         ↓
    App must survive tiny widths
         ↓
    Layouts must reflow!
```

**Example:**
```
Compact (phone):      Bubble (tiny):
┌─────────────┐      ┌──────┐
│  List Pane  │      │  App  │
│  Detail Pane│      │ Icon  │
└─────────────┘      └──────┘
```

### 2. **Bubble Bar**
```
Tablet/Foldable taskbar
    ↓
Bubble Bar at bottom
    ↓
Quick access to bubbles
    ↓
App must handle docked state
```

### 3. **Desktop Interactive PiP**
```
Picture-in-Picture
    ↓
Interactive (not read-only!)
    ↓
User can interact with app
    ↓
App must work in small window
```

---

## Code Analysis

### 1. **BoxWithConstraints for Adaptive Layout**

```kotlin
BoxWithConstraints(Modifier.fillMaxWidth()) {
    val width = maxWidth
    val twoPane = width >= 600.dp
    
    Column(Modifier.fillMaxWidth()) {
        Text("maxWidth = $width  twoPane=$twoPane")
        
        if (!twoPane) {
            Pane("List pane")
            Pane("Detail pane (stacked under list on compact)")
        } else {
            Row {
                Column(Modifier.weight(1f)) { Pane("List pane") }
                Column(Modifier.weight(1f)) { Pane("Detail pane") }
            }
        }
    }
}
```

**What it does:**
- **Measures available width** at composition time
- **Switches layout** based on width ≥ 600dp
- **Two-pane layout** on large screens
- **Single-column layout** on small screens

**The Pattern:**
```
Compact (< 600dp):      Large (≥ 600dp):
┌─────────────┐         ┌──────────┬──────────┐
│  List Pane  │         │ List Pane│Detail Pane│
│  Detail Pane│         └──────────┴──────────┘
└─────────────┘
```

---

### 2. **Configuration Change Handling**

```kotlin
/**
 * API 37: ... Keyboard/touch/colorMode config changes go 
 * to onConfigurationChanged instead of recreating the Activity.
 */
```

**Before API 37:**
```kotlin
class MyActivity : AppCompatActivity() {
    // Orientation change → Activity DESTROYED and RECREATED
    // All state lost! 😱
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Heavy setup every time!
    }
}
```

**After API 37:**
```kotlin
class MyActivity : AppCompatActivity() {
    // Orientation change → onConfigurationChanged() only
    // Activity survives! 🎉
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Just update layout, keep state
        updateLayoutForNewSize()
    }
}
```

---

### 3. **The Commented "Bad" Patterns**

```kotlin
// Pre-37 (commented patterns you should stop relying on):
// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
// manifest: android:screenOrientation="portrait"
// manifest: android:resizeableActivity="false"
```

**These are now invalid on large screens!**

---

## Adaptive Layout Strategies

### Strategy 1: BoxWithConstraints (Compose)
```kotlin
BoxWithConstraints {
    if (maxWidth < 600.dp) {
        // Compact layout
        Column { /* stacked content */ }
    } else {
        // Large layout
        Row { /* side-by-side content */ }
    }
}
```

### Strategy 2: NavigationSuiteScaffold (Material3)
```kotlin
// The recommended way for navigation
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold

NavigationSuiteScaffold(
    navigationSuiteItems = { /* nav items */ }
) {
    // Automatically swaps between:
    // - Bottom bar (compact)
    // - Navigation rail (medium)
    // - Navigation drawer (large)
}
```

### Strategy 3: WindowSizeClass (Material3)
```kotlin
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo

@Composable
fun AdaptiveContent() {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val sizeClass = adaptiveInfo.windowSizeClass
    
    when {
        sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact -> {
            // Phone layout
        }
        sizeClass.widthSizeClass == WindowWidthSizeClass.Medium -> {
            // Foldable/tablet layout
        }
        sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded -> {
            // Desktop/large tablet layout
        }
    }
}
```

---

## Before vs After: Layout Behavior

### ❌ BEFORE (API 36):
```kotlin
// Locked portrait
class PortraitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Activity always portrait
        // On tablet: shows huge blank spaces on sides
        // User can't use in landscape
    }
}
```

### ✅ AFTER (API 37):
```kotlin
// Adaptive layout
class AdaptiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // No orientation lock!
        // Layout adapts to any size
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Reflow layout for new size
        updateUI()
    }
}
```

---

## Real-World Impact

### Example 1: Photo Gallery App
```
Compact (phone):          Large (tablet/desktop):
┌─────────────┐         ┌──────────┬──────────┐
│  Grid View  │         │ Grid View│ Detail   │
│  (thumbnails)│         │(thumbnails)│  Pane   │
└─────────────┘         └──────────┴──────────┘
```

### Example 2: Email App
```
Compact (phone):          Large (tablet/desktop):
┌─────────────┐         ┌──────────┬──────────┐
│  Inbox List │         │ Inbox    │ Email    │
└─────────────┘         │ List     │ Content  │
                        └──────────┴──────────┘
```

### Example 3: Settings App
```
Compact (phone):          Large (tablet/desktop):
┌─────────────┐         ┌──────────┬──────────┐
│  Categories │         │Categories│  Detail  │
└─────────────┘         └──────────┴──────────┘
```

---

## App Bubbles: The New Challenge

### What are App Bubbles?
- User long-presses **any app icon**
- App opens in a **small floating bubble**
- **Tiny width** (maybe 200-300dp)
- Must work in **extreme compact mode**

### Example:
```kotlin
// App must handle ultra-compact layout
if (maxWidth < 300.dp) {
    // Hide non-essential UI
    // Show only core functionality
    // Use small fonts and icons
} else if (maxWidth < 600.dp) {
    // Normal compact layout
} else {
    // Large layout
}
```

---

## Desktop Interactive PiP

### What is Interactive PiP?
- Picture-in-Picture mode
- **User can interact** (not read-only!)
- Small window, but with touch/click support
- Like a desktop app window

### Example:
```kotlin
// App must handle PiP mode
if (isInPictureInPictureMode) {
    // Show simplified controls
    // Enable interaction (new in API 37!)
    // Maintain functionality
}
```

---

## Key Improvements Summary

| Improvement | Why It Matters | Who Benefits |
|-------------|---------------|--------------|
| **No Orientation Locks** | Users can use apps in any orientation | All Users |
| **Dynamic Resizing** | Multi-window multitasking | All Users |
| **App Bubbles** | Quick access to apps | Power Users |
| **Interactive PiP** | Picture-in-picture with interaction | All Users |
| **Better Performance** | No activity recreation | Developers/Users |

---

## The "Why" Behind This Change

**Before Android 17:**
- Apps assumed **fixed window sizes**
- Locked orientations **frustrated users** on tablets
- Fixed aspect ratios **wasted screen space**
- Activity recreation **wasted resources**
- No app bubbles or interactive PiP

**After Android 17:**
- Apps must be **truly adaptive**
- Users get **full control** over window size
- **Better multitasking** experience
- **Smoother** configuration changes
- **New use cases** (bubbles, PiP)

---

## Migration Checklist

- [ ] **Remove orientation locks** (manifest and code)
- [ ] **Remove resizeableActivity="false"**
- [ ] **Remove aspect ratio constraints**
- [ ] **Use BoxWithConstraints** or WindowSizeClass
- [ ] **Test on all screen sizes** (phone, tablet, desktop)
- [ ] **Test in split-screen mode**
- [ ] **Test in App Bubbles**
- [ ] **Test in interactive PiP**
- [ ] **Handle configuration changes** gracefully
- [ ] **Use NavigationSuiteScaffold** for navigation

---

## Summary

API 37's adaptive layout changes represent a **major shift** in Android app design:

1. **No more orientation locks** on large screens
2. **Must be resizable** (no `resizeableActivity="false"`)
3. **Aspect ratios ignored** on large screens
4. **Configuration changes don't recreate** the Activity
5. **New window modes** (App Bubbles, interactive PiP)

**The big picture:** Android is moving toward a **desktop-class experience** where apps are fluid windows that adapt to any size. API 37 forces this change, making Android more flexible and user-friendly on tablets, foldables, and desktops.

**The key takeaway:** Your app must now be **truly adaptive** - not just portrait/landscape, but any size, any shape, any window mode.