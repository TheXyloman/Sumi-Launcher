package com.sumilauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF171717)
private val SumiColors = lightColorScheme(
    primary = Ink, onPrimary = Color.White,
    secondary = Color(0xFF414141), onSecondary = Color.White,
    background = Color.White, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color.White, onSurfaceVariant = Color(0xFF414141),
    outline = Ink, outlineVariant = Color(0xFF747474),
    error = Ink, onError = Color.White, surfaceTint = Color.White,
)
private val SumiTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 52.sp, lineHeight = 60.sp, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 43.sp, letterSpacing = (-0.7).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp),
)
@Composable
fun SumiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SumiColors, typography = SumiTypography, content = content)
}
