package com.rick.apiupgrade37.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.catalogItems

@Composable
fun HomeScreen(
    listPadding: PaddingValues = PaddingValues(),
    onOpen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = listPadding.calculateTopPadding() + 16.dp,
            bottom = listPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Android 17 / API 37 lab", style = MaterialTheme.typography.headlineSmall)
            Text(
                AndroidApis.sdkLabel(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Text(
                "Each card is a working or guarded sample of a platform API that is new " +
                    "or newly enforced when you target SDK 37. Older patterns are left in " +
                    "comments in the Kotlin and the manifest.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        items(catalogItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(item.route) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.api37, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
