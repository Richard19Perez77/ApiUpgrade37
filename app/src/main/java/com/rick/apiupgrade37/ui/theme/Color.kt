package com.rick.apiupgrade37.ui.theme

import androidx.compose.ui.graphics.Color

// Fallback palette for API 24–30, where dynamic color does not exist. On API 31 and later
// these are unused unless the caller passes dynamicColor = false to ApiUpgrade37Theme.
// The 80/40 suffixes are Material tonal-palette luminance steps: 80 reads on a dark
// background, 40 on a light one.
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)