package com.example.levelup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = BackgroundBlack,
    secondary = NeonBlue,
    background = BackgroundBlack,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    onBackground = TextSecondary,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    onPrimary = BackgroundBlack,
    secondary = NeonBlue,
    background = TextPrimary,
    surface = Color(0xFFF5F5F5),
    onSurface = BackgroundBlack,
    onBackground = BackgroundBlack,
    error = ErrorRed
)

@Composable
fun LevelUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LevelUpTypography,
        shapes = Shapes(
            small = RoundedCornerShape(6.dp),
            medium = RoundedCornerShape(10.dp),
            large = RoundedCornerShape(14.dp)
        ),
        content = content
    )
}
