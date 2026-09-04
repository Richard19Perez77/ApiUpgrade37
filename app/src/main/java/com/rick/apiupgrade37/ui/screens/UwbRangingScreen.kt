package com.rick.apiupgrade37.ui.screens

import android.os.Build
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
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import java.util.concurrent.Executors

@Composable
fun UwbRangingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Not queried") }

    DisposableEffect(Unit) {
        if (!AndroidApis.isAndroid17) return@DisposableEffect onDispose { }
        val rm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            context.getSystemService(RangingManager::class.java)
        } else {
            // Pre-36: UWB via androidx.core.uwb / OEM SDKs. DL-TDoA is API 37.
            null
        }
        val executor = Executors.newSingleThreadExecutor()
        val cb = RangingManager.RangingCapabilitiesCallback { caps ->
            val uwb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                caps.uwbCapabilities
            } else {
                TODO("VERSION.SDK_INT < BAKLAVA")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                status = "uwb=${uwb != null} dlTdoa=${uwb?.isDlTdoaSupported} " +
                    "tech=${caps.technologyAvailability}"
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            rm?.registerCapabilitiesCallback(executor, cb)
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                rm?.unregisterCapabilitiesCallback(cb)
            }
            executor.shutdown()
        }
    }

    FeatureScaffold("UWB DL-TDoA", onBack) { padding ->
        FeatureBody(
            padding,
            "Downlink TDoA lets a device locate itself against multiple UWB anchors by " +
                "comparing arrival times. Requires FINE location (and BACKGROUND location if " +
                "you range while not visible) plus the UWB_RANGING permission.\n\n" +
                "This screen only queries capabilities. Starting a session needs FiRa OOB " +
                "bytes from your anchors — see the commented builder in source."
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
