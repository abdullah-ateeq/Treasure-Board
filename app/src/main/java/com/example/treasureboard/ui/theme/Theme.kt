package com.example.treasureboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.treasureboard.data.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1E160A),
    primaryContainer = Color(0xFF3A2E17),
    onPrimaryContainer = GoldLight,
    secondary = TealLight,
    onSecondary = Color(0xFF06221F),
    tertiary = CrimsonLight,
    background = Forest900,
    onBackground = Parchment,
    surface = Forest800,
    onSurface = Parchment,
    surfaceVariant = Forest700,
    onSurfaceVariant = ParchmentMuted,
    surfaceContainerHigh = Forest700,
    outline = Color(0xFF55624F),
    outlineVariant = Forest600,
    error = Color(0xFFE08A70)
)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color(0xFFFFF8EA),
    primaryContainer = Color(0xFFEBD9AE),
    onPrimaryContainer = Color(0xFF3A2A0E),
    secondary = TealBase,
    onSecondary = Color.White,
    tertiary = CrimsonBase,
    background = Linen,
    onBackground = WoodInk,
    surface = LinenSurface,
    onSurface = WoodInk,
    surfaceVariant = LinenVariant,
    onSurfaceVariant = WoodMuted,
    surfaceContainerHigh = LinenVariant,
    outline = Color(0xFFB5A382),
    outlineVariant = Color(0xFFD8C9A8),
    error = CrimsonBase
)

data class Backdrop(val brush: Brush, val isDark: Boolean)

val LocalBackdrop = staticCompositionLocalOf {
    Backdrop(Brush.verticalGradient(listOf(Forest800, Forest950)), true)
}

@Composable
fun isAppInDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun TreasureBoardTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val backdrop = if (darkTheme) {
        Backdrop(Brush.verticalGradient(listOf(Color(0xFF1A2721), Forest900, Forest950)), true)
    } else {
        Backdrop(Brush.verticalGradient(listOf(Color(0xFFF6EFDF), Linen, Color(0xFFE6DAC1))), false)
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalBackdrop provides backdrop) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
