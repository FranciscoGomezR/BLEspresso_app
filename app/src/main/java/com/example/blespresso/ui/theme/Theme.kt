package com.example.blespresso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorPalette = darkColors(
    /*
    background = darkGray,
    surface = darkGray,
    onPrimary = darkGray,
    onSecondary = gray,
    onBackground = gray,
    onSurface = gray,

     */
)

private val LightColorPalette = lightColors(
    /*
    primary = gray,
    primaryVariant = gray,
    secondary = gray
*/
    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun BlespressoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit)
{
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    // Android status bar follows the active palette (Color.kt `statusbar`); dark icons on a
    // light bar, light icons on a dark bar. themes.xml only provides the launch color.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val barColor = statusbar
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = barColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                barColor.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}