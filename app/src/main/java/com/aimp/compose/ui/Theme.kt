package com.aimp.compose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFFF8800)
private val DarkBg = Color(0xFF1E1E1E)
private val DarkSurface = Color(0xFF2A2A2A)

private val DarkAimpColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Orange,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightAimpColors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Orange,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1E1E1E),
    onSurface = Color(0xFF1E1E1E)
)

@Composable
fun AimpTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkAimpColors else LightAimpColors,
        content = content
    )
}
