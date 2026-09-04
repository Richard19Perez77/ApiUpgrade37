package com.rick.apiupgrade37.ui.screens

import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

/**
 * - API 37: AccessibilityEvent.setTextChangeTypes() (IN_COMPOSITION, conversion
 suggestion, COMMITTED_BY_IME) so screen readers can tell composing CJKV from a commit. Hardware-keyboard password fields no longer flash the last char.
 *S
 * - Pre-37: TYPE_VIEW_TEXT_CHANGED with no composition vs commit distinction. Password fields could echo the last typed character on a hardware keyboard.
 *
 * - Need — if you ship an IME. Nicety for ordinary apps (EditText/TextView pick this up when the IME provides TextAttribute).
 */
@Composable
fun AccessibilityImeScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }

    FeatureScaffold("CJKV IME a11y", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 adds AccessibilityEvent.setTextChangeTypes() so screen readers can tell " +
                "composing CJKV text from a committed conversion.\n\n" +
                "TEXT_CHANGE_TYPE_IN_COMPOSITION — still composing\n" +
                "TEXT_CHANGE_TYPE_CONVERSION_SUGGESTION_SELECTED_BY_IME — candidate chosen\n" +
                "TEXT_CHANGE_TYPE_COMMITTED_BY_IME — committed\n\n" +
                "IME authors set this on TYPE_VIEW_TEXT_CHANGED. EditText/TextView do it when " +
                "the IME provides TextAttribute. Compose BasicTextField will pick this up from " +
                "the platform in later Compose releases; SecureTextField hides the last typed " +
                "character on physical keyboards by default on 17."
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Type with a CJKV IME") }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                Text(
                    "Reference constants: " +
                        "IN_COMPOSITION=${AccessibilityEvent.TEXT_CHANGE_TYPE_IN_COMPOSITION} " +
                        "COMMITTED=${AccessibilityEvent.TEXT_CHANGE_TYPE_COMMITTED_BY_IME}"
                )
            }
            // View-based equivalent:
            // event.textChangeTypes = AccessibilityEvent.TEXT_CHANGE_TYPE_COMMITTED_BY_IME
        }
    }
}
