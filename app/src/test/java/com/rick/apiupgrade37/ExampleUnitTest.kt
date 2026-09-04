package com.rick.apiupgrade37

import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.ui.catalogItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local JVM tests. These run on the desktop JVM against the stubbed android.jar, so they
 * can only touch code that does not call into the framework at runtime.
 *
 * That limit is worth understanding before writing more of them, because Android 17
 * tightened it. Test code has long reached for reflection to force
 * [android.os.Build.VERSION.SDK_INT] to a chosen value so a version-gated branch could be
 * exercised on the JVM:
 *
 *     // Throws IllegalAccessException on API 37 — do not revive this.
 *     Build.VERSION::class.java.getField("SDK_INT")
 *         .apply { isAccessible = true }
 *         .setInt(null, 24)
 *
 * `static final` fields are genuinely final when targeting 37: reflection throws and the
 * JNI SetStatic*Field family crashes the process. The supported replacements are
 * Robolectric, which models the SDK level properly, or an injectable wrapper.
 *
 * [AndroidApis] is that wrapper's first step. It reads SDK_INT behind a property, so a
 * future version can take the level as a constructor argument and become testable without
 * any reflection at all. Its constants are compile-time values, which is why the first
 * test below works on the JVM while `AndroidApis.isAndroid17` would not.
 */
class ExampleUnitTest {

    /**
     * Guards against a rename or a bad merge silently repointing the project at the wrong
     * platform. CINNAMON_BUN is 37; if this fails, the compileSdk and the constant have
     * drifted apart.
     */
    @Test
    fun android17DessertCodeIsApi37() {
        assertEquals(37, AndroidApis.ANDROID_17)
    }

    /**
     * The catalog is a plain list with no framework dependency, so it is one of the few
     * things here that can be asserted on the JVM. A lower bound rather than an exact
     * count, so that adding a sample does not fail the build.
     */
    @Test
    fun catalogIsNotEmpty() {
        assertTrue(catalogItems.size >= 15)
    }

    /**
     * Every card has to reach a screen. The router in Api37App matches on these strings,
     * and a duplicate would make one of the two entries unreachable — a failure with no
     * compile-time signal, since the `when` simply matches the first branch.
     */
    @Test
    fun catalogRoutesAreUnique() {
        val routes = catalogItems.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }
}
