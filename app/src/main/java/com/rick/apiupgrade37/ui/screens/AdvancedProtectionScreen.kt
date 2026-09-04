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
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun AdvancedProtectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(readEnabled(context)) }

    DisposableEffect(Unit) {
        if (!AndroidApis.isAndroid17) return@DisposableEffect onDispose { }
        val mgr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            context.getSystemService(AdvancedProtectionManager::class.java)
        } else {
            // Pre-37: no AdvancedProtectionManager. Infer from DevicePolicyManager / Play Protect.
            null
        }
        val cb = AdvancedProtectionManager.Callback { value -> enabled = value }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            mgr?.registerAdvancedProtectionCallback(
                ContextCompat.getMainExecutor(context),
                cb
            )
        }
        onDispose { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            mgr?.unregisterAdvancedProtectionCallback(cb)
        }
        }
    }

    FeatureScaffold("Advanced Protection", onBack) { padding ->
        FeatureBody(
            padding,
            "Android Advanced Protection Mode (AAPM) is a user opt-in hardening profile: " +
                "block sideloading, restrict USB data, force Play Protect, and more.\n\n" +
                "Apps should query AdvancedProtectionManager and hide high-risk flows " +
                "(custom APK installers, USB file transfer, debug overlays) when it is on.\n\n" +
                "Pre-37: no single platform switch; apps inferred risk from DevicePolicyManager " +
                "or their own settings."
        ) {
            Text("AAPM enabled = $enabled")
        }
    }
}

private fun readEnabled(context: Context): Boolean {
    if (!AndroidApis.isAndroid17) return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        context.getSystemService(AdvancedProtectionManager::class.java)
            ?.isAdvancedProtectionEnabled == true
    } else {
        false
    }
}
