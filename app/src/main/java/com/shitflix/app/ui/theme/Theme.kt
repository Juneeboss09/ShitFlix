package com.shitflix.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NetflixRed = Color(0xFFE50914)
val NetflixDark = Color(0xFF141414)
val NetflixCard = Color(0xFF1F1F1F)
val NetflixMuted = Color(0xFFB3B3B3)

private val DarkScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    background = NetflixDark,
    onBackground = Color.White,
    surface = NetflixCard,
    onSurface = Color.White,
    secondary = NetflixMuted,
    onSecondary = Color.Black,
)

private val NetflixTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 40.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = NetflixMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)

@Composable
fun ShitFlixTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE")
    val dark = isSystemInDarkTheme() // theme is always dark, but kept for future light variant
    MaterialTheme(colorScheme = DarkScheme, typography = NetflixTypography, content = content)
}
