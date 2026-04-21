package com.sumilauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SumiColors = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color.White,
    secondary = Color(0xFF2B2B2B),
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFF333333),
)

@Composable
fun SumiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SumiColors,
        content = content,
    )
}
