package com.rick.apiupgrade37.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun BehaviorChangesScreen(
    onBack: (() -> Unit)?,
    listPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val npu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_NEURAL_PROCESSING_UNIT)
    } else {
        // Pre-37: NNAPI via PackageManager.FEATURE_OPENGLES_EXTENSION_PACK / vendor extras.
        false
    }
    val body = @Composable { padding: PaddingValues ->
        FeatureBody(
            padding,
            "These fire only when targetSdk is 37+, even if the device already runs Android 17."
        ) {
            Text("Lock-free MessageQueue", style = MaterialTheme.typography.titleMedium)
            Text(
                "android.os.MessageQueue is lock-free when you target 37. Faster, but any app " +
                    "that reflected on private MessageQueue fields breaks. Tests should use " +
                    "TestLooperManager.peekWhen() / poll() instead of peeking mMessages."
            )
            Text("static final is actually final", style = MaterialTheme.typography.titleMedium)
            Text(
                "Reflection or JNI SetStatic*Field on a static final now throws " +
                    "IllegalAccessException / crashes. Do not patch Build.VERSION.SDK_INT in tests; " +
                    "use Robolectric shadows or a wrapper."
            )
            // BAD (API 37+):
            // Build.VERSION::class.java.getField("SDK_INT").apply { isAccessible = true }.setInt(null, 24)
            Text("Safer native DCL", style = MaterialTheme.typography.titleMedium)
            Text(
                "API 34 made dynamically loaded DEX/JAR read-only. Targeting 37 extends that to " +
                    "native .so files: System.load(path) throws UnsatisfiedLinkError unless the " +
                    "file is marked read-only (and preferably extracted to an immutable dir). " +
                    "Prefer System.loadLibrary() from jniLibs."
            )
            Text("SMS OTP delay", style = MaterialTheme.typography.titleMedium)
            Text(
                "Standard SMS OTPs are delayed 3 hours for apps targeting 37 that are not the " +
                    "default SMS / assistant / companion. Use SMS Retriever or User Consent APIs. " +
                    "WebOTP already delayed non-matching domains."
            )
            Text("Background audio", style = MaterialTheme.typography.titleMedium)
            Text(
                "Playback, audio focus, and volume APIs from the background are hardened. " +
                    "Alarms stay exempt. Use a while-in-use FGS of the correct type."
            )
            Text("NPU declaration", style = MaterialTheme.typography.titleMedium)
            Text(
                "FEATURE_NEURAL_PROCESSING_UNIT declared in the manifest (required=false). " +
                    "Device reports feature=$npu. Targeting 37 without the declaration can block " +
                    "LiteRT NPU / NNAPI / vendor NPU SDKs."
            )
            Text("Physical-keyboard secrets", style = MaterialTheme.typography.titleMedium)
            Text(
                "Password fields no longer flash the last typed character when a hardware " +
                    "keyboard is attached. Compose SecureTextField follows this in 1.12+."
            )
            Text("PQC", style = MaterialTheme.typography.titleMedium)
            Text(
                "Keystore can mint ML-DSA keys (JCA). APK Signature Scheme v3.2 is hybrid " +
                    "classical + ML-DSA. Play App Signing will offer rotation; self-managed keys " +
                    "need a NEW classical key paired with ML-DSA (cannot reuse the old one). " +
                    "apksigner in current build-tools performs the hybrid sign."
            )
            Text("deviceSdk=${Build.VERSION.SDK_INT} isAndroid17=${AndroidApis.isAndroid17}")
        }
    }
    // This screen is reachable two ways: as a top-level tab (onBack == null), where the
    // tab bar owns the bottom inset, and as a catalog entry, where FeatureScaffold supplies
    // its own bars. Hence the two branches.
    if (onBack == null) {
        Scaffold { padding ->
            // Carry the horizontal insets through. In landscape they hold the display
            // cutout and the side navigation bar, and dropping them puts text under both.
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
        FeatureScaffold("Target-37 behaviors", onBack, body)
    }
}
