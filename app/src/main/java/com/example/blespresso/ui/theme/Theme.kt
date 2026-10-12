package com.example.blespresso.ui.theme

import android.app.Activity
import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// TLDR: Whole app follows `activePalette` (Color.kt, STATUS TODO #11). MaterialTheme colors are
//       built from the active palette on every composition, so unstyled Text / Button / Surface
//       pick palette colors too. Root Surface gives every screen the palette background and
//       LocalContentColor gives default text `textprimary`. System dark mode is ignored by design.
//       Exceptions (fixed colors): dial >105 °C red band, transparent end of the status glow.
@Composable
fun BlespressoAppTheme(content: @Composable () -> Unit) {
    val colors = Colors(
        primary = accent,
        primaryVariant = accent,
        secondary = accent_indic,
        secondaryVariant = accent_indic,
        background = background,
        surface = surface,
        error = Color(0xFFB00020),
        onPrimary = on_accent,
        onSecondary = on_accent,
        onBackground = textprimary,
        onSurface = textprimary,
        onError = Color.White,
        isLight = background.luminance() > 0.5f
    )

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
        shapes = Shapes
    ) {
        Surface(color = background) {
            CompositionLocalProvider(LocalContentColor provides textprimary) {
                content()
            }
        }
    }
}
