package com.rick.apiupgrade37.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rick.apiupgrade37.ui.screens.AccessibilityImeScreen
import com.rick.apiupgrade37.ui.screens.AdaptiveLayoutsScreen
import com.rick.apiupgrade37.ui.screens.AdvancedProtectionScreen
import com.rick.apiupgrade37.ui.screens.AlarmListenerScreen
import com.rick.apiupgrade37.ui.screens.AppFunctionsScreen
import com.rick.apiupgrade37.ui.screens.BehaviorChangesScreen
import com.rick.apiupgrade37.ui.screens.CameraMediaScreen
import com.rick.apiupgrade37.ui.screens.ContactsPickerScreen
import com.rick.apiupgrade37.ui.screens.EyeDropperScreen
import com.rick.apiupgrade37.ui.screens.HearingAidScreen
import com.rick.apiupgrade37.ui.screens.HomeScreen
import com.rick.apiupgrade37.ui.screens.JobSchedulerScreen
import com.rick.apiupgrade37.ui.screens.LiveUpdateScreen
import com.rick.apiupgrade37.ui.screens.LocalNetworkScreen
import com.rick.apiupgrade37.ui.screens.MemoryLimitsScreen
import com.rick.apiupgrade37.ui.screens.MetricStyleScreen
import com.rick.apiupgrade37.ui.screens.NetworkSecurityScreen
import com.rick.apiupgrade37.ui.screens.PhotoPickerScreen
import com.rick.apiupgrade37.ui.screens.ProfilingScreen
import com.rick.apiupgrade37.ui.screens.UwbRangingScreen

private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_ADAPTIVE = "adaptive"
private const val ROUTE_BEHAVIORS = "behaviors"

/**
 * Simple in-app router (no Navigation Compose dependency).
 *
 * On API 37 large screens the system ignores orientation locks, so this chrome
 * is built with FilterChips that wrap instead of a portrait-only BottomBar.
 * Production apps should use NavigationSuiteScaffold from
 * material3-adaptive-navigation-suite (bottom bar ↔ rail).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Api37App() {
    var route by rememberSaveable { mutableStateOf(ROUTE_CATALOG) }
    val topLevel = route == ROUTE_CATALOG || route == ROUTE_ADAPTIVE || route == ROUTE_BEHAVIORS
    val back: () -> Unit = { route = ROUTE_CATALOG }

    BackHandler(enabled = !topLevel) { route = ROUTE_CATALOG }

    Scaffold(
        bottomBar = {
            if (topLevel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = route == ROUTE_CATALOG,
                        onClick = { route = ROUTE_CATALOG },
                        label = { Text("Catalog") }
                    )
                    FilterChip(
                        selected = route == ROUTE_ADAPTIVE,
                        onClick = { route = ROUTE_ADAPTIVE },
                        label = { Text("Adaptive") }
                    )
                    FilterChip(
                        selected = route == ROUTE_BEHAVIORS,
                        onClick = { route = ROUTE_BEHAVIORS },
                        label = { Text("Behaviors") }
                    )
                }
            }
        }
    ) { padding ->
        when (route) {
            ROUTE_CATALOG -> HomeScreen(
                listPadding = padding,
                onOpen = { route = it }
            )
            ROUTE_ADAPTIVE -> AdaptiveLayoutsScreen(onBack = null, listPadding = padding)
            ROUTE_BEHAVIORS -> BehaviorChangesScreen(onBack = null, listPadding = padding)
            "contacts" -> ContactsPickerScreen(back)
            "eyedropper" -> EyeDropperScreen(back)
            "localnet" -> LocalNetworkScreen(back)
            "photos" -> PhotoPickerScreen(back)
            "aapm" -> AdvancedProtectionScreen(back)
            "ech" -> NetworkSecurityScreen(back)
            "profiling" -> ProfilingScreen(back)
            "jobs" -> JobSchedulerScreen(back)
            "alarms" -> AlarmListenerScreen(back)
            "memory" -> MemoryLimitsScreen(back)
            "live" -> LiveUpdateScreen(back)
            "metrics" -> MetricStyleScreen(back)
            "camera" -> CameraMediaScreen(back)
            "hearing" -> HearingAidScreen(back)
            "uwb" -> UwbRangingScreen(back)
            "appfn" -> AppFunctionsScreen(back)
            "ime" -> AccessibilityImeScreen(back)
            "adaptive_detail" -> AdaptiveLayoutsScreen(onBack = back)
            "behaviors_detail" -> BehaviorChangesScreen(onBack = back)
        }
    }
}
