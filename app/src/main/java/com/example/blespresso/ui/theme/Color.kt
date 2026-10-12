package com.example.blespresso.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// TLDR: Theme palettes (docs/UX/README.md §2). One AppPalette per theme; the active one is
//       selected by `activePalette` below. All screens use the top-level getters, so
//       switching palette needs no call-site change. activePalette is Compose state: assigning
//       it at runtime recomposes every screen (hook for STATUS TODO #11, dark mode).
data class AppPalette(
    val background_indic: Color,
    val surface_indic: Color,
    val accent_indic: Color,
    val textprimary_indic: Color,
    val textsecondary_indic: Color,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val textprimary: Color,
    val textsecondary: Color,
    val textbox_background: Color,
    val textbox_text: Color,
    val icon_off: Color,
    val icon_heat: Color,
    val icon_ready: Color,
    val icon_glow: Color,
    val on_accent: Color,
    val statusbar: Color          // Android system status bar (top, clock/battery)
)

// former #1/#3
val PALETTE_CLASSIC = AppPalette(
    background_indic    = Color(0xFFEEEEEE),
    surface_indic       = Color(0xFFC3C8D3),
    accent_indic        = Color(0xFFEA9216),
    textprimary_indic   = Color(0xFF313841),
    textsecondary_indic = Color(0xFF3A4750),
    background          = Color(0xFFEEEEEE),
    surface             = Color(0xFFC3C8D3),
    accent              = Color(0xFFFFBD76),
    textprimary         = Color(0xFF313841),
    textsecondary       = Color(0xFF3A4750),
    textbox_background  = Color(0xFFC3C8D3),
    textbox_text        = Color(0xFF313841),
    icon_off            = Color(0x61313841),
    icon_heat           = Color(0xFFE07A1F),
    icon_ready          = Color(0xFF2E9E5B),
    icon_glow           = Color(0xFFEA9216),
    on_accent           = Color(0xFF313841),
    statusbar           = Color(0xFFC3C8D3)
)

// former #7, TODO #6
val PALETTE_GAGGIA_LIGHT = AppPalette(
    background_indic    = Color(0xFFF4EFE6),
    surface_indic       = Color(0xFFB8BCC2),
    accent_indic        = Color(0xFFC8102E),
    textprimary_indic   = Color(0xFF1A1A1A),
    textsecondary_indic = Color(0xFF4A4D52),
    background          = Color(0xFFF4EFE6),
    surface             = Color(0xFFDDDFE2),
    accent              = Color(0xFFC8102E),
    textprimary         = Color(0xFF1A1A1A),
    textsecondary       = Color(0xFF4A4D52),
    textbox_background  = Color(0xFFFFFFFF),
    textbox_text        = Color(0xFF1A1A1A),
    icon_off            = Color(0x611A1A1A),
    icon_heat           = Color(0xFFE07A1F),
    icon_ready          = Color(0xFF2E9E5B),
    icon_glow           = Color(0xFFC8102E),
    on_accent           = Color(0xFFFFFFFF),
    statusbar           = Color(0xFFDDDFE2)
)

// former #8, TODO #6
val PALETTE_GAGGIA_DARK = AppPalette(
    background_indic    = Color(0xFF121212),
    surface_indic       = Color(0xFF3A3D42),
    accent_indic        = Color(0xFFE53950),
    textprimary_indic   = Color(0xFFF2F2F2),
    textsecondary_indic = Color(0xFFB8BCC2),
    background          = Color(0xFF1A1A1A),
    surface             = Color(0xFF2A2C30),
    accent              = Color(0xFFE53950),
    textprimary         = Color(0xFFF2F2F2),
    textsecondary       = Color(0xFFB8BCC2),
    textbox_background  = Color(0xFF3A3D42),
    textbox_text        = Color(0xFFF2F2F2),
    icon_off            = Color(0x61F2F2F2),
    icon_heat           = Color(0xFFFF9A3D),
    icon_ready          = Color(0xFF4CC97E),
    icon_glow           = Color(0xFFE53950),
    on_accent           = Color(0xFFFFFFFF),
    statusbar           = Color(0xFF2A2C30)
)

// >>> THEME SWITCH: change this one line (PALETTE_CLASSIC / _GAGGIA_LIGHT / _GAGGIA_DARK) <<<
var activePalette by mutableStateOf(PALETTE_GAGGIA_LIGHT)

val background_indic   : Color get() = activePalette.background_indic
val surface_indic      : Color get() = activePalette.surface_indic
val accent_indic       : Color get() = activePalette.accent_indic
val textprimary_indic  : Color get() = activePalette.textprimary_indic
val textsecondary_indic: Color get() = activePalette.textsecondary_indic
val background         : Color get() = activePalette.background
val surface            : Color get() = activePalette.surface
val accent             : Color get() = activePalette.accent
val textprimary        : Color get() = activePalette.textprimary
val textsecondary      : Color get() = activePalette.textsecondary
val textbox_background : Color get() = activePalette.textbox_background
val textbox_text       : Color get() = activePalette.textbox_text
val icon_off           : Color get() = activePalette.icon_off
val icon_heat          : Color get() = activePalette.icon_heat
val icon_ready         : Color get() = activePalette.icon_ready
val icon_glow          : Color get() = activePalette.icon_glow
val on_accent          : Color get() = activePalette.on_accent
val statusbar          : Color get() = activePalette.statusbar

// REFERENCE PALETTES (not used, future color ideas) --------------------------------------
/*
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Cyan100 = Color(0xFFb2ebf2)
val Cyan800 = Color(0xFF00838F)
val Red500 = Color( 0xFF9e170c)
val Red850= Color(0xFF8f0049)
val Red900 = Color( 0xFF8f0c00)

val darkGray = Color(0xFF1a1717)
val darkGrayCompl50 =  Color(0xFFf5f9f9)
val darkGrayCompl100 =  Color(0xFFf4efef)
val darkGrayCompl200 =  Color(0xFFe4e9e9)
val darkGrayCompl400 =  Color(0xFFb0b4b4)
val darkBlue = Color(0xFF18192b)
val white = Color(0xFFF3F3F3)
val gray = Color(0xFF3F3F3F)
val blueGray = Color(0xFF404352)
val nightDark = Color(0xFF403757)

val purple = Color(0xFF9b11ba)
val orange = Color(0xFFdb660d)
val redOrange = Color(0xFFe84a23)
val green = Color(0xFF0ddb25)
val blue = Color(0xFF140ddb)
val brightBlue = Color(0xFF027cf5)

val colorButtonBox = Cyan800
val colorButtonText = white
val colorButtonCtrlText = blueGray
val color_1_3_BoxBackground = Color(0xFF404040)
val color_1_3_BoxText = white

val color_2_3_BoxTitleText = Color.Black
val color_2_3_BoxTitleDivider = Color.Black
val color_2_3_BoxBackground = darkGrayCompl400
val color_2_3_BoxText = nightDark// white
val color_2_3_BoxInputText =  white
val color_2_3_BoxInputBackground = gray.copy(alpha = 0.5f)

val color_3_3_BoxTitleText = Color.Black
val color_3_3_BoxTitleDivider = Color.Black
val color_3_3_BoxBackground = darkGrayCompl400
val color_3_3_BoxText = nightDark //white
val color_3_3_BoxInputText =  white
val color_3_3_BoxInputBackground = gray.copy(alpha = 0.5f)
*/

//Color palette #0
/*
val blue_background_indic= Color(0xFF6b7280)//Color(0xFF0F172A)
val blue_surface_indic= Color(0xFF334155)
val gold_accent1_indic= Color(0xFFD4AF37)
val gold_accent2_indic= Color(0xFFd97706)
val white_textprimary_indic= Color(0xFFF8FAFC)
val blue_textsecondary_indic= Color(0xFF94A3B8)

val blue_background= Color(0xFF6b7280)//Color(0xFF0F172A)
val blue_surface= Color(0xFF334155)
val gold_accent1= Color(0xFFD4AF37)
val gold_accent2= Color(0xFFd97706)
val white_textprimary= Color(0xFFF8FAFC)
val blue_textsecondary= Color(0xFF94A3B8)


*/

//Color palette #1
/*
val background_indic= Color(0xFFEEEEEE)
val surface_indic= Color(0xFFC3C8D3)
val accent_indic= Color(0xFFEA9216)
val textprimary_indic= Color(0xFF313841)
val textsecondary_indic= Color(0xFF3A4750)

val background= Color(0xFFEEEEEE)
val surface= Color(0xFFC3C8D3)
val accent= Color(0xFFEA9216)
val textprimary= Color(0xFF313841)
val textsecondary= Color(0xFF3A4750)
*/

//COLOR PALETTE #2
/*
val background_indic= Color(0xFFF9F7F0)
val surface_indic= Color(0xFFD8D2C2)
val accent_indic= Color(0xFFB17457)
val textprimary_indic= Color(0xFF4A4947)
val textsecondary_indic= Color(0xFF505457)

val background= Color(0xFFF9F7F0)
val surface= Color(0xFFD8D2C2)
val accent= Color(0xFFB17457)
val textprimary= Color(0xFF4A4947)
val textsecondary= Color(0xFF505457)
*/

//COLOR PALETTE #3
/*
val background_indic= Color(0xFFFFF6E9)
val surface_indic= Color(0xFFE0DED8)
val accent_indic= Color(0xFFFFBD76)
val textprimary_indic= Color(0xFF0A171D)
val textsecondary_indic= Color(0xFF232E34)

val background= Color(0xFFFFF6E9)
val surface= Color(0xFFE0DED8)
val accent= Color(0xFFFFBD76)
val textprimary= Color(0xFF0A171D)
val textsecondary= Color(0xFF232E34)
*/
//COLOR PALETTE #4
/*
val background_indic= Color(0xFFD9D3D1)
val surface_indic= Color(0xFF838585)
val accent_indic= Color(0xFFEC662D)
val textprimary_indic= Color(0xFF0B120E)
val textsecondary_indic= Color(0xFF2F3232)

val background= Color(0xFFD9D3D1)
val surface= Color(0xFF838585)
val accent= Color(0xFFEC662D)
val textprimary= Color(0xFF0B120E)
val textsecondary= Color(0xFF2F3232)
*/
//COLOR PALETTE #5
/*
val background_indic= Color(0xFFFAF3E1)
val surface_indic= Color(0xFFF5E7C6)
val accent_indic= Color(0xFFFF6D1F)
val textprimary_indic= Color(0xFF222222)
val textsecondary_indic= Color(0xFF4e4e4e)

val background= Color(0xFFFAF3E1)
val surface= Color(0xFFF5E7C6)
val accent= Color(0xFFFF6D1F)
val textprimary= Color(0xFF222222)
val textsecondary= Color(0xFF4e4e4e)
*/

//COLOR PALETTE #6
/*
val background_indic= Color(0xFFECF3F4)
val surface_indic= Color(0xFFC4DCDF)
val accent_indic= Color(0xFFFF893B)
val textprimary_indic= Color(0xFF465461)
val textsecondary_indic= Color(0xFF6b7681)

val background= Color(0xFFECF3F4)
val surface= Color(0xFFC4DCDF)
val accent= Color(0xFFFF893B)
val textprimary= Color(0xFF465461)
val textsecondary= Color(0xFF6b7681)
*/

//https://maketintsandshades.com/
