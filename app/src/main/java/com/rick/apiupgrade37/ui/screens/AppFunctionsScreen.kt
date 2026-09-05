package com.rick.apiupgrade37.ui.screens

import android.app.appfunctions.AppFunction
import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.app.appsearch.GenericDocument
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
 *  API 37: AppFunction API
 *      Android MCP (model context protocol)
 *          apps can now expose functions or tools to on device AI agents
 *          standardized API for this
 *
 *  MCP:
 *      on device AI agents interacting with apps
 *      standardized function calling across apps
 *      discoverable tools that agents can use
 *      privacy first local AI processing
 *
 *  Ex.
 *      discover your notes taking app's "createNote" function
 *      call it with "write shopping list"
 *      get the created list note back as GenericDocument
 *      all without your app needing to implement custom APIs!
 *
 *  Any agent can use any app's functions!
 *
 *  Why?
 *      standardized function registry
 *      discoverable tools for AI agents
 *      GenericDocument for structured data
 *      platform wide integration
 *      enables the next generation of AI assistants
 *
 *  Android is getting ready for the AI era!
 *      This is how Android apps will interact with on-device AI assistants!
 *
 *
 * - API 37: AppFunctionManager.registerAppFunction exposes a tool (here createNote) that returns a GenericDocument for on-device agents.
 *
 * - Pre-37: No platform AppFunctions / Android MCP. Apps were not discoverable as agent tools except via custom APIs.
 *
 * - Nicety — optional agent integration. The app runs fine unregistered.
 */
@Composable
fun AppFunctionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Not registered") }

    FeatureScaffold("AppFunctions", onBack) { padding ->
        FeatureBody(
            padding,
            "Android 17 treats apps as tool providers for on-device agents (Android MCP). " +
                "Register functions through AppFunctionManager. Production apps usually use the " +
                "Jetpack AppFunctions library (@AppFunction + KDoc) which generates this binder glue.\n\n" +
                "This sample registers a platform AppFunction that returns a GenericDocument. " +
                "Agents discover it via searchAppFunctions / observeAppFunctions."
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    if (!AndroidApis.isAndroid17) return@Button
                    val mgr = context.getSystemService(AppFunctionManager::class.java)
                    val fn = AppFunction { _, _, callback ->
                        val doc = GenericDocument.Builder<GenericDocument.Builder<*>>(
                            "api37",
                            "note-1",
                            "DemoNote"
                        ).setPropertyString("title", "Created by AppFunction").build()
                        callback.onResult(ExecuteAppFunctionResponse(doc))
                    }
                    mgr.registerAppFunction(
                        "createNote",
                        ContextCompat.getMainExecutor(context),
                        fn
                    )
                    status = "Registered createNote — use the AppFunctions test agent / ADB to invoke it"
                }
            ) { Text("Register createNote()") }
            Text(status)
        }
    }
}
