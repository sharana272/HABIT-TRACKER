package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeMode

data class ExtendedColors(
    val cardBackground: Color,
    val cardBorder: Color,
    val surfaceRaised: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentGlow: Color,
    val streakBadgeBg: Color,
    val streakBadgeText: Color,
    val checkRingBorder: Color,
    val checkRingFill: Color,
    val checkRingIcon: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        cardBackground = ObsidianSurface,
        cardBorder = ObsidianStrokeStructural,
        surfaceRaised = ObsidianSurfaceRaised,
        textPrimary = ObsidianTextPrimary,
        textSecondary = ObsidianTextSecondary,
        textMuted = ObsidianTextMuted,
        accentGlow = EmeraldPrimary,
        streakBadgeBg = ObsidianSurfaceRaised,
        streakBadgeText = MintSecondary,
        checkRingBorder = ObsidianStrokeStructural,
        checkRingFill = EmeraldPrimary,
        checkRingIcon = ObsidianBase
    )
}

val ObsidianColorScheme: ColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = ObsidianBase,
    primaryContainer = DeepEmeraldTertiary,
    onPrimaryContainer = ObsidianTextPrimary,
    secondary = MintSecondary,
    onSecondary = ObsidianBase,
    secondaryContainer = ObsidianSurfaceRaised,
    onSecondaryContainer = MintSecondary,
    tertiary = MintSecondary,
    onTertiary = ObsidianBase,
    background = ObsidianBase,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceRaised,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianStrokeStructural,
    outlineVariant = ObsidianStrokeSubtle
)

val OrganicColorScheme: ColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = FoliageSecondary,
    onPrimaryContainer = Color.White,
    secondary = FoliageSecondary,
    onSecondary = Color.White,
    secondaryContainer = OrganicSageTint,
    onSecondaryContainer = ForestPrimary,
    tertiary = MutedSageTertiary,
    onTertiary = Color.White,
    background = OrganicCanvas,
    onBackground = OrganicTextPrimary,
    surface = OrganicCardSurface,
    onSurface = OrganicTextPrimary,
    surfaceVariant = OrganicSageTint,
    onSurfaceVariant = OrganicTextSecondary,
    outline = OrganicBorderStructural,
    outlineVariant = OrganicBorderSage
)

@Composable
fun RulzHabitTheme(
    themeMode: ThemeMode = ThemeMode.OBSIDIAN_DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.OBSIDIAN_DARK -> true
        ThemeMode.REFINED_LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) ObsidianColorScheme else OrganicColorScheme
    val extendedColors = if (isDark) {
        ExtendedColors(
            cardBackground = ObsidianSurface,
            cardBorder = ObsidianStrokeStructural,
            surfaceRaised = ObsidianSurfaceRaised,
            textPrimary = ObsidianTextPrimary,
            textSecondary = ObsidianTextSecondary,
            textMuted = ObsidianTextMuted,
            accentGlow = EmeraldPrimary,
            streakBadgeBg = ObsidianSurfaceRaised,
            streakBadgeText = MintSecondary,
            checkRingBorder = ObsidianStrokeStructural,
            checkRingFill = EmeraldPrimary,
            checkRingIcon = ObsidianBase
        )
    } else {
        ExtendedColors(
            cardBackground = OrganicCardSurface,
            cardBorder = OrganicBorderStructural,
            surfaceRaised = OrganicSageTint,
            textPrimary = OrganicTextPrimary,
            textSecondary = OrganicTextSecondary,
            textMuted = OrganicTextMuted,
            accentGlow = ForestPrimary,
            streakBadgeBg = OrganicSageTint,
            streakBadgeText = ForestPrimary,
            checkRingBorder = OrganicBorderSage,
            checkRingFill = ForestPrimary,
            checkRingIcon = Color.White
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
