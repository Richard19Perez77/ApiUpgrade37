package com.rick.apiupgrade37.ui.screens

import android.content.Context
import android.os.Build
import android.security.advancedprotection.AdvancedProtectionManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 * Advanced Protection Mode is one of the few entries in this lab that is NOT an API 37
 * feature: AdvancedProtectionManager shipped in Android 16 (API 36, BAKLAVA). Gating it
 * behind isAndroid17 would report "off" on an Android 16 device where the user has
 * actually turned it on, which is the wrong way to fail for a security signal.
 */
@Composable
fun AdvancedProtectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(readEnabled(context)) }

    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            // Pre-36: no platform switch to observe. Apps inferred risk from DevicePolicyManager or their own settings.
            return@DisposableEffect onDispose { }
        }
        val mgr = context.getSystemService(AdvancedProtectionManager::class.java)
            ?: return@DisposableEffect onDispose { }
        val cb = AdvancedProtectionManager.Callback { value -> enabled = value }
        mgr.registerAdvancedProtectionCallback(ContextCompat.getMainExecutor(context), cb)
        onDispose { mgr.unregisterAdvancedProtectionCallback(cb) }
    }

    FeatureScaffold("Advanced Protection", onBack) { padding ->
        FeatureBody(
            padding,
            "Android Advanced Protection Mode (AAPM) is a user opt-in hardening profile: " +
                "block sideloading, restrict USB data, force Play Protect, and more.\n\n" +
                "Apps should query AdvancedProtectionManager and hide high-risk flows " +
                "(custom APK installers, USB file transfer, debug overlays) when it is on.\n\n" +
                "Available from API 36 (Android 16), not 37 — check for BAKLAVA, not " +
                "CINNAMON_BUN, or you will report 'off' on a protected Android 16 device.\n\n" +
                "Reading it needs QUERY_ADVANCED_PROTECTION_MODE, declared in the manifest. " +
                "Pre-36 there was no single platform switch."
        ) {
            Text("AAPM enabled = $enabled")
        }
    }
}

private fun readEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
    return context.getSystemService(AdvancedProtectionManager::class.java)
        ?.isAdvancedProtectionEnabled == true
}
