package com.example.blespresso.ui.theme

import android.content.Context

// TLDR: In-app light/dark theme choice (STATUS TODO #13). System dark mode is ignored.
//       Stored as a palette key (not a Boolean) so more palettes can be added without
//       migration; missing / unknown key -> Gaggia light. Read once in MainActivity.onCreate
//       (before setContent, so the first frame is already in the saved theme); written with
//       apply() on every toggle.
object ThemePrefs {
    private const val PREFS_FILE = "ui_prefs"
    private const val KEY_PALETTE = "palette"
    private const val VALUE_LIGHT = "gaggia_light"
    private const val VALUE_DARK = "gaggia_dark"

    val isDark: Boolean
        get() = activePalette == PALETTE_GAGGIA_DARK

    fun load(context: Context) {
        val key = prefs(context).getString(KEY_PALETTE, VALUE_LIGHT)
        activePalette = if (key == VALUE_DARK) PALETTE_GAGGIA_DARK else PALETTE_GAGGIA_LIGHT
    }

    fun toggle(context: Context) {
        val toDark = !isDark
        activePalette = if (toDark) PALETTE_GAGGIA_DARK else PALETTE_GAGGIA_LIGHT
        prefs(context).edit()
            .putString(KEY_PALETTE, if (toDark) VALUE_DARK else VALUE_LIGHT)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
}
