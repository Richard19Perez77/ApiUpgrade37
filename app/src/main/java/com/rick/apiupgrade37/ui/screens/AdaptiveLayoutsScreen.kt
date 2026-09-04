package com.rick.apiupgrade37.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            intro = "Targeting API 37, orientation/resizability/aspect-ratio locks are ignored " +
                "on large screens (sw > 600dp), including a phone in desktop mode. Layouts must " +
                "reflow. BoxWithConstraints (below) is the Compose replacement for layout-w600dp XML.\n\n" +
                "Production: NavigationSuiteScaffold from material3-adaptive-navigation-suite " +
                "swaps a bottom bar for a navigation rail automatically."
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val twoPane = maxWidth >= 600.dp
                Text("maxWidth = $maxWidth  twoPane=$twoPane  (also updates in App Bubbles and interactive desktop PiP)")
                if (!twoPane) {
                    Column {
                        Pane("List pane")
                        Pane("Detail pane (stacked under list on compact)")
                    }
                } else {
                    Row {
                        Column(Modifier.weight(1f)) { Pane("List pane") }
                        Column(Modifier.weight(1f)) { Pane("Detail pane") }
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
            body(
                PaddingValues(
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
    Card(Modifier.padding(4.dp).fillMaxWidth()) {
        Text(label, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
    }
}
