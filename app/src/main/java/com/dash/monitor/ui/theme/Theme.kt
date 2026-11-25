package com.dash.monitor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val ink: Color,
    val inkSecondary: Color,
    val hairline: Color,
    val invertedBg: Color,
    val invertedInk: Color
)

private val DarkDashPalette = DashPalette(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceElevated = Color(0xFF141414),
    ink = Color(0xFFFFFFFF),
    inkSecondary = Color(0xFF8C8C8C),
    hairline = Color(0xFF262626),
    invertedBg = Color(0xFFFFFFFF),
    invertedInk = Color(0xFF000000)
)

private val LightDashPalette = DashPalette(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFAFAFA),
    surfaceElevated = Color(0xFFF2F2F2),
    ink = Color(0xFF000000),
    inkSecondary = Color(0xFF666666),
    hairline = Color(0xFFE2E2E2),
    invertedBg = Color(0xFF000000),
    invertedInk = Color(0xFFFFFFFF)
)

val LocalDashPalette = staticCompositionLocalOf { DarkDashPalette }

object DashSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
    val hero = 64.dp
}

private val DashTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    )
)

val TabularMetricStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum"
)

@Composable
fun DashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkDashPalette else LightDashPalette
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.ink,
            onPrimary = palette.invertedInk,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.inkSecondary,
            outline = palette.hairline
        )
    } else {
        lightColorScheme(
            primary = palette.ink,
            onPrimary = palette.invertedInk,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.inkSecondary,
            outline = palette.hairline
        )
    }

    CompositionLocalProvider(LocalDashPalette provides palette) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = DashTypography,
            content = content
        )
    }
}
