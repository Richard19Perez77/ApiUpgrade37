package com.rick.apiupgrade37.ui.screens

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 *
 * - Pre-37: TYPE_HEARING_AID (classic, API 28) and TYPE_BLE_HEADSET lumped LE aids with generic LE headsets.
 *      USAGE_ASSISTANT existed from API 26 but shared media volume.
 *
 * - Nicety — unless you are a hearing or assistant app, then routing/volume is a need.
 *      Do not enter MODE_ASSISTANT_CONVERSATION from a normal app.
 *
 */
@Composable
fun HearingAidScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val am = context.getSystemService(AudioManager::class.java)
    val devices = remember {
        am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).joinToString("\n") { dev ->
            val kind = when (dev.type) {
                AudioDeviceInfo.TYPE_BLE_HEARING_AID -> "BLE_HEARING_AID (API 37)"
                AudioDeviceInfo.TYPE_HEARING_AID -> "HEARING_AID (classic, API 28)"
                AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE_HEADSET"
                else -> "type=${dev.type}"
            }
            "${dev.productName}: $kind"
        }.ifEmpty { "No output devices reported" }
    }

    FeatureScaffold("Hearing aids & assistant audio", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 distinguishes BLE Audio hearing aids from generic BLE headsets via " +
                "AudioDeviceInfo.TYPE_BLE_HEARING_AID so you can skip ducking tricks meant for " +
                "AirPods-like devices.\n\n" +
                "A dedicated STREAM_ASSISTANT / USAGE_ASSISTANT volume is independent of media. " +
                "Assistant apps can enter MODE_ASSISTANT_CONVERSATION so volume keys and BT " +
                "peripherals keep controlling the assistant stream outside active playback.\n\n" +
                "Users can route notifications/ring/alarm to the aid or the speaker independently."
        ) {
            if (AndroidApis.isAndroid17) {
                Text("STREAM_ASSISTANT=${AudioManager.STREAM_ASSISTANT}")
                Text("USAGE_ASSISTANT=${AudioAttributes.USAGE_ASSISTANT}")
                Text("MODE_ASSISTANT_CONVERSATION=${AudioManager.MODE_ASSISTANT_CONVERSATION}")
            } else {
                // Pre-37: USAGE_ASSISTANT exists from API 26; STREAM_ASSISTANT / MODE_ASSISTANT_CONVERSATION are 37.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Text("USAGE_ASSISTANT=${AudioAttributes.USAGE_ASSISTANT} (API 26+)")
                }
            }
            Text(devices)
            Text(
                "Do not call audioManager.mode = MODE_ASSISTANT_CONVERSATION from a normal app; " +
                    "that mode is for assistant-role packages. Shown here as a constant reference."
            )
        }
    }
}
