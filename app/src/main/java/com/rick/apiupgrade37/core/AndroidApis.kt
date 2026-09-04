package com.rick.apiupgrade37.core

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Named SDK gates used throughout the sample.
 *
 * Dessert codes in this tree:
 * - [Build.VERSION_CODES.N] = 24 (this app's minSdk)
 * - [Build.VERSION_CODES.P] = 28
 * - [Build.VERSION_CODES.Q] = 29
 * - [Build.VERSION_CODES.R] = 30
 * - [Build.VERSION_CODES.S] = 31
 * - [Build.VERSION_CODES.TIRAMISU] = 33
 * - [Build.VERSION_CODES.UPSIDE_DOWN_CAKE] = 34
 * - [Build.VERSION_CODES.VANILLA_ICE_CREAM] = 35
 * - [Build.VERSION_CODES.BAKLAVA] = 36 (Android 16)
 * - [Build.VERSION_CODES.CINNAMON_BUN] = 37 (Android 17)
 */
object AndroidApis {
    /** Android 17 / API 37 dessert code. */
    const val ANDROID_17: Int = Build.VERSION_CODES.CINNAMON_BUN

    val deviceSdk: Int get() = Build.VERSION.SDK_INT

    /**
     * Lint understands this as an API 37 gate, so `if (isAndroid17) { newApi() }`
     * does not need a second `SDK_INT` check or a `TODO()` else-branch.
     */
    @get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.CINNAMON_BUN)
    val isAndroid17: Boolean get() = deviceSdk >= ANDROID_17

    fun sdkLabel(): String {
        val name = when {
            deviceSdk >= ANDROID_17 -> "Cinnamon Bun / Android 17"
            deviceSdk >= Build.VERSION_CODES.BAKLAVA -> "Baklava / Android 16"
            deviceSdk >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> "Vanilla Ice Cream / Android 15"
            deviceSdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> "Upside Down Cake / Android 14"
            deviceSdk >= Build.VERSION_CODES.TIRAMISU -> "Tiramisu / Android 13"
            else -> "API $deviceSdk"
        }
        return "device=$deviceSdk ($name), target=37, min=24"
    }
}
