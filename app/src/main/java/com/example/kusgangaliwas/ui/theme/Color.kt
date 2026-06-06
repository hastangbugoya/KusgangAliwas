package com.example.kusgangaliwas.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object KaPalette {
    // Base
    val IronBlack = Color(0xFF0B0D10)
    val IronBackground = Color(0xFF101318)
    val IronSurface = Color(0xFF171B22)
    val IronSurfaceHigh = Color(0xFF202632)
    val IronSurfaceHighest = Color(0xFF2A313D)

    // Text
    val TextPrimary = Color(0xFFF2F4F7)
    val TextSecondary = Color(0xFFB4BBC6)
    val TextMuted = Color(0xFF7D8592)

    // Lines / borders
    val Outline = Color(0xFF4B5563)
    val OutlineMuted = Color(0xFF303743)

    // Brand / accents
    val Ember = Color(0xFFFF5A1F)
    val EmberDark = Color(0xFF8F250C)
    val EmberContainer = Color(0xFF451104)
    val OnEmber = Color(0xFF1A0500)

    val Amber = Color(0xFFFFB020)
    val AmberContainer = Color(0xFF3F2B00)

    val SteelBlue = Color(0xFF4EA3FF)
    val SteelBlueContainer = Color(0xFF0E2A45)

    val Success = Color(0xFF39D98A)
    val SuccessContainer = Color(0xFF073D27)

    val Danger = Color(0xFFFF453A)
    val DangerContainer = Color(0xFF4A0D0A)

    val Purple = Color(0xFFB78CFF)
    val PurpleContainer = Color(0xFF2B1748)
}
// Existing screen aliases
val MissedSessionsRed = KaPalette.Danger
val PartialYellow = KaPalette.Amber
val SuccessGreen = KaPalette.Success


internal val KaDarkColorScheme = darkColorScheme(
    primary = KaPalette.Ember,
    onPrimary = KaPalette.OnEmber,
    primaryContainer = KaPalette.EmberContainer,
    onPrimaryContainer = Color(0xFFFFD5C8),

    secondary = KaPalette.Amber,
    onSecondary = Color(0xFF241900),
    secondaryContainer = KaPalette.AmberContainer,
    onSecondaryContainer = Color(0xFFFFE2A8),

    tertiary = KaPalette.SteelBlue,
    onTertiary = Color(0xFF001D35),
    tertiaryContainer = KaPalette.SteelBlueContainer,
    onTertiaryContainer = Color(0xFFD4E9FF),

    background = KaPalette.IronBackground,
    onBackground = KaPalette.TextPrimary,

    surface = KaPalette.IronSurface,
    onSurface = KaPalette.TextPrimary,

    surfaceVariant = KaPalette.IronSurfaceHigh,
    onSurfaceVariant = KaPalette.TextSecondary,

    surfaceContainerLowest = KaPalette.IronBlack,
    surfaceContainerLow = KaPalette.IronSurface,
    surfaceContainer = KaPalette.IronSurfaceHigh,
    surfaceContainerHigh = KaPalette.IronSurfaceHighest,
    surfaceContainerHighest = Color(0xFF343B49),

    outline = KaPalette.Outline,
    outlineVariant = KaPalette.OutlineMuted,

    error = KaPalette.Danger,
    onError = Color.White,
    errorContainer = KaPalette.DangerContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

data class KaWorkoutColors(
    val inProgress: Color,
    val inProgressContainer: Color,
    val planned: Color,
    val plannedContainer: Color,
    val cycle: Color,
    val cycleContainer: Color,
    val completed: Color,
    val completedContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val muted: Color
)

val LocalKaWorkoutColors = staticCompositionLocalOf {
    KaWorkoutColors(
        inProgress = KaPalette.Ember,
        inProgressContainer = KaPalette.EmberContainer,
        planned = KaPalette.SteelBlue,
        plannedContainer = KaPalette.SteelBlueContainer,
        cycle = KaPalette.Purple,
        cycleContainer = KaPalette.PurpleContainer,
        completed = KaPalette.Success,
        completedContainer = KaPalette.SuccessContainer,
        danger = KaPalette.Danger,
        dangerContainer = KaPalette.DangerContainer,
        muted = KaPalette.TextMuted
    )
}

object KaTheme {
    val workoutColors: KaWorkoutColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKaWorkoutColors.current
}