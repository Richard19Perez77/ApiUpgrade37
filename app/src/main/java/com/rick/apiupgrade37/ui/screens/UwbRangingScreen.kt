package com.rick.apiupgrade37.ui.screens

import android.ranging.RangingManager
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
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 *  Ultra-Wideband UWB
 *
 *  DL-TDoA Downlink Time Difference of Arrival
 *      Queries device's UWB hardware capabilities and shows whether DL-TDoA positioning is supported.
 *      Used in IOT like digital car keys and smart home devices.
 *      Improves accuracy, coverage, battery, scale, and practical.
 *
 * - API 37: RangingManager capabilities expose uwbCapabilities.isDlTdoaSupported
 *   (downlink TDoA vs multiple anchors). Starting a session uses DlTdoaRangingParams
 *   (commented here — needs FiRa OOB from real anchors).
 *
 * - Pre-37: RangingManager itself is API 36 without this DL-TDoA path. Older UWB
 *   apps used vendor/FiRa APIs outside this manager.
 *
 * - Nicety — only if you range. This screen only reads capabilities.
 *
 */
@Composable
fun UwbRangingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Not queried") }

    DisposableEffect(Unit) {
        if (!AndroidApis.isAndroid17) return@DisposableEffect onDispose { }
        val rm = context.getSystemService(RangingManager::class.java)
        // Deliver on the main executor: the callback writes Compose state.
        val executor = ContextCompat.getMainExecutor(context)
        val cb = RangingManager.RangingCapabilitiesCallback { caps ->
            val uwb = caps.uwbCapabilities
            status = "uwb=${uwb != null} dlTdoa=${uwb?.isDlTdoaSupported} " +
                "tech=${caps.technologyAvailability}"
        }
        rm.registerCapabilitiesCallback(executor, cb)
        onDispose { rm.unregisterCapabilitiesCallback(cb) }
    }

    FeatureScaffold("UWB DL-TDoA", onBack) { padding ->
        FeatureBody(
            padding,
            "Downlink TDoA lets a device locate itself against multiple UWB anchors by " +
                "comparing arrival times.\n\n" +
                "This screen only reads capabilities, which needs no permission. Running an " +
                "actual ranging session needs the UWB_RANGING runtime permission (declared in " +
                "the manifest but deliberately not requested here), and ACCESS_FINE_LOCATION " +
                "if you derive position from it — that one is NOT declared in this manifest, " +
                "since the lab never ranges.\n\n" +
                "Starting a session also needs FiRa OOB bytes from your anchors — see the " +
                "commented builder in source."
        ) {
            Text(status)
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    // Starting a live session with empty OOB config is not useful on a phone
                    // without anchors. The production sequence is:
                    //
                    // val session = rangingManager.createRangingSession(executor, callback)
                    // val params = DlTdoaRangingParams.createFromFiraConfigPacket(oob, byteArrayOf(0))
                    // val device = RawRangingDevice.Builder()
                    //     .setRangingDevice(RangingDevice.Builder().build())
                    //     .setDlTdoaRangingParams(params)
                    //     .build()
                    // val config = RawDtTagRangingConfig.Builder(device).build()
                    // val pref = RangingPreference.Builder(
                    //     RangingPreference.DEVICE_ROLE_DT_TAG, config
                    // ).setSessionConfig(SessionConfig.Builder().build()).build()
                    // session.start(pref)
                    status = if (status.startsWith("uwb=")) status else "Waiting for capability callback"
                }
            ) { Text("Refresh hint") }
        }
    }
}
