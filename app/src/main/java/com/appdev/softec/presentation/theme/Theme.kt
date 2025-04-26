package com.appdev.softec.presentation.theme

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


val DarkColorScheme = darkColorScheme(
    primary = Color.Black,
    tertiary = Color(0xFF8572EB),
    background = backColor,
    surfaceTint = Color.White,
    secondary = Color(0xff7B70FF),
    outlineVariant = Color(0xff3C3F48),
    surfaceVariant = Color(0xff423D46),
    onSurfaceVariant = Color(0xff2A2A2C),
    onBackground = elementBack,
    secondaryContainer = Color(0xff2F333E),
    inverseOnSurface = Color(0xff24272E),
    inverseSurface = Color(0xffA6ACB5),
)

val LightColorScheme = lightColorScheme(
    onBackground = Color.White,
    primary = Color.White,
    tertiary = Color.Black.copy(alpha = 0.9f),
    surface = Color.White,
    surfaceTint = Color.Black.copy(alpha = 0.9f),
    secondary = Color(0xff7B70FF),
    background = Color(0xffF4F5FA),
    outlineVariant = Color(0xff3C3F48),
    surfaceVariant = Color.White,
    onSurfaceVariant = Color.Blue.copy(alpha = 0.1f),
    secondaryContainer = Color(0xffE5EBF7),
    inverseOnSurface = Color(0xffE5EBF7),
    inverseSurface = Color.Gray

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
fun SoftecTheme(
    isDarkMode: Boolean,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkMode -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
