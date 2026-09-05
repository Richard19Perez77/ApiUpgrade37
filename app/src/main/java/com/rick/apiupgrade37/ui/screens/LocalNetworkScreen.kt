package com.rick.apiupgrade37.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 *
 *  ACCESS_LOCAL_NETWORK
 *      API 37 Permission
 *      Apps must explicitly request permission to access local network resources
 *      mDNS, Chromecast, IoT Devices
 *
 *  Changes:
 *      user explicitly grants local network access
 *      apps can't silently scan your network
 *      better privacy control
 *      user knows which apps access local devices
 *
 *  Wi-Fi Status Display
 *      clarifies that Wi-Fi is separate from local network permissions
 *      Wi-Fi enabled != local network access granted
 *      helps developers understand the distinction
 *
 *  Use Cases:
 *      Smart Home App
 *          connecting to a smart plug on local network
 *      Chromecast App
 *          will assist in discovering Chromecast device, use mDNS
 *      Printer App
 *          printing to network printer, connects to printer
 *
 *  Permissions:
 *      ACCESS_LOCAL_NETWORK
 *          is in the NEARBY_DEVICES permission group, which includes:
 *
 *          ACCESS_LOCAL_NETWORK
 *          ACCESS_WIFI_STATE
 *          CHANGE_WIFI_STATE
 *          BLUETOOTH
 *          NEARBY_WIFI_DEVICES
 *
 *     all permission related to lcoal connectivity
 *     user sees them as "nearby devices" in settings
 *     consistent grouping for user understanding
 *
 *  Improvements:
 *      explicity permisions required
 *      users know which apps access local network
 *      apps can't silently screen
 *      privacy is protected
 *      security is improved
 *
 *  Why?
 *      growing concerns about network privacy are addressed
 *      no more INTERNET permission as silent discovery
 *      user's have more control over IoT devices
 *
 * - API 37: Request ACCESS_LOCAL_NETWORK (NEARBY_DEVICES group).
 *      Until granted, unprivileged LAN sockets are blocked unless you use a system device picker.
 *
 * - Pre-37: INTERNET was enough to reach 192.168.x.x, mDNS printers, Chromecast, IoT.
 *
 * - Need — if this app talks to the LAN.
 *      Skip it if you never leave the public internet.
 *
 */
@Composable
fun LocalNetworkScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember {
        mutableStateOf(grantLabel(context))
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        status = if (granted) "ACCESS_LOCAL_NETWORK granted" else "Denied — LAN sockets stay blocked"
    }

    FeatureScaffold("Local network", onBack) { padding ->
        FeatureBody(
            padding,/**/
            "Targeting API 37, unprivileged apps cannot open sockets to local-network hosts " +
                "until ACCESS_LOCAL_NETWORK is granted (or you use a system device picker).\n\n" +
                "Pre-37: INTERNET was enough to hit 192.168.x.x / mDNS printers / smart plugs.\n" +
                "The permission sits in the NEARBY_DEVICES group.\n\n" +
                "Prefer pickers for one-shot casting; request this permission only for " +
                "ongoing LAN protocols (SSDP, raw TCP to a NAS, etc.)."
        ) {
            Text(status)
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    // `enabled` is not a gate lint understands, so the guard is repeated here.
                    if (!AndroidApis.isAndroid17) return@Button
                    launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
            ) { Text("Request ACCESS_LOCAL_NETWORK") }
            val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
            Text("Wi-Fi enabled=${wifi?.isWifiEnabled} (not a substitute for the new permission)")
        }
    }
}

private fun grantLabel(context: android.content.Context): String {
    if (!AndroidApis.isAndroid17) return "Device < 37: local-network block is not targetSdk-gated here"
    val granted = if (AndroidApis.isAndroid17) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Pre-37: INTERNET was enough for LAN sockets.
        true
    }
    return if (granted) "Already granted" else "Not granted — LAN is blocked for this app"
}
