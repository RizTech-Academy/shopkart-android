package com.riztech.shopkart.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFF4DB6AC)
private val Amber = Color(0xFFFFA000)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = Amber,
    tertiary = TealLight,
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    secondary = Amber,
    tertiary = Teal,
)

@Composable
fun ShopKartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You on Android 12+. Honouring the user's wallpaper palette costs
    // nothing and is what the platform expects; the brand colours remain the
    // fallback everywhere else.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
