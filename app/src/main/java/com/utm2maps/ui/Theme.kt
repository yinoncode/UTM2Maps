package com.utm2maps.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UtmColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF08210B),
    tertiary = Color(0xFF00695C),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF172033),
    surface = Color.White,
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFE8EEF7),
    onSurfaceVariant = Color(0xFF3E4858),
    error = Color(0xFFB3261E)
)

@Composable
fun Utm2MapsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UtmColorScheme,
        content = content
    )
}
