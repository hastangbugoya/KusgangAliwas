package com.example.kusgangaliwas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

internal val DarkColorScheme = darkColorScheme(
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

    outline = KaPalette.Outline,
    outlineVariant = KaPalette.OutlineMuted,

    error = KaPalette.Danger,
    onError = Color.White,
    errorContainer = KaPalette.DangerContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

// Temporary: keep light scheme pointing to the same visual identity.
// Later we can build a true light theme if needed.
internal val LightColorScheme = DarkColorScheme

@Composable
fun KusgangAliwasTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = KaDarkColorScheme

    val workoutColors = KaWorkoutColors(
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

    CompositionLocalProvider(
        LocalKaWorkoutColors provides workoutColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}