package com.rick.apiupgrade37.ui.screens

import android.net.DnsResolver
import android.net.dns.HttpsEndpoint
import android.os.Build
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
    var status by remember { mutableStateOf("Tap to query HTTPS DNS (TYPE_HTTPS / ECH configs)") }
    val context = LocalContext.current

    FeatureScaffold("ECH & CT", onBack) { padding ->
        FeatureBody(
            padding,
            "Encrypted Client Hello hides SNI. API 37 adds DnsResolver TYPE_HTTPS queries " +
                "and a <domainEncryption> network-security-config element " +
                "(opportunistic | enabled | disabled).\n\n" +
                "Certificate Transparency is ON by default when you target 37 " +
                "(on 36 you opted in with <certificateTransparency enabled=\"true\" />).\n\n" +
                "See res/xml/network_security_config.xml for the XML you should ship."
        ) {
            Button(
                enabled = AndroidApis.isAndroid17,
                onClick = {
                    if (!AndroidApis.isAndroid17) return@Button
                    val resolver = DnsResolver(context, Looper.getMainLooper())
                    // Pre-37: DnsResolver.getInstance().query(..., Callback<List<InetAddress>>)
                    resolver.query(
                        /* network = */ null,
                        "cloudflare-ech.com",
                        DnsResolver.TYPE_HTTPS,
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
