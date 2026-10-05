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
 * ShopKart's brand: deep indigo with a warm coral accent.
 *
 * Dynamic colour (Material You) is deliberately off. A storefront's colours are
 * part of how people recognise it and how its photos are framed; tinting the
 * whole shop from the user's wallpaper makes product photography sit on
 * unpredictable backgrounds.
 */
object BrandColors {
    val Indigo = Color(0xFF3B2FD9)
    val IndigoDark = Color(0xFF2A1FB0)
    val IndigoLight = Color(0xFFB9B3FF)
    val Coral = Color(0xFFFF6B4A)
    val Gold = Color(0xFFFFB400)
    val Mint = Color(0xFF12A36B)
    val Ink = Color(0xFF14121F)
    val Mist = Color(0xFFF5F4FB)
}

private val LightColors = lightColorScheme(
    primary = BrandColors.Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E3FF),
    onPrimaryContainer = BrandColors.IndigoDark,
    secondary = BrandColors.Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4DC),
    onSecondaryContainer = Color(0xFF7A2410),
    tertiary = BrandColors.Mint,
    background = BrandColors.Mist,
    onBackground = BrandColors.Ink,
    surface = Color.White,
    onSurface = BrandColors.Ink,
    surfaceVariant = Color(0xFFEFEDF7),
    onSurfaceVariant = Color(0xFF5E5A72),
    surfaceContainerLow = Color(0xFFF8F7FD),
    surfaceContainer = Color(0xFFF1F0F8),
    outline = Color(0xFFD4D1E3),
    outlineVariant = Color(0xFFE7E5F1),
)

private val DarkColors = darkColorScheme(
    primary = BrandColors.IndigoLight,
    onPrimary = Color(0xFF1B1080),
    primaryContainer = Color(0xFF2F25A8),
    onPrimaryContainer = Color(0xFFE6E3FF),
    secondary = Color(0xFFFF8F73),
    onSecondary = Color(0xFF5C1A0A),
    tertiary = Color(0xFF5BD6A2),
    background = Color(0xFF0F0E17),
    onBackground = Color(0xFFE8E6F2),
    surface = Color(0xFF191826),
    onSurface = Color(0xFFE8E6F2),
    surfaceVariant = Color(0xFF26243A),
    onSurfaceVariant = Color(0xFFB9B5CC),
    surfaceContainerLow = Color(0xFF16151F),
    surfaceContainer = Color(0xFF201F2E),
    outline = Color(0xFF45425C),
    outlineVariant = Color(0xFF2E2C42),
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
