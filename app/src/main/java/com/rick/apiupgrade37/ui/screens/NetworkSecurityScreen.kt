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

/**
 *
 *  Network Security Enhancements
 *      ECH hides SNI - hides SNI (Server Name Indication)
 *      HTTPS DNS Records - query for ECH configurations via DNS
 *      Certificate Transparency - enabled by default
 *      Domain Encryption - new network security config element
 *
 *  ECH - Encrypted Client Hello
 *      Privacy issues -> now encrypted where SNI was plain text
 *
 *  API 37 improves:
 *      SNI privacy with Encrypted SNI (ECH)
 *      DNS HTTPS Records DnsResolver queries improves Discovery
 *      ECH Configs, the default is on and effortless
 *      Certificate Transparency is now on by default for Security
 *      Domain Encryption domainEncryption element instills control
 *
 *  DnsResolver Setup
 *      Creates a DNS resolver instance
 *      Uses main looper for callback dispatch
 *      Can query HTTPS records (new in API 37)
 *
 *  Querying HTTPS Records
 *      DNS HTTPS resource records
 *      Contains ECH configuration
 *      Used to encrypt SNI
 *
 *  Processing HTTPS Records
 *      List of HTTPS RRs
 *      ECH configuration from each record
 *      Status Update - shows what was found
 *
 *  Certificate Transparency (CT)
 *      All SSL/TLS certificate are logged publicly
 *      Browser/OSes check the logs
 *      Forge certificate are detected quickly
 *      Security is improved
 *
 *  <certificateTransparency enabled="true" />
 *      No need for this in network-security-config and domain-config
 *
 *  domainEncryption
 *      mode="opportunistic"
 *      trustAnchors="system"
 *
 *  Security Implications:
 *      Privacy
 *      Content Filtering
 *      Censorship
 *
 *  Testing ECH
 *      ECH - encrypts SNI privacy for all users
 *      HTTPS DNS Records discovers ECH config for all apps
 *      Certificate Transparency detects forged certificates all users security
 *      Domain Encryption will Control ECH behavior helps Discover
 *      CT Default more secure by default all users benefit
 *
 *  The "Why" Behind This Change
 *      SNI is ecnrypted - privacy preserved
 *      CT is default - improved security automatically
 *      ECH auto-discovers - no manual config needed
 *      DNS HTTPS records - standardizes discovery
 *      Better privacy and security for all users
 *
 *  API 37 privacy and security by default:
 *      makes the web more private and secure
 *      without devs doing anything
 *      ECH, CT default-on and DNS HTTPS records work together to
 *
 *
 * - API 37: DnsResolver HTTPS-record query returns HttpsEndpoint / ECH configs. network_security_config adds a domainEncryption element (opportunistic here).
 *      Certificate Transparency is on by default at target 37.
 *
 * - Pre-37: Classic TLS with cleartext SNI.
 *      On API 36, CT was opt-in via certificateTransparency enabled=true.
 *
 * - Mixed — CT default-on is a need (hosts without CT logs can fail).
 *      Opportunistic ECH and the HTTPS DNS query are niceties.
 */
@Composable
fun NetworkSecurityScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf("Tap to query HTTPS DNS (ECH configs)") }
    val context = LocalContext.current

    FeatureScaffold("ECH & CT", onBack) { padding ->
        FeatureBody(
            padding,
            "Encrypted Client Hello hides SNI. API 37 adds DnsResolver HTTPS-record queries and a <domainEncryption> network-security-config element (opportunistic | enabled | disabled).\n\n Certificate Transparency is ON by default when you target 37 (on 36 you opted in with <certificateTransparency enabled=\"true\" />).\n\n See res/xml/network_security_config.xml for the XML you should ship."
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
