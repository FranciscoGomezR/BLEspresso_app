# UX proposal — machine status, palette, icons (STATUS TODO #6)

> **AI agent implementing STATUS TODO #9: read §8 only.** §8 is self-contained and
> authoritative. §1–§7 are design history; on any conflict, §8 wins.

Input for TODO #9 (0x1401 machine status `{mode, state}`). Open `preview.html` in a browser
to see both icon options, OFF vs ON, light/dark.

## 1. Style
Gaggia Classic look: brushed steel / neutral surfaces, near-black text, one brand-red
accent, warm cream background. Flat, no gradients except the "lamp" glow.

- **Mode** = text label, accent color, letter-spaced caps: `CLASSIC`, `PROFILE`,
  `STEP FUNCTION` (mode 0/1/2).
- **State** = icon + short text under it (e.g. "Pre-infusion").

## 2. Palette (`ui/theme/Color.kt`)
Three palettes = `AppPalette` instances in `Color.kt`. Switch by editing ONE line:
`var activePalette by mutableStateOf(PALETTE_GAGGIA_LIGHT)` (or `PALETTE_CLASSIC`,
`PALETTE_GAGGIA_DARK`). Old palettes #0–#6 kept at file bottom as commented reference.

| Palette | Use | background | surface | accent | textprimary |
|---|---|---|---|---|---|
| #1/#3 `PALETTE_CLASSIC` | previous default | #EEEEEE | #C3C8D3 | #FFBD76 | #313841 |
| #7 `PALETTE_GAGGIA_LIGHT` | ACTIVE | #F4EFE6 | #DDDFE2 | #C8102E | #1A1A1A |
| #8 `PALETTE_GAGGIA_DARK` | dark (not active) | #1A1A1A | #2A2C30 | #E53950 | #F2F2F2 |

Gaggia hex values are approximations of the brand red / black / steel / cream, not
official codes.

Roles covered by each palette:

| val | Applied to |
|---|---|
| `background*`, `surface*`, `textprimary*`, `textsecondary*`, `accent*` | screens, text, buttons |
| `*_indic` | `BoilerTemperatureProgressIndicator()` |
| `textbox_background`, `textbox_text` | all `BasicTextField` inputs (both screens) |
| `icon_off` | unlit icon (text color, 38 % alpha) |
| `icon_heat` (orange) / `icon_ready` (green) | lit icon tint: green for 0x02 READY and 0x07 BREW_AUTOSTOP, orange for every other lit state |
| `icon_glow` | lamp halo base (TODO #9) |
| `on_accent` | text on `accent` backgrounds (buttons) |
| `statusbar` | Android system status bar (applied in `Theme.kt`; icons auto dark/light) |

Icons never hardcode color: SVG uses `currentColor`, app tints from palette.

**Theme switch (STATUS TODO #13):** user toggles Gaggia light ↔ dark with the sun/moon icon at
the right end of the main-screen top bar (`layout_DarkLightMode_preview.html`). Choice saved by
`ui/theme/ThemePrefs.kt` (SharedPreferences `ui_prefs` / `palette`), loaded in
`MainActivity.onCreate`. System dark mode is ignored. `activePalette` default in Color.kt =
first-launch theme.

**Rule (STATUS TODO #11):** every UI color comes from `activePalette` — via the Color.kt
getters, or via `MaterialTheme.colors`, which `Theme.kt` builds from the active palette
(primary = `accent`, background, surface, on* = `textprimary` / `on_accent`). `Theme.kt` also
wraps all screens in a `Surface(background)` with default text color `textprimary`, and sets the
Android status bar from `statusbar`. Fixed exceptions: dial >105 °C red band
(`BoilerTemperatureProgressIndicator.kt`), transparent edge of the status glow.

## 3. Icon format
Single-color **SVG** source -> Android **VectorDrawable** (Android Studio > New > Vector
Asset > Local file). Why: resolution independent, tiny APK cost, tint per theme,
animatable alpha. No PNG/WebP needed.

## 4. "Illumination" (ON / power applied)
Mimics Classic front-panel lamps.

- **OFF:** outline glyph, `icon_off`.
- **ON:** glyph tinted by role color + soft halo (Compose: `Modifier.drawBehind` radial
  gradient or `shadow` with role color, alpha ~0.4).
- **Pulse:** slow 2 s alpha breathing on active-power states (heating, brewing, steaming,
  step on). Static for READY / AUTOSTOP / OFF.

## 5. Icon options

- **Option A — line glyphs** (`option_a/`): 24 dp, 1.6 stroke, Material Symbols weight.
  Brew phases differ by drops above the cup: 1 drop pre-infusion, 3 drops infusion,
  big + small drop taper. STEAMING: three steam jets converging at top, opening into a
  cloud at the bottom (`state_08_steaming.svg` / `ic_state_08_steaming`).
- **Option B — panel lamps** (`option_b/`): the same glyph inside a round lamp lens with
  ring. Brew phases also show a 3-segment ring (1/3, 2/3, 3/3 lit) = progress at a glance.

Selected: **Option A**, except 0x04 / 0x05 / 0x06 (brew phases) = **Option B** (lamp +
3-segment progress ring). See `icon_selection.md`.

## 6. State table (selected icons)

Source SVG: `docs/UX/option_a/`. App drawable: `app/src/main/res/drawable/`
(VectorDrawable, black stroke; tint with role color at use site, e.g.
`Icon(painterResource(R.drawable.ic_state_00_heating_on), null, tint = icon_heat)`).

| Code | State | SVG (`docs/UX/`) | Drawable (`R.drawable.`) | Role color | Pulse |
|---|---|---|---|---|---|
| 0x00 | HEATING_ON | `option_a/state_00_heating_on.svg` | `ic_state_00_heating_on` | icon_heat | yes |
| 0x01 | HEATING_OFF | `option_a/state_00_heating_on.svg` | `ic_state_00_heating_on` | icon_off | no |
| 0x02 | READY | `option_a/state_02_ready.svg` | `ic_state_02_ready` | icon_ready | no |
| 0x03 | BREWING (classic) | `option_a/state_03_brewing.svg` | `ic_state_03_brewing` | icon_heat | yes |
| 0x04 | BREW_PREINFUSE | `option_b/state_04_brew_preinfuse.svg` | `ic_state_04_brew_preinfuse` | icon_heat | yes |
| 0x05 | BREW_INFUSE | `option_b/state_05_brew_infuse.svg` | `ic_state_05_brew_infuse` | icon_heat | yes |
| 0x06 | BREW_TAPER | `option_b/state_06_brew_taper.svg` | `ic_state_06_brew_taper` | icon_heat | yes |
| 0x07 | BREW_AUTOSTOP | `option_a/state_07_brew_autostop.svg` | `ic_state_07_brew_autostop` | icon_ready | no |
| 0x08 | STEAMING | `option_a/state_08_steaming.svg` | `ic_state_08_steaming` | icon_heat | yes |
| 0x10 | STEP_OFF | `option_a/state_10_step_off.svg` | `ic_state_10_step_off` | icon_off | no |
| 0x11 | STEP_ON | `option_a/state_11_step_on.svg` | `ic_state_11_step_on` | icon_heat | yes |

**Boiler indicator = one icon slot** for 0x00 / 0x01 / 0x02. The icon swaps on each
0x1401 notify: HEATING_ON -> `state_00_heating_on.svg` lit (icon_heat, pulse);
HEATING_OFF -> same icon unlit (icon_off); READY -> `state_02_ready.svg` (icon_ready).

**Brew indicator = one icon slot** for 0x03..0x07. The icon swaps on each 0x1401 notify:
BREWING (classic) / BREW_PREINFUSE / BREW_INFUSE / BREW_TAPER lit (icon_heat, pulse);
BREW_AUTOSTOP lit (icon_ready, static).

**Step indicator = one icon slot** for 0x10 / 0x11. STEP_OFF -> `state_10_step_off.svg`
(icon_off); STEP_ON -> `state_11_step_on.svg` lit (icon_heat, pulse).

Unknown code -> text "—", no icon.

## 7. Layout proposals (STATUS TODO #8)
Preview: `layout_preview.html` (state / mode / palette selectors drive all mocks).
Slots: boiler (0x00–0x02), brew (0x03–0x07), steam (0x08), step (0x10–0x11); inactive slot
shows its idle icon unlit.

| Layout | Placement | Pros | Cons |
|---|---|---|---|
| L1 Status strip | strip between dial and "Machine Configuration": mode text left, 4 slots right, active slot label = state | least code change, dial untouched | ~60 dp extra height |
| L2 Dial corners | mode text above dial, slots in dial-row corners, state text inside dial | no extra height, status next to temperature | corners crowded on narrow phones |
| L3 Header bar + side column | top bar: mode chip + state text; slots stacked right of dial | clear hierarchy, state always top, fits 360 dp | dial slightly off-centre, new top bar |

Selected: **L3**, side-by-side variant. Top bar (surface bg): mode chip (accent
border/text) + state text. Dial row: `Row(horizontalArrangement = Center)` = dial (280 dp)
+ 16 dp gap + slot column (52 dp: boiler, brew, steam, step, vertically centred); the pair
is centred as one group. Min width 348 dp -> fits 360 dp phones. (Pinned-right variant
rejected: needs 400 dp, overlaps on 360–393 dp.)
Buttons: one row — Controller left, Read + Write right (Controller moves into the empty
0.4-weight column of the Read/Write row; separate Controller row removed). Button row directly under the brew table with
~14 dp vertical padding, not pinned to the screen bottom. Rest unchanged.

## 8. Implementation spec for STATUS TODO #9 (authoritative)

All values in §8 are confirmed decisions (labels, sizes, colors, glow/pulse, idle/no-status
behavior). No open points.

Scope: UI side of 0x1401 machine status on `RemoteControlScreen` (STATUS TODO #9).
BLE/VM logic is TODO #7 (done first). Paths relative to `app/src/main/`.

Input (from TODO #7, do not change):
```kotlin
data class MachineStatus(val mode: Int, val state: Int)       // raw codes
viewModel.machineStatus: MachineStatus?                        // null = not received yet
```
`null` -> mode text `—`, state text `—`, all four slots idle. All code -> text/icon/color
mapping lives in the UI (8.2); do not add it to the VM or BLE layer.

### 8.0 Integration seam (how to wire it)
- `MachineStatus` is in `java/.../data/BoilerDataCharacteristics.kt`;
  `RemoteControlViewModel.machineStatus` (read-only, `private set`) is already implemented.
- New file `java/.../presentation/MachineStatusUi.kt`: one pure mapping function
  (e.g. `fun MachineStatus?.toUi(): MachineStatusUi`) returning mode text, state label,
  contentDescription, active slot, drawable, tint, lit, pulse — straight from 8.2 / 8.3.
- Stateless composables taking `status: MachineStatus?`: `StatusTopBar(status)`,
  `StatusSlotColumn(status)`. `RemoteControlScreen` calls them with `viewModel.machineStatus`.
- Add `@Preview`s with fake values (e.g. `MachineStatus(1, 0x04)`, `null`) to check every
  state without BLE. Do not add setters or test hooks to the ViewModel.

### 8.1 Already done (TODO #6) — do not redo
- `java/.../ui/theme/Color.kt`: `AppPalette` data class + `PALETTE_CLASSIC`,
  `PALETTE_GAGGIA_LIGHT` (ACTIVE), `PALETTE_GAGGIA_DARK`; selected by `activePalette`.
  Top-level getters (`background`, `accent_indic`, `icon_heat`, ...) read `activePalette`. Gaggia hex values are approximations (accepted).
  Every palette defines: `background*`, `surface*`, `accent*`, `textprimary*`,
  `textsecondary*` (each + `_indic` twin), `textbox_background`, `textbox_text`,
  `icon_off`, `icon_heat`, `icon_ready`, `icon_glow` (unused by this spec; glow = tint).
  `on_accent` (text on `accent`): added in TODO #9 (8.1b).
  Lit colors: only two. `icon_ready` (green) for 0x02 / 0x07; `icon_heat` (orange) for all
  other lit states. (`icon_brew` / `icon_steam` removed.)
- All `BasicTextField`s in `RemoteControlScreen.kt` and `ControllerParameterScreen.kt`
  already use `textbox_background` / `textbox_text`.
- Drawables exist: `res/drawable/ic_state_*.xml` (VectorDrawable, 24 dp, black stroke).
  0x04 / 0x05 / 0x06 drawables are Option B (lamp lens + 3-segment ring: 1/3, 2/3, 3/3
  lit); all others Option A. Same drawable names; use as listed in 8.2.
  Always tint at use site: `Icon(painterResource(id), contentDescription, tint = <icon_*>)`.
- Do not change palette values.

### 8.1b Palette switch (part of TODO #9)
- **#7 (Gaggia light)** is the active palette: `activePalette = PALETTE_GAGGIA_LIGHT`.
- No system dark-mode handling yet (future STATUS TODO #11; runtime switch = assign
  `activePalette`, screens recompose).
- Add new val `on_accent` (text on `accent` backgrounds) to all 3 palettes:
  CLASSIC `#313841`; GAGGIA_LIGHT `#FFFFFF`; GAGGIA_DARK `#FFFFFF`.
- All `Button` text on `RemoteControlScreen` **and** `ControllerParameterScreen` uses
  `on_accent` (was `textprimary`: near-black on Gaggia red = poor contrast). This is the only
  allowed change to `ControllerParameterScreen`. Mode chip unaffected (red text on surface).

### 8.2 Status model
Payload 0x1401 = `{mode: UByte, state: UByte}`.

| mode | Text |
|---|---|
| 0 | `CLASSIC` |
| 1 | `PROFILE` |
| 2 | `STEP FUNCTION` |
| other / not yet received | `—` |

| state | Name | Slot | Drawable | Tint | Lit | Pulse | Label (top bar) | Slot text + contentDescription |
|---|---|---|---|---|---|---|---|---|
| 0x00 | HEATING_ON | boiler | `ic_state_00_heating_on` | `icon_heat` | yes | yes | `HEATING` | `HEATING` |
| 0x01 | HEATING_OFF | boiler | `ic_state_00_heating_on` | `icon_off` | no | no | `HEATER OFF` | `HEATER OFF` |
| 0x02 | READY | boiler | `ic_state_02_ready` | `icon_ready` | yes | no | `READY` | `READY` |
| 0x03 | BREWING | brew | `ic_state_03_brewing` | `icon_heat` | yes | yes | `BREWING` | `BREWING` |
| 0x04 | BREW_PREINFUSE | brew | `ic_state_04_brew_preinfuse` | `icon_heat` | yes | yes | `PRE-INFUSION [BREWING]` | `PRE-INFUSION` |
| 0x05 | BREW_INFUSE | brew | `ic_state_05_brew_infuse` | `icon_heat` | yes | yes | `INFUSION [BREWING]` | `INFUSION` |
| 0x06 | BREW_TAPER | brew | `ic_state_06_brew_taper` | `icon_heat` | yes | yes | `TAPERING [BREWING]` | `TAPERING` |
| 0x07 | BREW_AUTOSTOP | brew | `ic_state_07_brew_autostop` | `icon_ready` | yes | no | `SHOT DONE` | `SHOT DONE` |
| 0x08 | STEAMING | steam | `ic_state_08_steaming` | `icon_heat` | yes | yes | `STEAMING` | `STEAMING` |
| 0x10 | STEP_OFF | step | `ic_state_10_step_off` | `icon_off` | no | no | `STEP OFF` | `STEP OFF` |
| 0x11 | STEP_ON | step | `ic_state_11_step_on` | `icon_heat` | yes | yes | `STEP ON` | `STEP ON` |
| other | — | none | — | — | — | — | `—` | — |

Label (incl. `[BREWING]`) = top bar state text only. Text under the active slot icon =
contentDescription column (no `[BREWING]`), shown in sentence case: first letter capital,
rest lowercase (`Pre-infusion`, `Heater off`, `Shot done`). Idle slots: both = idle label
(8.3), already sentence case.

### 8.3 Slots
Four fixed slots, always visible, order top to bottom: boiler, brew, steam, step.
Exactly one slot is active (the slot of the current state); the others are idle.

| Slot | Idle drawable | Idle label |
|---|---|---|
| boiler | `ic_state_00_heating_on` | Boiler |
| brew | `ic_state_03_brewing` | Brew |
| steam | `ic_state_08_steaming` | Steam |
| step | `ic_state_10_step_off` | Step |

- Idle slot: idle drawable, tint `icon_off`, label `textsecondary`, 10 sp.
- Active slot: drawable/tint from 8.2, label = 8.2 Label, `textprimary`, 10 sp, SemiBold.
- Unknown state / no status yet: all four idle.
- Slot cell: `Column(width 52.dp, CenterHorizontally)`: icon 30 dp in a 34 dp box + label.

### 8.4 Illumination
- Lit (Lit = yes): behind the icon draw a radial glow, color = tint, alpha 0.4, radius ~18 dp
  (`Modifier.drawBehind { drawCircle(Brush.radialGradient(...)) }`).
- 0x04 / 0x05 / 0x06 (Option B lamp drawables): glow on the inner glyph only, not on the
  lamp circle (lens + ring). Keep the glow radius inside the lens (~10 dp for a 30 dp icon)
  so the ring gets no halo. The ring/lens itself is tinted normally, no extra effect.
- Not lit: no glow.
- Pulse = yes: animate glow alpha 0.4 -> 0.1 -> 0.4, 2000 ms, `infiniteRepeatable`
  (`rememberInfiniteTransition`). Pulse = no: static.

### 8.5 Layout (`RemoteControlScreen`, Connected branch)
```
Column(fillMaxSize)
├─ TopBar  Row(fillMaxWidth, bg = surface, padding h 14.dp / v 10.dp,
│          CenterVertically, spacedBy 10.dp)                                  NEW
│   ├─ Mode chip: Text(mode text), color accent_indic, Bold, 13 sp,
│   │   letterSpacing 0.1 em, border 1.5.dp accent_indic, RoundedCornerShape(12.dp),
│   │   padding h 10.dp / v 1.dp
│   ├─ Text(active state Label or "—"), textprimary, 13 sp
│   └─ (main screen only, TODO #13) Spacer(weight 1f) + IconButton 32.dp: theme switch,
│       moon (ic_theme_dark) while light / sun (ic_theme_light) while dark, tint textprimary
├─ DialRow  Row(weight 0.7f, fillMaxWidth, bg = background_indic,
│          horizontalArrangement = Center, verticalAlignment = CenterVertically) CHANGED
│   ├─ BoilerTemperatureProgressIndicator(...)    UNCHANGED (same 280.dp, same params)
│   ├─ Spacer(width 16.dp)
│   └─ StatusSlotColumn: Column(spacedBy 2.dp) { boiler, brew, steam, step }   NEW
├─ Config section (title, presets, brew table)    UNCHANGED
└─ ButtonRow (existing Read/Write Row)                                       CHANGED
    ├─ former empty weight-0.4 Column -> now holds the Controller button (left)
    ├─ Read  (weight 0.3, unchanged)
    └─ Write (weight 0.3, unchanged)
    Padding: vertical 14.dp; placed directly under the brew table, NOT pinned to bottom.
    Delete the old separate Controller Row (same onClick / navigation moves with the button).
    Button text color: on_accent (8.1b).
```
Min width: 280 + 16 + 52 = 348 dp (fits 360 dp phones).

### 8.6 Do not
- Modify `BoilerTemperatureProgressIndicator.kt` or its call arguments.
- Pin the slot column to the right edge (rejected: needs 400 dp).
- Use `preview.html` or `layout_preview.html` as spec (visual reference only). Use the
  drawables in 8.2 as-is; do not regenerate them from `docs/UX/option_a|b/`.
- Hardcode colors; always use `Color.kt` vals.
- Change palettes (beyond the 8.1b switch), text fields, or the config section beyond 8.5.
- Implement WRITE blocking while brewing/steaming (STATUS TODO #10, out of scope).

### 8.7 Done when
- `PALETTE_GAGGIA_LIGHT` active; build passes with each of the 3 palettes active.
- Each state 0x00–0x11 shows correct slot, drawable, tint, glow, pulse, labels.
- Controller / Read / Write in one row under the brew table, working as before.
- Button text readable on both screens (`on_accent`, white on red with #7).

### 8.8 Busy lock (STATUS TODO #10)
- `RemoteControlViewModel.isMachineBusy` = state in 0x03..0x06 or 0x08 (brewing phases,
  steaming). Not busy: 0x00–0x02, 0x07 SHOT DONE, 0x10/0x11, unknown, `null`.
- While busy:
  - RemoteControlScreen: Controller, Read, Write disabled; ControllerParameterScreen: Read,
    Write disabled (Main stays enabled).
  - Disabled look: background `surface`, text `icon_off`.
  - All BasicTextFields `readOnly`.
  - Text "Machine busy" (textprimary, 13 sp, centred) in a reserved 18 dp line above the
    button row (empty when not busy, no layout jump).
- Write fires on double-tap of its label (`detectTapGestures`): handlers return early when
  busy; `pointerInput(isBusy)` so the check is never stale.
- VM: `writeMachineConfig` / `writePIDConfig` return false when busy. Reads are not guarded
  in the VM (connect-time read also fetches 0x1401).
