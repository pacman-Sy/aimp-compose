package com.aimp.compose.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Exact AIMP palette decoded from the official APK
private val AimpOrange = Color(0xFFDC8626)
private val AimpOrangeDark = Color(0xFFC87100)
private val AimpDarkBg = Color(0xFF212121)
private val AimpDarkSurface = Color(0xFF2B2B2B)
private val AimpText = Color(0xFFFFFFFF)
private val AimpTextDim = Color(0xFFBEBEBE)

private val DarkAimpColors = darkColorScheme(
    primary = AimpOrange,
    onPrimary = Color.White,
    secondary = AimpOrangeDark,
    background = AimpDarkBg,
    surface = AimpDarkSurface,
    onBackground = AimpText,
    onSurface = AimpText,
    onSurfaceVariant = AimpTextDim,
    outline = Color(0xFF3A3A3A)
)

private val LightAimpColors = lightColorScheme(
    primary = AimpOrange,
    onPrimary = Color.White,
    secondary = AimpOrangeDark,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFDDDDDD)
)

@Composable
fun AimpTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkAimpColors else LightAimpColors,
        content = content
    )
}
