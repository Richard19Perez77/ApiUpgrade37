```kotlin
package com.rick.apiupgrade37.ui.screens

import android.net.DnsResolver
import android.net.dns.HttpsEndpoint
import android.os.CancellationSignal
import android.os.Looper
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
import java.net.UnknownHostException

@Composable
fun NetworkSecurityScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf("Tap to query HTTPS DNS (ECH configs)") }
    val context = LocalContext.current

    FeatureScaffold("ECH & CT", onBack) { padding ->
        FeatureBody(
            padding,
            ""
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    if (!AndroidApis.isAndroid17) return@Button
                    val resolver = DnsResolver(context, Looper.getMainLooper())
                    resolver.query(
                        /* network = */ null,
                        "cloudflare-ech.com",
                        DnsResolver.FLAG_EMPTY,
                        ContextCompat.getMainExecutor(context),
                        DnsResolver.HTTPS_QUERY_WAIT_AUTO,
                        CancellationSignal(),
                        object : DnsResolver.Callback<HttpsEndpoint> {
                            override fun onAnswer(answer: HttpsEndpoint, rcode: Int) {
                                val records = answer.httpsRecords
                                val ech = records.mapNotNull { rec ->
                                    runCatching { rec.echConfigList }.getOrNull()
                                }
                                status = "rcode=$rcode records=${records.size} echConfigs=${ech.size}"
                            }

                            override fun onError(error: DnsResolver.DnsException) {
                                status = "DNS error code=${error.code} ${error.message}"
                            }
                        }
                    )
                }
            ) { Text("Query HTTPS records") }
            Text(status)
            Text(
                "UnknownHostException on older stacks is expected if TYPE_HTTPS is unsupported: " +
                    UnknownHostException::class.java.simpleName
            )
        }
    }
}

```

---

# Analysis of `NetworkSecurityScreen.kt`

## What This Class Does

`NetworkSecurityScreen` is a **Compose UI screen** that demonstrates Android API 37's new **network security enhancements**:
- **Encrypted Client Hello (ECH)** - hides SNI (Server Name Indication)
- **HTTPS DNS Records** - query for ECH configurations via DNS
- **Certificate Transparency (CT)** - enabled by default
- **Domain Encryption** - new network security config element

---

## What is Encrypted Client Hello (ECH)?

**ECH** is a security feature that **encrypts the Server Name Indication (SNI)** during TLS handshakes.

**The Problem Before ECH:**
```
Client → Server: "Hello, I want to connect to facebook.com" (SNI in plaintext)
         ↓
Anyone watching: "Oh, they're visiting Facebook!"
         ↓
Privacy issue! 👀
```

**With ECH (API 37):**
```
Client → Server: "Hello, I want to connect to [encrypted]"
         ↓
Anyone watching: "I have no idea which site they're visiting!"
         ↓
Privacy preserved! 🎉
```

---

## The API 37 Improvements

| Feature | Before (API ≤36) | After (API 37) | Improvement |
|---------|-----------------|----------------|-------------|
| **SNI Privacy** | Plaintext SNI | Encrypted SNI (ECH) | **Privacy** 🔒 |
| **DNS HTTPS Records** | Not available | `DnsResolver` queries | **Discovery** |
| **ECH Configs** | Manual configuration | Automatic via DNS | **Effortless** |
| **Certificate Transparency** | Opt-in | Default on | **Security** 🛡️ |
| **Domain Encryption** | Not available | `domainEncryption` element | **Control** |

---

## Code Analysis

### 1. **DnsResolver Setup**

```kotlin
val resolver = DnsResolver(context, Looper.getMainLooper())
```

**What it does:**
- Creates a DNS resolver instance
- Uses main looper for callback dispatch
- Can query HTTPS records (new in API 37)

**Why Main Looper:**
```kotlin
// Callbacks need to run on main thread to update UI
// Looper.getMainLooper() ensures UI-safe updates
```

---

### 2. **Querying HTTPS Records**

```kotlin
resolver.query(
    /* network = */ null,           // Default network
    "cloudflare-ech.com",            // Domain to query
    DnsResolver.FLAG_EMPTY,          // No special flags
    ContextCompat.getMainExecutor(context),  // Main thread executor
    DnsResolver.HTTPS_QUERY_WAIT_AUTO,       // Auto timeout
    CancellationSignal(),            // Can cancel query
    object : DnsResolver.Callback<HttpsEndpoint> {
        override fun onAnswer(answer: HttpsEndpoint, rcode: Int) {
            // Process HTTPS records
        }
        
        override fun onError(error: DnsResolver.DnsException) {
            // Handle error
        }
    }
)
```

**What This Queries:**
- DNS **HTTPS resource records** (type 65)
- Contains ECH configuration
- Used to encrypt SNI

---

### 3. **Processing HTTPS Records**

```kotlin
override fun onAnswer(answer: HttpsEndpoint, rcode: Int) {
    val records = answer.httpsRecords
    val ech = records.mapNotNull { rec ->
        runCatching { rec.echConfigList }.getOrNull()
    }
    status = "rcode=$rcode records=${records.size} echConfigs=${ech.size}"
}
```

**What it extracts:**
1. **HTTPS Records** → List of HTTPS RRs
2. **ECH Config Lists** → ECH configuration from each record
3. **Status Update** → Shows what was found

**HTTPS Record Example:**
```
cloudflare-ech.com. 300 IN HTTPS 1 . alpn="h2" ech=AEX+D... (ECH config)
                                         ^^^^^^^^^^^^^
                                         This is the ECH config!
```

---

## Certificate Transparency (CT)

### What is Certificate Transparency?

CT is a system where:
1. All SSL/TLS certificates are **logged publicly**
2. **Browsers/OSes** check the logs
3. **Forge certificates** are detected quickly
4. **Security** is improved

### Before API 37:
```xml
<!-- Had to opt-in to CT -->
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">example.com</domain>
        <!-- Must explicitly enable CT -->
        <certificateTransparency enabled="true" />
    </domain-config>
</network-security-config>
```

### After API 37:
```xml
<!-- CT is ON by default -->
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">example.com</domain>
        <!-- No need to specify! CT is automatic -->
    </domain-config>
</network-security-config>
```

**Impact:**
- **More secure** by default
- **No developer action** needed
- **Failure if CT logs missing** (need to handle)

---

## Domain Encryption Network Security Config

### New API 37 Element:

```xml
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">example.com</domain>
        
        <!-- NEW: Domain encryption configuration -->
        <domainEncryption 
            mode="opportunistic"    <!-- opportunistic | enabled | disabled -->
            trustAnchors="system"   <!-- system | user | all -->
        />
    </domain-config>
</network-security-config>
```

**Modes:**

| Mode | Behavior | Use Case |
|------|----------|----------|
| `opportunistic` | Use ECH if available, fallback if not | Best compatibility |
| `enabled` | Require ECH, fail if not available | Strong security |
| `disabled` | Don't use ECH | Testing/debugging |

---

## The Complete ECH Flow

```
App wants to connect to cloudflare-ech.com
         ↓
Step 1: Query HTTPS DNS Records
         ↓
   DnsResolver.query() → HTTPS RR
         ↓
Step 2: Extract ECH Config
         ↓
   rec.echConfigList → ECH config
         ↓
Step 3: TLS Handshake with ECH
         ↓
   Client Hello → Encrypted SNI
         ↓
Step 4: Connection Established
         ↓
   Server decrypts SNI, accepts connection
         ↓
   User privacy preserved! 🎉
```

---

## Real-World Impact

### Before API 37:
```
User visits facebook.com
         ↓
ISP sees: "User is visiting facebook.com"
         ↓
ISP: "Let's log that. Targeted ads for social media!"
         ↓
Privacy violated! 😱
```

### After API 37:
```
User visits facebook.com  
         ↓
ISP sees: "User is connecting to [encrypted]"
         ↓
ISP: "I can't see which site!"
         ↓
Privacy preserved! 🎉
```

---

## The API Check Pattern

```kotlin
Button(
    enabled = AndroidApis.isAndroid17,  // ✅ CHECK 1: API 37+
    onClick = {
        // ✅ CHECK 2: Double-check in onClick
        if (!AndroidApis.isAndroid17) return@Button
        
        // ✅ CHECK 3: DnsResolver available (API 37+)
        val resolver = DnsResolver(context, Looper.getMainLooper())
        
        // Query HTTPS records
        resolver.query(...)
    }
) { Text("Query HTTPS records") }
```

**Fallback Handling:**
```kotlin
Text(
    "UnknownHostException on older stacks is expected if TYPE_HTTPS is unsupported: " +
    UnknownHostException::class.java.simpleName
)
```

**Why This Matters:**
- Older Android versions don't support HTTPS DNS records
- Query will fail with `UnknownHostException`
- This is **expected behavior**, not a bug

---

## Security Implications

### 1. **Privacy**
```
Without ECH:  Anyone can see which sites you visit
With ECH:     Only the server knows which site you visit
```

### 2. **Content Filtering**
```
Without ECH:  ISPs can block specific sites
With ECH:     ISPs can't see the site name to block it
```

### 3. **Censorship**
```
Without ECH:  Government can censor specific sites
With ECH:     Government can't identify the site
```

---

## Testing ECH

### Test Domain: cloudflare-ech.com
```kotlin
// Cloudflare provides a test domain
"cloudflare-ech.com"
// This domain supports ECH
// Query HTTPS records to get ECH config
```

### Expected Results:
```
On API 37+:  ✅ HTTPS records returned, ECH config found
On API 36-:  ❌ UnknownHostException (TYPE_HTTPS not supported)
```

---

## Network Security Config Example

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <!-- Default behavior for all domains -->
    <base-config>
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    
    <!-- Domain-specific configurations -->
    <domain-config>
        <domain includeSubdomains="true">api.myapp.com</domain>
        
        <!-- API 37: Opportunistic ECH -->
        <domainEncryption mode="opportunistic" />
        
        <!-- Certificate Transparency is ON by default in API 37 -->
        <!-- No need to specify certificateTransparency -->
    </domain-config>
    
    <!-- Development domains (no ECH required) -->
    <domain-config>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domainEncryption mode="disabled" />
    </domain-config>
</network-security-config>
```

---

## Key Improvements Summary

| Feature | What It Does | Who Benefits |
|---------|-------------|--------------|
| **ECH** | Encrypts SNI (server name) | All users (privacy) |
| **HTTPS DNS Records** | Discovers ECH configs | All apps |
| **Certificate Transparency** | Detects forged certificates | All users (security) |
| **Domain Encryption** | Control ECH behavior | Developers |
| **CT Default** | More secure by default | All users |

---

## The "Why" Behind This Change

**Before Android 17:**
- **SNI** was plaintext → anyone could see which sites you visit
- **CT** was optional → many apps didn't enable it
- **ECH** required manual configuration → complex setup
- **DNS HTTPS records** didn't exist → couldn't auto-discover ECH

**After Android 17:**
- **SNI is encrypted** → privacy preserved
- **CT is default** → improved security automatically
- **ECH auto-discovers** → no manual config needed
- **DNS HTTPS records** → standardized discovery
- **Better privacy and security** for all users

---

## Summary

API 37's network security enhancements are about **privacy and security by default**:

1. **Encrypted Client Hello (ECH)** → SNI encrypted, privacy preserved
2. **HTTPS DNS Records** → Auto-discover ECH configurations
3. **Certificate Transparency** → On by default, security improved
4. **Domain Encryption Config** → Granular control over ECH

**The big picture:** Android is making the web more private and secure without requiring developers to do anything. ECH, CT default-on, and DNS HTTPS records work together to:
- **Hide what sites users visit** (privacy)
- **Detect forged certificates** (security)
- **Auto-configure encryption** (effortless)
- **Make web browsing safer** for everyone

**Developer Action Items:**
- Test with `cloudflare-ech.com` to verify ECH support
- Handle `UnknownHostException` gracefully on older devices
- Consider updating `network_security_config.xml` for ECH
- Be aware that CT default-on may cause failures for hosts without CT logs