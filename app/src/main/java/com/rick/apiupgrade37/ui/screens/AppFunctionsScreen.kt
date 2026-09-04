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
