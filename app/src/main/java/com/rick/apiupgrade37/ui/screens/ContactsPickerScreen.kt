package com.rick.apiupgrade37.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsPickerSessionContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold

@Composable
fun ContactsPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("No session yet") }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        status = if (result.resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data
            val preview = uri?.let { session ->
                context.contentResolver.query(
                    session,
                    arrayOf(ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    buildString {
                        while (cursor.moveToNext()) {
                            append(cursor.getString(0) ?: "?")
                            append(" ")
                            append(cursor.getString(1) ?: "")
                            append('\n')
                        }
                    }
                }
            }
            "Session $uri\n$preview"
        } else {
            "Cancelled"
        }
    }

    FeatureScaffold("Contacts picker", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37 ships a system Contacts Picker. You never hold READ_CONTACTS; the user " +
                "picks people and fields, and you get a temporary session Uri.\n\n" +
                "Apps targeting 37 that still fire Intent.ACTION_PICK for contacts are " +
                "automatically upgraded to the new UI. Use ACTION_PICK_CONTACTS for extras " +
                "(multi-select, field filters, work/personal profiles)."
        ) {
            Button(
                onClick = {
                    val intent = if (AndroidApis.isAndroid17) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                            Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
                                putStringArrayListExtra(
                                    ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                                    arrayListOf(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                                    )
                                )
                                putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 5)
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        } else {
                            TODO("VERSION.SDK_INT < CINNAMON_BUN")
                        }
                    } else {
                        // Pre-37: classic picker (still valid) or request READ_CONTACTS and query the provider.
                        Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                        // Force-preview the new UI on an API 37 device while targeting lower:
                        // intent.putExtra("android.provider.extra.USE_SYSTEM_CONTACTS_PICKER", true)
                    }
                    picker.launch(intent)
                }
            ) { Text(if (AndroidApis.isAndroid17) "Pick contacts (API 37)" else "Pick contact (legacy ACTION_PICK)") }
            Text(status)
        }
    }
}
