package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantBlueAccent,
    secondary = ElegantCrimsonRed,
    tertiary = ElegantSlate900,
    background = ElegantSlate950,
    surface = ElegantSlate900,
    onPrimary = ElegantLightBackground,
    onSecondary = ElegantLightBackground,
    onBackground = ElegantLightBackground,
    onSurface = ElegantLightBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElegantSlate900,
    secondary = ElegantCrimsonRed,
    tertiary = ElegantBlueAccent,
    background = ElegantLightBackground,
    surface = ElegantWhiteSurface,
    onPrimary = ElegantWhiteSurface,
    onSecondary = ElegantWhiteSurface,
    onBackground = ElegantSlate900,
    onSurface = ElegantSlate900
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default to false to showcase the premium brand design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
