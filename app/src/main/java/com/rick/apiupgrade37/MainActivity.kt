package com.rick.apiupgrade37

import android.app.HandoffActivityData
import android.app.HandoffActivityDataRequestInfo
import android.app.HandoffActivityParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.Api37App
import com.rick.apiupgrade37.ui.theme.ApiUpgrade37Theme

/**
 * Compose-first host for the API 37 lab.
 *
 * Handoff / Continue On (API 37): call [setHandoffEnabled] when this screen is ready to be
 * continued on another device, then return [HandoffActivityData] from
 * [onHandoffActivityDataRequested]. Extras must fit in a [PersistableBundle] under ~50KB.
 *
 * Pre-37 there was no platform Continue On. Apps rolled their own with Nearby Share,
 * custom deep links, or Firebase.
 *
 * [enableEdgeToEdge] exists from Activity 1.8 / platform R+; it is the current default
 * for new templates (replacing manual WindowCompat + SYSTEM_UI_FLAG_*).
 */
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (AndroidApis.isAndroid17) {
            val params = HandoffActivityParams.Builder()
                .setAllowHandoffWithoutPackageInstalled(true)
                .build()
            setHandoffEnabled(true, params)
        }

        setContent {
            ApiUpgrade37Theme {
                Api37App()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    override fun onHandoffActivityDataRequested(
        handoffRequestInfo: HandoffActivityDataRequestInfo
    ): HandoffActivityData {
        val extras = PersistableBundle().apply {
            putString(EXTRA_HANDOFF_NOTE, "restored-from-handoff")
            putBoolean("activeRequest", handoffRequestInfo.isActiveRequest)
        }
        return HandoffActivityData.Builder(componentName)
            .setExtras(extras)
            .setFallbackUri("https://developer.android.com/about/versions/17".toUri())
            .build()
        // Direct web-only handoff (no native activity on the peer):
        // return HandoffActivityData.createWebHandoff(Uri.parse("https://example.com/continue"))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // API 37: keyboard / keyboardHidden / navigation / touchscreen / colorMode arrive here
        // instead of destroying/recreating the Activity (unless you opted in with
        // android:recreateOnConfigChanges).
        Log.d(TAG, "configChanged uiMode=${newConfig.uiMode} touch=${newConfig.touchscreen}")
    }

    companion object {
        const val EXTRA_HANDOFF_NOTE = "handoff_note"
        private const val TAG = "MainActivity"
    }
}
