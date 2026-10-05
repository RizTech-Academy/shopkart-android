package com.riztech.shopkart.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ShopKart's brand: warm walnut wood on cream, with a terracotta accent.
 *
 * Dynamic colour (Material You) is deliberately off. A storefront's colours are
 * part of how people recognise it and how its photos are framed; tinting the
 * whole shop from the user's wallpaper makes product photography sit on
 * unpredictable backgrounds.
 */
object BrandColors {
    /** Walnut. Named for its role, so a palette change touches only this file. */
    val Primary = Color(0xFF7A4B2A)
    val PrimaryDark = Color(0xFF5A3419)
    val PrimaryLight = Color(0xFFE2B98F)
    /** Terracotta. */
    val Accent = Color(0xFFD9653B)
    val Gold = Color(0xFFF2A900)
    val Mint = Color(0xFF3F7D4E)
    val Ink = Color(0xFF231A14)
    val Mist = Color(0xFFFAF5EE)
}

private val LightColors = lightColorScheme(
    primary = BrandColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E3D3),
    onPrimaryContainer = BrandColors.PrimaryDark,
    secondary = BrandColors.Accent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8DFD3),
    onSecondaryContainer = Color(0xFF6E2A12),
    tertiary = BrandColors.Mint,
    background = BrandColors.Mist,
    onBackground = BrandColors.Ink,
    surface = Color.White,
    onSurface = BrandColors.Ink,
    surfaceVariant = Color(0xFFF3ECE3),
    onSurfaceVariant = Color(0xFF6B5D52),
    surfaceContainerLow = Color(0xFFFCF9F5),
    surfaceContainer = Color(0xFFF5EEE6),
    outline = Color(0xFFDCCFC2),
    outlineVariant = Color(0xFFEDE4DA),
)

private val DarkColors = darkColorScheme(
    primary = BrandColors.PrimaryLight,
    onPrimary = Color(0xFF3F230F),
    primaryContainer = Color(0xFF5A3419),
    onPrimaryContainer = Color(0xFFF1E3D3),
    secondary = Color(0xFFF08A63),
    onSecondary = Color(0xFF5A1E08),
    tertiary = Color(0xFF8FC79B),
    background = Color(0xFF17120E),
    onBackground = Color(0xFFEDE3D9),
    surface = Color(0xFF221B15),
    onSurface = Color(0xFFEDE3D9),
    surfaceVariant = Color(0xFF30271F),
    onSurfaceVariant = Color(0xFFC9B8A8),
    surfaceContainerLow = Color(0xFF1C1611),
    surfaceContainer = Color(0xFF29211A),
    outline = Color(0xFF55473B),
    outlineVariant = Color(0xFF3A3028),
)

private val base = Typography()

private val ShopKartTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge,
    bodyMedium = base.bodyMedium,
    bodySmall = base.bodySmall,
)

private val ShopKartShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** A price, set large and heavy wherever it is the point of the screen. */
val PriceStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = (-0.25).sp)

@Composable
fun ShopKartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ShopKartTypography,
        shapes = ShopKartShapes,
        content = content,
    )
}
