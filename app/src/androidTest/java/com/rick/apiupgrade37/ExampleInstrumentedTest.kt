package com.rick.apiupgrade37

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rick.apiupgrade37.core.AndroidApis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests. These run on a real device or emulator, so unlike the local JVM
 * tests they see the actual platform and a real SDK level.
 *
 * That makes this the right place to assert anything version-dependent, and it is the
 * honest way to test an API 37 gate: run the suite on an Android 17 image and on an older
 * one, and let the device supply the SDK level. Do not try to force the level from inside
 * a test — see the note in ExampleUnitTest about static final fields being frozen when
 * targeting 37.
 *
 * Note also that a test which pokes at Looper internals needs
 * TestLooperManager.peekWhen()/poll() rather than reflection into MessageQueue, because
 * the queue is lock-free under target 37 and its private fields have changed shape.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rick.apiupgrade37", appContext.packageName)
    }

    /**
     * Confirms the gate agrees with the device it is running on, rather than asserting a
     * fixed answer — this has to pass on both an Android 17 image and an older one.
     * A mismatch here would mean @ChecksSdkIntAtLeast is advertising a threshold that the
     * property does not actually implement, which would make lint trust a broken guard.
     */
    @Test
    fun isAndroid17MatchesTheDeviceItRunsOn() {
        val deviceIsAtLeast37 = android.os.Build.VERSION.SDK_INT >= 37
        assertEquals(deviceIsAtLeast37, AndroidApis.isAndroid17)
    }

    /**
     * The label is shown at the top of every feature screen, so an empty or malformed one
     * would be visible everywhere at once.
     */
    @Test
    fun sdkLabelDescribesTheDevice() {
        val label = AndroidApis.sdkLabel()
        assertTrue(label, label.contains("device=${android.os.Build.VERSION.SDK_INT}"))
        assertTrue(label, label.contains("target=37"))
    }
}
