package com.example.routinetaskmanager.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = EerieBlack,
    onPrimary = White,

    background = Color(0xFF101115),
    onBackground = Color(0xFFE9ECF2),

    surface = Color(0xFF181A20),
    surfaceDim = Color(0xFF121319),
    surfaceBright = Color(0xFF2B2E37),

    surfaceContainerLowest = Color(0xFF0D0E12),
    surfaceContainerLow = Color(0xFF161920),
    surfaceContainer = Color(0xFF1B1E26),
    surfaceContainerHigh = Color(0xFF222633),
    surfaceContainerHighest = Color(0xFF292D38),

    onSurface = Color(0xFFE9ECF2),
    onSurfaceVariant = Color(0xFFB7BECC),

    outline = Color(0xFF8A92A3),
    outlineVariant = Color(0xFF444C5D),

    secondary = Color(0xFFA29CBE),
    onSecondary = Color(0xFF262237),

    secondaryContainer = Color(0xFF425282),
    onSecondaryContainer = Color(0xFFEAF0FF),

    tertiary = Color(0xFFD1D983),
    onTertiary = Color(0xFF222319),

    tertiaryContainer = Color(0xFF53634C),
    onTertiaryContainer = Color(0xFFEAF5E4),

    primaryContainer = Color(0xFF3F4F7D),
    onPrimaryContainer = Color(0xFFEAF0FF),

    inverseSurface = Color(0xFFE5E7EC),
    inverseOnSurface = Color(0xFF202228),
    inversePrimary = EerieBlack,

    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = EerieBlack,
    onPrimary = White,

    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF6F5FA),
    surfaceDim = Color(0xFFE8E6F2),
    surfaceBright = Color(0xFFF7F9FE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4F9),
    surfaceContainer = Color(0xFFEEECF5),
    surfaceContainerHigh = Color(0xFFE5E8ED),
    surfaceContainerHighest = Color(0xFFF8F8F8),

    onSurface = Color(0xFF212121),
    onSurfaceVariant = Color(0xFF41474D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFD7DDE9),

    secondary = Color(0xFF9490AC),
    secondaryContainer = Color(0xFFA3B1EE),
    onSecondaryContainer = Color(0xFF00174A),

    tertiary = Color(0xFFEFF3A2),
    onTertiary = Color(0xFF212121),
    tertiaryContainer = Color(0xFFCFDECB),

    primaryContainer = Color(0xFFE1E6FF),
    onPrimaryContainer = Color(0xFF384956),

    surfaceTint = Color.Transparent
)

@Composable
fun RoutineTaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        //darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}