Based on the code analysis, here are the **API 37 bullet points** and the **gain/improvement** for each:

---

## **1. Lock-free MessageQueue**
- **What it does:** `android.os.MessageQueue` becomes lock-free when targeting API 37
- **Gain:** **Performance improvement** - Faster message queue operations
- **Breaking change:** Apps that reflected on private `MessageQueue` fields (like `mMessages`) break

---

## **2. static final is actually final**
- **What it does:** Reflection or JNI `SetStatic*Field` on `static final` fields throws `IllegalAccessException` or crashes
- **Gain:** **Security improvement** - Better encapsulation and memory safety
- **Breaking change:** Tests that patch `Build.VERSION.SDK_INT` via reflection will break

---

## **3. Safer native DCL (Dynamic Code Loading)**
- **What it does:** Extends API 34's read-only DEX/JAR restriction to native `.so` files; `System.load(path)` fails on writable files
- **Gain:** **Security improvement** - Prevents tampering with loaded native libraries
- **Breaking change:** Apps that extract and load `.so` files from writable locations need to switch to `System.loadLibrary()`

---

## **4. SMS OTP delay**
- **What it does:** Standard SMS OTPs are delayed 3 hours unless app is default SMS/assistant/companion
- **Gain:** **Privacy improvement** - Protects users from background SMS reading
- **Breaking change:** Apps reading OTPs from SMS must switch to SMS Retriever or User Consent APIs

---

## **5. Background audio hardening**
- **What it does:** Playback, audio focus, and volume APIs from background are restricted
- **Gain:** **Battery/User experience improvement** - Prevents unwanted background audio
- **Breaking change:** Apps playing from background need properly typed while-in-use foreground service

---

## **6. NPU declaration requirement**
- **What it does:** Apps using NPU must declare `FEATURE_NEURAL_PROCESSING_UNIT` in manifest
- **Gain:** **Transparency** - Users can see when apps use neural hardware
- **Breaking change:** Apps using NNAPI/LiteRT NPU/vendor NPU SDKs without declaration get blocked

---

## **7. Physical-keyboard secrets**
- **What it does:** Password fields no longer flash the last typed character with hardware keyboard
- **Gain:** **Security/Privacy improvement** - Prevents shoulder-surfing on physical keyboards
- **Note:** Automatic improvement (no action needed)

---

## **8. PQC (Post-Quantum Cryptography)**
- **What it does:** Keystore supports ML-DSA keys; APK Signature Scheme v3.2 uses hybrid classical + ML-DSA
- **Gain:** **Future-proofing** - Quantum-resistant signing and key storage
- **Breaking change:** Self-managed keys need NEW classical key paired with ML-DSA (can't reuse old one)

---

## **Summary Table**

| Feature | Gain | Action Required |
|---------|------|-----------------|
| Lock-free MessageQueue | Performance | Fix reflection |
| static final protection | Security | Stop patching SDK_INT |
| Native DCL | Security | Use loadLibrary() |
| SMS OTP delay | Privacy | Use SMS Retriever |
| Background audio | Battery/UX | Use FGS |
| NPU declaration | Transparency | Add manifest flag |
| Keyboard echo | Privacy | None (automatic) |
| PQC signing | Future-proofing | Rotate keys |

**Core theme:** Most changes are **security/privacy hardening** with performance as a bonus for the MessageQueue change.

---

I cannot directly create a downloadable file on your system, but I can provide you with the complete markdown content. You can copy it and save it as `API37-Migration-Guide.md`.

Here's the complete content ready to copy:

```markdown
# Android API 37 Migration Guide
## Code Samples for Each Behavioral Change

---

## 1. Lock-free MessageQueue

### Problem
Apps reflecting on `MessageQueue` private fields break.

### Solution
Use `TestLooperManager` for tests. For production code, the lock-free behavior is automatic.

**❌ BAD (Pre-API 37 style - breaks on API 37+):**
```kotlin
// Reflection on private field - CRASHES on API 37
val queue = Looper.myQueue()
val mMessages = queue.javaClass.getDeclaredField("mMessages")
mMessages.isAccessible = true
val messages = mMessages.get(queue)
```

**✅ GOOD (Tests only):**
```kotlin
import android.os.TestLooperManager
import androidx.test.core.app.ApplicationProvider

@Test
fun testMessageQueue() {
    val looperManager = TestLooperManager(ApplicationProvider.getApplicationContext())
    looperManager.peekWhen()
    looperManager.poll()
    looperManager.execute()
}
```

---

## 2. static final is Actually Final

### Problem
Reflection or JNI on `static final` fields throws `IllegalAccessException`.

### Solution
Use Robolectric or dependency injection instead of patching.

**❌ BAD (Crashes on API 37+):**
```kotlin
// Don't do this!
val sdkInt = Build.VERSION::class.java.getDeclaredField("SDK_INT")
sdkInt.isAccessible = true
sdkInt.setInt(null, 24)  // 💥 CRASH on API 37+
```

**✅ GOOD - Use Robolectric:**
```kotlin
// In test class
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])  // Simulates API 24
class MyTest {
    // Tests run with SDK_INT = 24
}
```

**✅ GOOD - Use DI/Wrapper pattern:**
```kotlin
interface AndroidVersionProvider {
    val sdkInt: Int
}

class RealVersionProvider : AndroidVersionProvider {
    override val sdkInt: Int get() = Build.VERSION.SDK_INT
}

class TestVersionProvider(override val sdkInt: Int) : AndroidVersionProvider
```

---

## 3. Safer Native DCL (System.load)

### Problem
`System.load(path)` on writable `.so` files throws `UnsatisfiedLinkError`.

### Solution
Use `System.loadLibrary()` from `jniLibs` instead.

**❌ BAD (Throws on API 37+):**
```kotlin
// Extracting to writable location
val libFile = File(cacheDir, "mylib.so")
extractLibrary(libFile)  // Writable file
System.load(libFile.absolutePath)  // 💥 UNSATISFIEDLINKERROR on API 37+
```

**✅ GOOD - Use loadLibrary():**
```kotlin
// Place libs in app/src/main/jniLibs/armeabi-v7a/libmylib.so
// Then load via:
System.loadLibrary("mylib")  // ✅ Works on API 37+
```

**✅ GOOD - Mark file read-only:**
```kotlin
// If you must use System.load()
val libFile = File(cacheDir, "mylib.so")
extractLibrary(libFile)
libFile.setReadOnly()  // Mark read-only
System.load(libFile.absolutePath)  // ✅ Works if read-only
```

**📁 Directory Structure:**
```
app/
└── src/
    └── main/
        └── jniLibs/
            ├── armeabi-v7a/
            │   └── libmylib.so
            ├── arm64-v8a/
            │   └── libmylib.so
            └── x86_64/
                └── libmylib.so
```

---

## 4. SMS OTP Delay

### Problem
SMS OTPs delayed 3 hours for non-default SMS apps.

### Solution
Use **SMS Retriever API** or **User Consent API**.

**✅ GOOD - SMS Retriever API:**
```kotlin
// 1. Add dependency
// implementation 'com.google.android.gms:play-services-auth:20.7.0'

class OTPReceiver : BroadcastReceiver() {
    private val smsRetriever = SmsRetriever.getClient(context)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    // Extract OTP from message
                    val otp = extractOTP(message)
                    // Auto-fill OTP
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Handle timeout
                }
            }
        }
    }
}
```

**✅ GOOD - User Consent API:**
```kotlin
// 1. Add dependency
// implementation 'com.google.android.gms:play-services-auth-api-phone:18.0.1'

private fun requestOTP() {
    val client = PhoneAuthClient.getClient(context)
    val intent = client.getSignInIntent()
    startIntentSenderForResult(
        intent.intentSender,
        REQUEST_CODE,
        null, 0, 0, 0, null
    )
}

// Handle result
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            val message = data?.getStringExtra(PhoneAuthProvider.EXTRA_OTP)
            // Extract and use OTP
        }
    }
}
```

**❌ BAD (Delayed 3 hours):**
```kotlin
// Reading SMS directly
val sms = contentResolver.query(
    Uri.parse("content://sms/inbox"),
    arrayOf("body"),
    null, null, null
)
// This will be delayed 3 hours on API 37+
```

---

## 5. Background Audio Hardening

### Problem
Background playback/focus/volume APIs are restricted.

### Solution
Use a correctly typed foreground service.

**✅ GOOD - Foreground Service:**
```kotlin
// AndroidManifest.xml
<service
    android:name=".MusicService"
    android:foregroundServiceType="mediaPlayback" />

// Service implementation
class MusicService : Service() {
    override fun onCreate() {
        super.onCreate()
        
        // Start foreground
        if (Build.VERSION.SDK_INT >= 34) {
            // Create notification for Android 14+
            val notification = createNotification()
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        
        // Audio focus management
        val afd = AudioManager(applicationContext)
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> pausePlayback()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlayback()
                    AudioManager.AUDIOFOCUS_GAIN -> resumePlayback()
                }
            }
            .build()
        
        afd.requestAudioFocus(focusRequest)
    }
}
```

**❌ BAD (No foreground service):**
```kotlin
// Playing audio without FGS
val mediaPlayer = MediaPlayer()
mediaPlayer.setDataSource(url)
mediaPlayer.prepare()
mediaPlayer.start()  // 💥 Restricted on API 37+
```

---

## 6. NPU Declaration Requirement

### Problem
NPU access blocked without manifest declaration.

### Solution
Add `FEATURE_NEURAL_PROCESSING_UNIT` to manifest.

**✅ GOOD - Add to AndroidManifest.xml:**
```xml
<manifest ...>
    <!-- Required for API 37+ NPU access -->
    <uses-feature
        android:name="android.hardware.neural_processing_unit"
        android:required="false" />
    
    <application>
        ...
    </application>
</manifest>
```

**✅ GOOD - Check feature availability:**
```kotlin
fun isNPUAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_NEURAL_PROCESSING_UNIT
        )
    } else {
        // Pre-API 37 fallback
        false
    }
}

// Usage in code
if (isNPUAvailable()) {
    // Use NNAPI or LiteRT NPU delegate
    interpreter.useNnApiForInference()
} else {
    // Fallback to CPU
    interpreter.useCpuForInference()
}
```

**❌ BAD (No manifest declaration):**
```kotlin
// Using NNAPI without manifest declaration
val interpreter = Interpreter(model)
val delegate = NnApiDelegate()  // 💥 Blocked on API 37+
interpreter.setDelegate(delegate)
```

---

## 7. Physical-Keyboard Secrets (Automatic)

### Problem
Password fields no longer flash last typed character.

### Solution
**✅ GOOD - Automatic improvement!**

No code changes needed. This is a security enhancement that applies automatically to all `TextField` with `visualTransformation = PasswordVisualTransformation()`.

```kotlin
// Compose 1.12+ SecureTextField handles this automatically
TextField(
    value = password,
    onValueChange = { password = it },
    visualTransformation = PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
)

// XML EditText with inputType="textPassword" also automatically fixed
// <EditText
//     android:inputType="textPassword"
//     android:password="@id/password" />
```

---

## 8. PQC (Post-Quantum Cryptography) - ML-DSA

### Problem
Self-managed keys need rotation to support ML-DSA.

### Solution
Generate new keys with ML-DSA algorithm.

**✅ GOOD - Generate ML-DSA Key Pair:**
```kotlin
import java.security.KeyPairGenerator

fun generateMLDSAKeys() {
    // Available in Android Keystore on API 37+
    val keyPairGenerator = KeyPairGenerator.getInstance(
        "ML-DSA",  // Or "ML-DSA-44", "ML-DSA-65", "ML-DSA-87"
        "AndroidKeyStore"
    )
    
    val spec = KeyGenParameterSpec.Builder(
        "mldsa_key",
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setAlgorithmParameterSpec(MLDSAParameterSpec.ML_DSA_65)
        .setDigests(KeyProperties.DIGEST_NONE)  // ML-DSA is hash-and-sign
        .build()
    
    keyPairGenerator.initialize(spec)
    val keyPair = keyPairGenerator.genKeyPair()
    
    // Store key alias for later use
    val privateKey = keyPair.private
    val publicKey = keyPair.public
}
```

**✅ GOOD - Sign and Verify with ML-DSA:**
```kotlin
fun signData(data: ByteArray, keyAlias: String): ByteArray {
    val signature = Signature.getInstance("ML-DSA")
    val privateKey = getPrivateKey(keyAlias)
    signature.initSign(privateKey)
    signature.update(data)
    return signature.sign()
}

fun verifyData(data: ByteArray, signatureBytes: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("ML-DSA")
    signature.initVerify(publicKey)
    signature.update(data)
    return signature.verify(signatureBytes)
}
```

**✅ GOOD - APK Signing with ML-DSA (build.gradle):**
```gradle
android {
    defaultConfig {
        // Play App Signing handles rotation for managed keys
    }
}

// For self-managed keys, use apksigner:
// apksigner sign --ks keystore.jks --key key.alias --ml-dsa app.apk
```

**❌ BAD (Cannot reuse old classical keys):**
```kotlin
// This WON'T work with ML-DSA
val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
val keyPair = keyPairGenerator.genKeyPair()  // Old key - can't pair with ML-DSA

// You MUST generate a NEW key
val mldsaKeyPair = KeyPairGenerator.getInstance("ML-DSA", "AndroidKeyStore")
    .genKeyPair()  // ✅ New key with ML-DSA
```

---

## Quick Reference Table

| Feature | Minimum API | Code Change Required? | Sample Reference |
|---------|------------|----------------------|------------------|
| Lock-free MessageQueue | 37 | Only if using reflection | Section 1 |
| static final | 37 | Yes (use Robolectric/DI) | Section 2 |
| Native DCL | 37 | Yes (use loadLibrary) | Section 3 |
| SMS OTP | 37 | Yes (use SMS Retriever) | Section 4 |
| Background Audio | 37 | Yes (use FGS) | Section 5 |
| NPU Declaration | 37 | Yes (manifest) | Section 6 |
| Keyboard Echo | 37 | No (automatic) | Section 7 |
| PQC Signing | 37 | Yes (rotate keys) | Section 8 |

---

## Build Configuration

Update your `build.gradle` to target API 37:

```gradle
android {
    compileSdk = 37
    
    defaultConfig {
        targetSdk = 37
        minSdk = 21  // Or your minimum
    }
}
```

## Testing Checklist

- [ ] Remove all reflection on `MessageQueue`
- [ ] Use Robolectric instead of reflecting on `Build.VERSION.SDK_INT`
- [ ] Replace `System.load()` with `System.loadLibrary()`
- [ ] Implement SMS Retriever for OTPs
- [ ] Add foreground service for background audio
- [ ] Add `FEATURE_NEURAL_PROCESSING_UNIT` to manifest
- [ ] Generate new ML-DSA keys for self-managed signing
- [ ] Update dependencies (Compose 1.12+, Support Library)
- [ ] Test on Android 17 (API 37) emulator/device
```

---

## How to Save This File

**Option 1: Copy and Paste (Windows/Linux/macOS)**
1. Select all the content above (from line 1 to the end)
2. Copy it (Ctrl/Cmd + C)
3. Open a text editor (Notepad, VS Code, Sublime, etc.)
4. Paste the content (Ctrl/Cmd + V)
5. Save as `API37-Migration-Guide.md`

**Option 2: Terminal Command (macOS/Linux)**
```bash
cat > API37-Migration-Guide.md << 'EOF'
[PASTE THE CONTENT HERE]
EOF
```

**Option 3: Using curl to download (if hosted)**
```bash
curl -o API37-Migration-Guide.md https://your-url-here.com/API37-Migration-Guide.md
```

**Option 4: Android Studio**
1. Right-click on your project root or docs folder
2. New → File
3. Name it `API37-Migration-Guide.md`
4. Paste the content
5. The markdown will render with formatting in Android Studio

---

## Additional Resources

- [Official Android 17 (API 37) Documentation](https://developer.android.com/about/versions/android-17)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [SMS Retriever API Guide](https://developers.google.com/identity/sms-retriever/overview)
- [Foreground Services Best Practices](https://developer.android.com/develop/background-work/foreground-services)
```