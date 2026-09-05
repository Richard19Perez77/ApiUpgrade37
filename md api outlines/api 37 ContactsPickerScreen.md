```kotlin
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
            ""
        ) {
            Button(
                onClick = {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
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
                        // Pre-37: classic picker, or READ_CONTACTS + ContactsContract queries.
                        // Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                        // intent.putExtra("android.provider.extra.USE_SYSTEM_CONTACTS_PICKER", true)
                        Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    }
                    picker.launch(intent)
                }
            ) { Text(if (AndroidApis.isAndroid17) "Pick contacts (API 37)" else "Pick contact (legacy ACTION_PICK)") }
            Text(status)
        }
    }
}

```

---

# Analysis of `ContactsPickerScreen.kt`

## What This Class Does

`ContactsPickerScreen` is a **Compose UI screen** that demonstrates Android API 37's new **Contacts Picker Session API**. It shows how apps can now select contacts and specific data fields **without requiring the dangerous `READ_CONTACTS` permission**.

---

## What is the Contacts Picker Session API?

The **Contacts Picker Session API** is a new system UI in API 37 that allows users to:
- 📇 **Select specific contacts** from their address book
- 📋 **Choose which fields** to share (phone, email, etc.)
- 🔒 **Grant temporary access** without `READ_CONTACTS` permission
- 🔄 **Session-based access** (temporary and limited)

---

## The Problem Before API 37

### ❌ BEFORE (API 36 and lower):
```kotlin
// Option 1: Request READ_CONTACTS permission (dangerous!)
<uses-permission android:name="android.permission.READ_CONTACTS" />
// App can read ENTIRE address book!
val cursor = contentResolver.query(
    ContactsContract.Contacts.CONTENT_URI,
    null, null, null, null
)
// Reads ALL contacts 😱

// Option 2: ACTION_PICK (basic)
val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
// Problems:
// - Only single selection
// - No field filtering
// - No temporary access
// - Limited UI
```

**Problems:**
1. **READ_CONTACTS** gives access to **ALL** contacts (privacy risk)
2. **ACTION_PICK** is limited and outdated
3. No way to specify **which fields** to share
4. No **multi-select** support
5. No **temporary session** (access persists)

### ✅ AFTER (API 37):
```kotlin
// Option: Contacts Picker Session API
val intent = Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
    // Request only specific fields
    putStringArrayListExtra(
        EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
        arrayListOf(
            CommonDataKinds.Phone.CONTENT_ITEM_TYPE,  // Only phone numbers
            CommonDataKinds.Email.CONTENT_ITEM_TYPE   // Only emails
        )
    )
    // Multi-select support
    putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 5)
    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
}

// User picks contacts
// Get temporary session URI
val sessionUri = result.data
// Query only selected contacts (no READ_CONTACTS needed!)
val cursor = contentResolver.query(sessionUri, ...)
```

**Benefits:**
1. **No READ_CONTACTS permission** needed!
2. **User selects** which contacts to share
3. **Field filtering** (only phone, email, etc.)
4. **Multi-select** support
5. **Temporary session** (access expires)
6. **Privacy-first** approach

---

## Code Analysis

### 1. **Intent Creation**

```kotlin
val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    // API 37: New Contacts Picker Session API
    Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
        // Request only phone and email fields
        putStringArrayListExtra(
            ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
            arrayListOf(
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            )
        )
        // Limit selection to 5 contacts
        putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 5)
        // Allow multiple selection
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }
} else {
    // Pre-API 37: Fallback to classic ACTION_PICK
    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
}
```

**API Check:** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN` (API 37)

**Key Extras:**

| Extra | Purpose | Example |
|-------|---------|---------|
| `EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS` | Which fields to request | Phone, Email, Address |
| `EXTRA_PICK_CONTACTS_SELECTION_LIMIT` | Max number of contacts | 5 |
| `EXTRA_ALLOW_MULTIPLE` | Allow multiple selection | true/false |

---

### 2. **Result Processing**

```kotlin
val picker = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    val data = result.data
    status = if (result.resultCode == Activity.RESULT_OK && data?.data != null) {
        val uri = data.data  // Session URI
        val preview = uri?.let { session ->
            // Query the session data (no READ_CONTACTS needed!)
            context.contentResolver.query(
                session,
                arrayOf(
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null, null
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
```

**What it does:**
1. **Gets session URI** from result
2. **Queries session data** (no permission needed!)
3. **Shows preview** of selected contacts
4. **Graceful handling** of cancellation

---

## Permission Comparison

### READ_CONTACTS (Dangerous Permission)
```
READ_CONTACTS
    ↓
Access to ALL contacts
    ↓
App can read your ENTIRE address book
    ↓
Privacy risk! 😱
```

### Contacts Picker Session (Safe)
```
User opens Contacts Picker
    ↓
User selects specific contacts
    ↓
User selects which fields to share
    ↓
App gets temporary session URI
    ↓
App can only access SELECTED contacts
    ↓
Privacy preserved! 🎉
```

---

## The Auto-Upgrade Behavior

### Important Note:
```kotlin
/**
 * Apps targeting 37 that still fire Intent.ACTION_PICK for contacts are 
 * automatically upgraded to the new UI.
 */
```

**What This Means:**
```kotlin
// Even if you don't update your code:
val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
startActivityForResult(intent)

// On API 37:
// This will AUTO-UPGRADE to the new Contacts Picker!
// Users get the new UI even with old code!
// No READ_CONTACTS needed!
```

**Why This Matters:**
- Apps don't need to update immediately
- Users get better privacy automatically
- Gradual migration is possible

---

## Real-World Use Cases

### 1. **Messaging App**
```kotlin
class MessagingApp {
    fun selectRecipients() {
        // Let user pick contacts for group chat
        val intent = Intent(ACTION_PICK_CONTACTS).apply {
            // Only need phone numbers
            putExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS, 
                arrayListOf(Phone.CONTENT_ITEM_TYPE))
            putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 10)
            putExtra(EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent)
    }
}
```

### 2. **Invitation App**
```kotlin
class InvitationApp {
    fun inviteFriends() {
        // Need name and email for invitations
        val intent = Intent(ACTION_PICK_CONTACTS).apply {
            putExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                arrayListOf(
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE
                ))
            putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 20)
        }
        startActivityForResult(intent)
    }
}
```

### 3. **Profile App**
```kotlin
class ProfileApp {
    fun importContact() {
        // Import contact as profile
        val intent = Intent(ACTION_PICK_CONTACTS).apply {
            putExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                arrayListOf(
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                ))
            putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 1)  // Single selection
            putExtra(EXTRA_ALLOW_MULTIPLE, false)
        }
        startActivityForResult(intent)
    }
}
```

---

## Session URI vs Direct Access

### Session URI (API 37):
```kotlin
// Get session URI
val uri = result.data

// Query only selected contacts
val cursor = contentResolver.query(
    uri,  // Temporary session URI
    projections,
    null, null, null
)
// ✅ Only selected contacts
// ✅ No READ_CONTACTS permission needed
// ✅ Temporary access
```

### Direct Access (Pre-API 37):
```kotlin
// Direct content URI
val cursor = contentResolver.query(
    ContactsContract.Contacts.CONTENT_URI,  // ALL contacts
    projections,
    null, null, null
)
// ❌ ALL contacts accessible
// ❌ Need READ_CONTACTS permission
// ❌ Permanent access
```

---

## Before vs After: User Experience

### ❌ BEFORE (API 36):
```
User installs MessagingApp
         ↓
App requests READ_CONTACTS permission
         ↓
System dialog:
  "Allow app to access your contacts?"
  [Allow] [Deny]
         ↓
User: "Why does this app need ALL my contacts?" 😕
         ↓
Many users deny → App can't work
```

### ✅ AFTER (API 37):
```
User installs MessagingApp
         ↓
App opens Contacts Picker
         ↓
System UI:
  "Select contacts to share"
  - User picks specific contacts
  - User selects fields (phone/email)
  - [Confirm]
         ↓
User: "I control exactly what I share!" 👍
         ↓
App gets only selected contacts
         ↓
Privacy preserved! 🎉
```

---

## Field Types Available

| Field | Constant | Use Case |
|-------|----------|----------|
| Phone | `Phone.CONTENT_ITEM_TYPE` | Calling, messaging |
| Email | `Email.CONTENT_ITEM_TYPE` | Sending email, invitations |
| Name | `StructuredName.CONTENT_ITEM_TYPE` | Display name |
| Address | `StructuredPostal.CONTENT_ITEM_TYPE` | Shipping addresses |
| Organization | `Organization.CONTENT_ITEM_TYPE` | Work contacts |
| Nickname | `Nickname.CONTENT_ITEM_TYPE` | Social apps |

**Requesting Specific Fields:**
```kotlin
putStringArrayListExtra(
    EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
    arrayListOf(
        Phone.CONTENT_ITEM_TYPE,        // Only need phone numbers
        StructuredName.CONTENT_ITEM_TYPE // And names
        // No email, no address, no organization
        // Privacy-first! 🔒
    )
)
```

---

## Key Improvements Summary

| Improvement | Why It Matters | Who Benefits |
|-------------|---------------|--------------|
| **No READ_CONTACTS** | Privacy-first approach | All Users |
| **User Selection** | User chooses which contacts | All Users |
| **Field Filtering** | Only necessary data shared | All Users |
| **Multi-Select** | Batch operations | All Users |
| **Session URI** | Temporary, limited access | All Users |
| **Auto-Upgrade** | Old apps get new UI | All Users |

---

## The "Why" Behind This Change

**Before Android 17:**
- Apps needed **READ_CONTACTS** to access any contact
- **Dangerous permission** gave access to ALL contacts
- Users **didn't trust** apps with their address book
- Many apps **couldn't work** without permission
- **Privacy nightmare** for users

**After Android 17:**
- **No READ_CONTACTS** needed
- **User controls** which contacts are shared
- **Field selection** limits data exposure
- **Session-based** access (temporary)
- **Privacy-first** design
- **Better trust** between users and apps

---

## Summary

API 37's Contacts Picker Session API is a **privacy game-changer**:

1. **No READ_CONTACTS permission** → User keeps control
2. **User selects contacts** → Only chosen contacts shared
3. **Field filtering** → Only necessary data (phone/email)
4. **Multi-select** → Select multiple contacts at once
5. **Session URI** → Temporary, limited access
6. **Auto-upgrade** → Old apps get new UI automatically

**The big picture:** This is Android's shift toward **granular, user-controlled data access**. Instead of "all or nothing" permissions, users now have **fine-grained control** over exactly what they share. This follows the trend of:
- Photo Picker (API 33) → Select specific photos
- Contacts Picker (API 37) → Select specific contacts
- Files Picker → Select specific files

**Developer Action Items:**
- Replace `READ_CONTACTS` with Contacts Picker Session API
- Request only the fields you need (phone, email, etc.)
- Support multi-select where appropriate
- Handle session URI properly
- Test on API 37+ devices
- Gracefully fallback for older devices