package com.example.blespresso.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.foundation.layout.Spacer
import com.example.blespresso.ui.theme.ThemePrefs
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.blespresso.R
import com.example.blespresso.data.MachineStatus
import com.example.blespresso.ui.theme.accent_indic
import com.example.blespresso.ui.theme.background_indic
import com.example.blespresso.ui.theme.icon_heat
import com.example.blespresso.ui.theme.icon_off
import com.example.blespresso.ui.theme.icon_ready
import com.example.blespresso.ui.theme.surface
import com.example.blespresso.ui.theme.textprimary
import com.example.blespresso.ui.theme.textsecondary

// TLDR: UI side of 0x1401 machine status (STATUS TODO #9, spec docs/UX/README.md §8).
//       All code -> text / icon / color mapping lives here; VM and BLE layers pass raw codes.

private const val NO_STATUS_TEXT = "—"

enum class StatusSlot { BOILER, BREW, STEAM, STEP }

// Tint resolved at draw time so a runtime palette switch (activePalette) is followed.
private enum class TintRole {
    HEAT, READY, OFF;

    fun color(): Color = when (this) {
        HEAT -> icon_heat
        READY -> icon_ready
        OFF -> icon_off
    }
}

// One row of README §8.2.
private data class StateSpec(
    val slot: StatusSlot,
    @DrawableRes val iconRes: Int,
    val tint: TintRole,
    val isLit: Boolean,
    val isPulse: Boolean,
    val label: String,                  // top bar text (e.g. "PRE-INFUSION [BREWING]")
    val contentDescription: String,     // slot label under the icon + accessibility text
    val isLampIcon: Boolean = false     // Option B drawables: glow on glyph only (§8.4)
)

private val STATE_SPECS: Map<Int, StateSpec> = mapOf(
    0x00 to StateSpec(StatusSlot.BOILER, R.drawable.ic_state_00_heating_on, TintRole.HEAT, true, true, "HEATING", "HEATING"),
    0x01 to StateSpec(StatusSlot.BOILER, R.drawable.ic_state_00_heating_on, TintRole.OFF, false, false, "HEATER OFF", "HEATER OFF"),
    0x02 to StateSpec(StatusSlot.BOILER, R.drawable.ic_state_02_ready, TintRole.READY, true, false, "READY", "READY"),
    0x03 to StateSpec(StatusSlot.BREW, R.drawable.ic_state_03_brewing, TintRole.HEAT, true, true, "BREWING", "BREWING"),
    0x04 to StateSpec(StatusSlot.BREW, R.drawable.ic_state_04_brew_preinfuse, TintRole.HEAT, true, true, "PRE-INFUSION [BREWING]", "PRE-INFUSION", isLampIcon = true),
    0x05 to StateSpec(StatusSlot.BREW, R.drawable.ic_state_05_brew_infuse, TintRole.HEAT, true, true, "INFUSION [BREWING]", "INFUSION", isLampIcon = true),
    0x06 to StateSpec(StatusSlot.BREW, R.drawable.ic_state_06_brew_taper, TintRole.HEAT, true, true, "TAPERING [BREWING]", "TAPERING", isLampIcon = true),
    0x07 to StateSpec(StatusSlot.BREW, R.drawable.ic_state_07_brew_autostop, TintRole.READY, true, false, "SHOT DONE", "SHOT DONE"),
    0x08 to StateSpec(StatusSlot.STEAM, R.drawable.ic_state_08_steaming, TintRole.HEAT, true, true, "STEAMING", "STEAMING"),
    0x10 to StateSpec(StatusSlot.STEP, R.drawable.ic_state_10_step_off, TintRole.OFF, false, false, "STEP OFF", "STEP OFF"),
    0x11 to StateSpec(StatusSlot.STEP, R.drawable.ic_state_11_step_on, TintRole.HEAT, true, true, "STEP ON", "STEP ON")
)

// README §8.3: idle drawable + idle label per slot, top to bottom.
private val SLOT_IDLE: List<Triple<StatusSlot, Int, String>> = listOf(
    Triple(StatusSlot.BOILER, R.drawable.ic_state_00_heating_on, "Boiler"),
    Triple(StatusSlot.BREW, R.drawable.ic_state_03_brewing, "Brew"),
    Triple(StatusSlot.STEAM, R.drawable.ic_state_08_steaming, "Steam"),
    Triple(StatusSlot.STEP, R.drawable.ic_state_10_step_off, "Step")
)

private fun modeText(mode: Int?): String = when (mode) {
    0 -> "CLASSIC"
    1 -> "PROFILE"
    2 -> "STEP FUNCTION"
    else -> NO_STATUS_TEXT
}

// Slot text style: first letter capital, rest lowercase ("PRE-INFUSION" -> "Pre-infusion").
private fun String.toSlotText(): String = lowercase().replaceFirstChar { it.uppercase() }

private fun MachineStatus?.stateSpec(): StateSpec? = this?.let { STATE_SPECS[it.state] }

// TLDR: Top bar (§8.5): mode chip + active state label. null / unknown -> "—".
@Composable
fun StatusTopBar(
    status: MachineStatus?,
    modifier: Modifier = Modifier,
    onToggleTheme: (() -> Unit)? = null     // TODO #13: light/dark switch at the right end
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = modeText(status?.mode),
            style = TextStyle(
                color = accent_indic, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.em
            ),
            modifier = Modifier
                .border(1.5.dp, accent_indic, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 1.dp)
        )
        Text(
            text = status.stateSpec()?.label ?: NO_STATUS_TEXT,
            style = TextStyle(color = textprimary, fontSize = 13.sp)
        )
        if (onToggleTheme != null) {
            // Icon shows the theme you switch TO: moon while light, sun while dark.
            val isDark = ThemePrefs.isDark
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleTheme, modifier = Modifier.size(32.dp)) {
                Icon(
                    painter = painterResource(
                        id = if (isDark) R.drawable.ic_theme_light else R.drawable.ic_theme_dark
                    ),
                    contentDescription = if (isDark) "Switch to light theme" else "Switch to dark theme",
                    tint = textprimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// TLDR: Slot column (§8.3): boiler, brew, steam, step. Only the slot of the current state is
//       active; the rest show their idle icon unlit.
@Composable
fun StatusSlotColumn(status: MachineStatus?, modifier: Modifier = Modifier) {
    val active = status.stateSpec()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SLOT_IDLE.forEach { (slot, idleIcon, idleLabel) ->
            if (active != null && active.slot == slot) {
                StatusSlotCell(
                    iconRes = active.iconRes, tint = active.tint.color(), label = active.contentDescription.toSlotText(),
                    contentDescription = active.contentDescription, isActive = true,
                    isLit = active.isLit, isPulse = active.isPulse, isLampIcon = active.isLampIcon
                )
            } else {
                StatusSlotCell(
                    iconRes = idleIcon, tint = icon_off, label = idleLabel,
                    contentDescription = idleLabel, isActive = false,
                    isLit = false, isPulse = false, isLampIcon = false
                )
            }
        }
    }
}

@Composable
private fun StatusSlotCell(
    @DrawableRes iconRes: Int,
    tint: Color,
    label: String,
    contentDescription: String,
    isActive: Boolean,
    isLit: Boolean,
    isPulse: Boolean,
    isLampIcon: Boolean
) {
    Column(modifier = Modifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .statusGlow(tint, isLit, isPulse, isLampIcon),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = label,
            maxLines = 2,
            style = TextStyle(
                color = if (isActive) textprimary else textsecondary,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}

// TLDR: §8.4 illumination. Radial glow behind the icon, color = tint, alpha 0.4.
//       Pulse: alpha 0.4 -> 0.1 -> 0.4 over 2000 ms. Lamp icons (0x04-0x06): glow kept inside
//       the lens (~10 dp) so the ring gets no halo; others ~18 dp.
@Composable
private fun Modifier.statusGlow(
    tint: Color,
    isLit: Boolean,
    isPulse: Boolean,
    isLampIcon: Boolean
): Modifier {
    if (!isLit) return this
    val transition = rememberInfiniteTransition()
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1000), RepeatMode.Reverse)
    )
    val alpha = if (isPulse) pulseAlpha else 0.4f
    val radiusDp = if (isLampIcon) 10.dp else 18.dp
    return this.drawBehind {
        val radiusPx = radiusDp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tint.copy(alpha = alpha), Color.Transparent),
                center = center,
                radius = radiusPx
            ),
            radius = radiusPx
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Previews: every state + no status, without BLE.
@Preview(showBackground = true, widthDp = 380)
@Composable
private fun StatusTopBarPreview() {
    Column {
        StatusTopBar(status = null)
        StatusTopBar(status = MachineStatus(0, 0x03))
        StatusTopBar(status = MachineStatus(1, 0x04))
        StatusTopBar(status = MachineStatus(2, 0x11))
    }
}

@Preview(showBackground = true, widthDp = 760)
@Composable
private fun StatusSlotColumnPreview() {
    val samples: List<MachineStatus?> =
        listOf(null) + listOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x10, 0x11)
            .map { MachineStatus(1, it) }
    Row(modifier = Modifier.background(background_indic).padding(8.dp)) {
        samples.forEach { StatusSlotColumn(status = it, modifier = Modifier.padding(4.dp)) }
    }
}
