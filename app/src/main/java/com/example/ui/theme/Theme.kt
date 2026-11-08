package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = Color(0xFF0F261C),
    primaryContainer = DarkSageContainer,
    onPrimaryContainer = Color(0xFFC7EBD5),
    secondary = DarkGoldSecondary,
    onSecondary = Color(0xFF3B2F0B),
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = Color(0xFFFFECC4),
    tertiary = SageGreenLight,
    onTertiary = Color(0xFF0A2216),
    background = CharcoalDarkBackground,
    onBackground = TextPrimaryDark,
    surface = CharcoalDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CharcoalDarkBorder,
    outlineVariant = Color(0xFF332F28)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPineGreen,
    onPrimary = Color.White,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = DeepPineGreenDark,
    secondary = AntiqueGold,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = AntiqueGoldDark,
    tertiary = DeepEspressoBrown,
    onTertiary = Color.White,
    tertiaryContainer = WarmBrownContainer,
    onTertiaryContainer = DeepEspressoBrown,
    background = CreamBeigeBackground,
    onBackground = TextPrimaryLight,
    surface = WarmBeigeSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = WarmBeigeSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFDCD2C3),
    outlineVariant = Color(0xFFEDE4D6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Kurdish Sorani is written Right-to-Left (RTL)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
