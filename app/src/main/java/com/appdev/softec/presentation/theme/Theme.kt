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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.presentation.feature.setting.CustomizationViewModel


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xff00DDC1),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black,
    onBackground = Color.White,
    primaryContainer = Color(0xff004F45),
    inversePrimary = Color(0xffB5CAC6),
    surface = Color.Black.copy(alpha = 0.8f),
    surfaceVariant = Color(0xff31363F),
    surfaceTint = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xff016A5F),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    onBackground = Color.Black,
    primaryContainer = Color(0x5A00BCD4),
    inversePrimary = Color(0xff394948),
    surface = Color.White,
    surfaceVariant = Color(0xffEEEEEE),
    surfaceTint = Color.White
)


@Composable
fun SoftecTheme(
    viewModel: CustomizationViewModel = hiltViewModel(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkMode by viewModel.customizationState.collectAsState()
    val colorScheme = when {
        isDarkMode.isDarkMode -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
