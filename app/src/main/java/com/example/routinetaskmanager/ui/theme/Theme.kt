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
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = EerieBlack,
    onPrimary = White,
    secondary = Color(0xFF71787E),
    surface = Color(0xFFF0F0FC),
    tertiary = Color(0xFFEFF3A2),
    onSurface = Color(0xFF212121),
    surfaceDim = Color(0xFFE8E6F2),
    primaryContainer = Color(0xFFD7DDFC),
    onPrimaryContainer = Color(0xFF384956),
    secondaryContainer = Color(0xFFCED3E9),
    onSecondaryContainer = Color(0xFF212121),
    outline = Color(0xFF71787E),
    tertiaryContainer = Color(0xFFCFDECB)


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun RoutineTaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}