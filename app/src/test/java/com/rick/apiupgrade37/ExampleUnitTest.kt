package com.rick.apiupgrade37

import com.rick.apiupgrade37.core.AndroidApis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun android17DessertCodeIsApi37() {
        assertEquals(37, AndroidApis.ANDROID_17)
    }

    @Test
    fun catalogIsNotEmpty() {
        assertTrue(com.rick.apiupgrade37.ui.catalogItems.size >= 15)
    }
}
