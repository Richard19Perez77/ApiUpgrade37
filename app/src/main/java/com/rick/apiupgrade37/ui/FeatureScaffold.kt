package com.rick.apiupgrade37.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rick.apiupgrade37.core.AndroidApis

/**
 * Chrome shared by every feature screen: a title bar and a Back affordance.
 *
 * A plain TextButton stands in for the usual IconButton with an arrow so the project does
 * not need the material-icons-extended artifact, which is large and would obscure how few
 * dependencies the samples actually require.
 *
 * The Back button and the system back gesture are separate paths. This is the button; the
 * gesture is handled by the BackHandler in [Api37App]. Both end up at the catalog. Note
 * that predictive back is on by default once you target 36 or later, so
 * android:enableOnBackInvokedCallback in the manifest is redundant here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        content(padding)
    }
}

/**
 * Body shared by every feature screen: the device's SDK level, an explanation, then
 * whatever interactive parts that screen provides.
 *
 * The SDK banner is on every screen deliberately. Most samples behave differently below
 * API 37, and without the banner it is easy to mistake a correct "this needs Android 17"
 * fallback for broken code.
 *
 * verticalScroll rather than LazyColumn: the content is a fixed handful of composables per
 * screen, so there is nothing to recycle, and scrolling has to work at App Bubble sizes
 * where even short text overflows.
 */
@Composable
fun FeatureBody(
    padding: PaddingValues,
    intro: String,
    extra: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Two padding calls, in this order, on purpose: the first insets the content
            // past the system bars and app bar, the second is the visual margin. Swapping
            // them would push the margin outside the insets and misalign the content.
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(AndroidApis.sdkLabel(), style = MaterialTheme.typography.labelMedium)
        Text(intro, style = MaterialTheme.typography.bodyMedium)
        extra()
    }
}
