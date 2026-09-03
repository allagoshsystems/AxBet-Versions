package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AxbetColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    background = BackgroundDark,
    surface = CardDark,
    onPrimary = ForegroundDark,
    onBackground = ForegroundDark,
    onSurface = ForegroundDark,
    error = DestructiveRed,
    surfaceVariant = AccentDark,
    outline = BorderDark,
    secondary = PrimaryBlue
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AxbetColorScheme,
        typography = Typography,
        content = content
    )
}
