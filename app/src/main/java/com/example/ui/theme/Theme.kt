package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SaffronGold,
    secondary = EmeraldTeal,
    tertiary = LighterSurface,
    background = SlateBackground,
    surface = DarkSurface,
    onPrimary = SlateBackground,
    onSecondary = SlateBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = DarkSaffron,
    secondary = EmeraldTeal,
    tertiary = LighterSurface,
    background = SlateBackground, // Keep slate theme consistent
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for terminal dashboard aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors so our brand style is preserved
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
