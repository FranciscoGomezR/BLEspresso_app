# Changelog

All notable changes to BLEspresso_app are logged here, newest first.

Format: one dated section per change set. Each entry states **what** changed,
**why**, and any **migration / action required** (flash erase, app update, etc.).
Keep it terse. Link source files with repo-relative paths.

Categories: `Added` `Changed` `Removed` `Fixed` `Docs` `Breaking`.

---

## 2026-10-12 — Busy lock while brewing / steaming (STATUS TODO #10)

Why: changing brew / PID parameters during a shot or steam is unsafe and confusing.

### Added
- `RemoteControlViewModel.isMachineBusy`: 0x1401 state 0x03..0x06 or 0x08.
  `writeMachineConfig` / `writePIDConfig` return false when busy (reads not guarded: the
  connect-time read also fetches 0x1401).
- "Machine busy" line above the button row (reserved 18 dp, no layout jump), both screens.

### Changed
- `RemoteControlScreen.kt`: Controller / Read / Write disabled while busy; 8 textboxes
  read-only. `ControllerParameterScreen.kt`: Read / Write disabled (Main enabled); 6 PID
  textboxes read-only. Disabled look: `surface` background, `icon_off` text.
- Write double-tap handlers return early when busy; `pointerInput(isBusy)` keeps the check
  current.

### Verified
- Hardware: blocked during 0x04–0x06 and 0x08 (both screens), re-enabled at 0x07; double-tap
  Write sends nothing while busy.

## 2026-10-11 — 0x1401 machine status on screen (STATUS TODO #9)

Why: show machine mode + state (TODO #7 data) on RemoteControlScreen per docs/UX/README.md §8.

### Added
- `presentation/MachineStatusUi.kt`: code -> label / icon / tint map (§8.2), `StatusTopBar`
  (mode chip + state label), `StatusSlotColumn` (boiler, brew, steam, step; idle slots unlit),
  glow (Option B lamp icons: glyph only) + 2 s pulse, `@Preview`s for all states + null.
  Slot text under the icon is the short label in sentence case (`Pre-infusion`,
  `Infusion`, `Tapering`, `Heater off`, ...), matching idle labels; top bar keeps
  upper case `... [BREWING]`.

### Changed
- `RemoteControlScreen.kt`: top bar; dial + 16 dp + slot column centred as a group;
  Controller button moved into the Read/Write row (left), old Controller row removed;
  button row padding 14 dp under the brew table.
- Button text uses new `on_accent` (white on Gaggia red) on RemoteControl and
  ControllerParameter screens.
- Active palette: Gaggia light.

### Verified
- Android Studio previews; hardware.

## 2026-10-11 — One-line palette switch (Color.kt)

Why: palettes were switched by moving `/* */` blocks that redeclared the same vals —
error-prone and impossible at runtime.

### Changed
- `ui/theme/Color.kt`: `data class AppPalette` (18 color roles) + `PALETTE_CLASSIC` (former
  #1/#3), `PALETTE_GAGGIA_LIGHT` (former #7), `PALETTE_GAGGIA_DARK` (former #8).
  Switch = `var activePalette by mutableStateOf(PALETTE_GAGGIA_LIGHT)`. Existing names
  (`background`, `accent_indic`, `icon_heat`, ...) are getters on `activePalette` -> no
  call-site changes; assigning `activePalette` at runtime recomposes all screens.
- Palettes #0–#6 kept at file bottom as commented reference.
- New role `statusbar` (CLASSIC #C3C8D3, GAGGIA_LIGHT #DDDFE2, GAGGIA_DARK #2A2C30);
  `ui/theme/Theme.kt` applies it to the Android status bar and picks dark/light status icons
  by luminance. `themes.xml` color now only covers app launch.
- `presentation/MachineStatusUi.kt`: state tint stored as `TintRole`, resolved at draw time
  (follows a runtime palette switch).

### Verified
- Build passes with each of the 3 palettes active. Values unchanged (GAGGIA_LIGHT active).

## 2026-10-11 — 0x1401 machine status logic (STATUS TODO #7)

Why: firmware TODO #25 made 0x1401 a 2 B `{mode, state}` READ + NOTIFY characteristic.
App now receives it; UI rendering follows in TODO #9.

### Added
- `data/BoilerDataCharacteristics.kt`: `MachineStatus(mode, state)` (raw unsigned codes).
- `data/RemoteControlReceiveManager.kt` + `data/ble/RemoteControlBLEReceiveManager.kt`:
  `dataMachineStatus` flow; `emitMachineStatus()` decodes len == 2 (other lengths ignored +
  logged, tag `BLE_MachineStatus`), used by `onCharacteristicRead` and
  `onCharacteristicChanged`.
- `presentation/RemoteControlViewModel.kt`: `machineStatus: MachineStatus?` (private set),
  null until received and on data-flow Loading / Error (disconnect / reconnect).

### Changed
- `RemoteControlBLEReceiveManager.kt`: 0x1401 added to `CCCD_NOTIFY_UUIDS` (first) and to the
  `rxReadMachineConfiguration()` read queue (last, 10 chars); comment firmware #24 -> #25.

### Verified
- Hardware: CCCD 0x1401 status=0; initial read `mode=1 state=0x00`; profile shot notifies
  0x04 -> 0x05 -> 0x06 -> 0x07 -> 0x00, one per change. No UI change.

### Migration
- None. GATT handles unchanged; requires firmware with TODO #25.

## 2026-10-09 — Status layout proposals (STATUS TODO #8)

### Docs
- `docs/UX/layout_preview.html`: 3 RemoteControlScreen layouts for mode text + 4 status
  slots (boiler, brew, steam, step). `docs/UX/README.md` §7 pros/cons.

## 2026-10-09 — UX proposal + Gaggia palettes (STATUS TODO #6)

Why: 0x1401 machine status (TODO #7) needs icons, and the app needed Gaggia-styled palettes
covering icons, background, text, textboxes and the boiler indicator.

### Added
- `docs/UX/`: `README.md` proposal, `option_a/` + `option_b/` (11 state SVGs each,
  `currentColor`), `preview.html` (OFF/ON glow, light/dark), `icon_selection.md`.
- `res/drawable/ic_state_*.xml`: 10 VectorDrawables from selected Option A (0x01 reuses
  `ic_state_00_heating_on`). Tint at use site.
- `ui/theme/Color.kt`: `//COLOR PALETTE #7 (Gaggia light)`, `#8 (Gaggia dark)` (commented);
  new vals `textbox_background`, `textbox_text`, `icon_off/heat/ready/glow` (lit = 2 colors: ready green for 0x02/0x07, heat orange otherwise) in
  active palette #1/#3 and in #7/#8.

### Changed
- `RemoteControlScreen.kt`, `ControllerParameterScreen.kt`: `BasicTextField` colors
  `surface`/`textprimary` -> `textbox_background`/`textbox_text`. No visual change with
  palette #1/#3 (same values).

## 2026-10-07 — Brew/steam preset limits = firmware limits, range label generated

Why: app accepted brew 0–150 / steam 100–145 °C, label showed 0–100 / 100–140 °C, firmware
accepts 20–110 / 100–135 °C. Out-of-range writes were rejected by firmware without feedback.

### Changed
- `RemoteControlViewModel.kt`: `BOILER_TEMP_MIN/MAX` -> `BREW_TEMP_PRESET_MIN/MAX` (20/110),
  `STEAM_TEMP_MIN/MAX` -> `STEAM_TEMP_PRESET_MIN/MAX` (100/135), matching firmware
  `espressoMachineServices.h`.
- `RemoteControlScreen.kt`: range labels built from those constants; `cBoilerTB` ->
  `cBrewTempPresetTB`, `cSteamTempTB` -> `cSteamTempPresetTB`.

## 2026-10-07 — Sync with firmware TODO #23: 0x1403 READ + NOTIFY (STATUS TODO #5)

Firmware now sets the active boiler setpoint (0x1403) from the brew (0x1404) / steam (0x1405)
presets on BREW/STEAM switch change and notifies it; 0x1403 is no longer writable.

### Changed
- `RemoteControlBLEReceiveManager.kt`: notifications enabled for 0x1402 **and** 0x1403 through a
  CCCD queue (`CCCD_NOTIFY_UUIDS`, `enableNextNotification()`, new `onDescriptorWrite`), one CCCD
  write at a time, BUSY retried (150 ms, 5x). Firmware TODO #24: add 0x1401 to the list.
- 0x1403 notify -> `dataToRead` -> `boilerTempSetpoint` -> `BoilerTemperatureProgressIndicator`.
- Machine-config read queue now also reads 0x1404 (`dataBrewTempPreset`).
- Brew textbox loads from 0x1404 `brewTempPreset` (was 0x1403: while steaming it would show and
  save the steam temp into the brew preset).
- `parseTemp4()` replaces 4 copies of the ASCII "DDDd" temperature parse.

### Renamed
- 0x1403: `SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID` -> `BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID`;
  VM `boilerSetpointTemp` -> `boilerTempSetpoint` (private set); indicator param
  `boilerTargetTemp` -> `boilerTempSetpoint`; `rxReadBoilerTargetTemp` -> `rxReadBoilerTempSetpoint`.
- Presets: `txNewBoilerTargetTemp` -> `txNewBrewTempPreset` (single 0x1404 write, no 0x1403);
  `txNewSteamTempSetpoint` -> `txNewSteamTempPreset`; `rxReadSteamTempSetpoint` ->
  `rxReadSteamTempPreset` (+ new `rxReadBrewTempPreset`); `dataSteamSetpoint` ->
  `dataSteamTempPreset`; `STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID` ->
  `STEAM_TEMP_PRESET_CHARACTERISTICS_UUID`; VM `steamTempSetpoint` -> `steamTempPreset`;
  `writeMachineConfig(boilerTemp, steamTemp, ...)` -> `(brewTempPreset, steamTempPreset, ...)`.

### Removed
- `closeConnection()` CCCD disable write (`disconnectCharacteristic`): it targeted 0x1403 (no CCCD
  until now -> no-op); firmware resets CCCDs on disconnect.
- Dead commented `dataToRead` collector in `RemoteControlViewModel.subscribeToChanges()`.

### Breaking / action
- Requires firmware with TODO #23. GATT handles after 0x1403 shifted +1: if notifications don't
  arrive, clear the phone's GATT cache (toggle Bluetooth / unpair).

## 2026-09-03 — Write-path BUSY-drop fix (boiler temp + preset sync)

`STATUS.md` TODO item 4 (assessment) / item 4.1 (fix). Confirmed the Write
button wiring in `RemoteControlScreen.kt` / `ControllerParameterScreen.kt` was
unaffected by the 2026-09-01 changes, but found the fix itself had introduced
an unserialized double-write that could silently drop under contention.

### Fixed
- **`txNewBoilerTargetTemp()`'s two writes no longer race.** Since 2026-09-01
  it dispatches both `0x1403` (active setpoint) and `0x1404` (brew preset) for
  one call. Android's GATT stack allows only one outstanding operation at a
  time; `writeCharacteristicsDevice()` discarded the boolean return of
  `gatt.writeCharacteristic()`, so the second write could return `false`
  (BUSY) and be silently dropped — no exception, no log, `onCharacteristicWrite`
  for `0x1404` never fires — while the UI still reported success. The `0x1404`
  write now only dispatches after the `0x1403` write has actually left the
  stack (`app/src/main/java/com/example/blespresso/data/ble/RemoteControlBLEReceiveManager.kt`).
- **`writeCharacteristicsDevice()` now returns a `WriteDispatchResult`**
  (`DISPATCHED / NOT_CONNECTED / BUSY`) instead of discarding
  `gatt.writeCharacteristic()`'s return value.
- **A transient BUSY write dispatch is retried**, not dropped —
  `writeWithRetry()` retries up to 5x with a 150 ms backoff (same pattern as
  the read queue's BUSY retry from 2026-09-01), calling `onDone` once
  dispatched or once retries are exhausted so callers can safely chain a
  follow-up write.

### Docs
- `STATUS.md` TODO item 4 marked done (assessment); new item 4.1 added
  documenting the risk and marked done (fix applied).

---

## 2026-09-01 — Machine-config read UUID fix + read-queue/screen-reentry hardening

`STATUS.md` TODO items 2 and 3. App's machine-config (`0x1400`) UUID map was off
by two against the firmware GATT table, the sequential read queue could abort
mid-chain or clobber a concurrent read op, and re-entering a screen silently
duplicated BLE subscriptions and left the Controller screen's Read button inert.

### Fixed
- **Machine-config (`0x1400`) UUID map corrected** to match `ble_cus.h`/`ble_cus.c`:
  steam preset moved `0x140A` -> `0x1405`; brew params shifted
  `0x1404`-`0x1409` -> `0x1406`-`0x140B`; added missing `0x1404` brew-temp-preset
  UUID (`txNewBoilerTargetTemp()` now writes both `0x1403` active setpoint and
  `0x1404` preset so they stay in sync)
  (`app/src/main/java/com/example/blespresso/data/ble/RemoteControlBLEReceiveManager.kt`).
  Previously `0x140A` (declining-pressure power) was misread/mislabeled as
  "steam setpoint", and the read chain crashed (`StringIndexOutOfBounds`) on the
  5th characteristic because a mis-mapped UUID's digit-count parser didn't match
  its actual payload length.
- **`onCharacteristicRead` no longer aborts the whole read chain on one bad
  characteristic** — checks `status == GATT_SUCCESS` before parsing, wraps the
  parse in try/catch; a failed or malformed char is logged and skipped instead
  of stopping subsequent reads.
- **Read queue rewritten as a front-to-back `ArrayDeque`** built only from
  characteristics that actually exist on the device (`startReadQueue()`),
  replacing the previous hard-coded tail-index bookkeeping.
  `rxReadMachineConfiguration()` / `rxReadPIDConfiguration()` now just declare
  their UUID list.
- **Two concurrent read operations no longer clobber each other.** Machine-config
  and PID-config reads share one GATT read queue; if one is requested while the
  other is in flight, it is now held as a single-slot `pendingReadRequest` and
  auto-started once the current queue drains, instead of `readQueue.clear()`
  silently dropping the in-flight op's remaining characteristics.
- **Transient GATT-busy dispatch is retried, not treated as a permanent skip.**
  `readCharacteristicsDevice()` now returns `DISPATCHED / BUSY / NOT_READABLE /
  NOT_CONNECTED`; `BUSY` (stack has another op in flight — Android GATT allows
  only one outstanding operation) retries the same characteristic up to 5x
  (150 ms backoff) before giving up, instead of instantly burning through the
  rest of the queue as "failed".
- **`RemoteControlViewModel.initiazeConnection()` re-entry guard added**
  (`isConnectionInitialized` flag) — it was called from a `LaunchedEffect(Unit)`
  on every screen that mounts it, and since the ViewModel is shared across
  screens, each screen visit re-subscribed to every BLE flow (stacking
  duplicate collectors on top of the still-running ones — observed 2-3x
  duplicate handling of a single BLE emission) and re-triggered
  `startConnection()` on an already-connected device, flapping `connectionState`
  and causing `LaunchedEffect(connectionState)` in `ControllerParameterScreen`
  to re-fire `rxReadPIDConfiguration()` a second time
  (`app/src/main/java/com/example/blespresso/presentation/RemoteControlViewModel.kt`).
- **Controller screen "Read" button now actually reads.** `onClick` only showed
  a Toast and never called `viewModel.rxReadPIDConfiguration()`
  (`app/src/main/java/com/example/blespresso/presentation/ControllerParameterScreen.kt`).

### Removed
- Dead `characteristicToReadQueue` / `characteristicToReadQueueIndex` /
  `characteristicToReadFLAG` fields and the commented-out
  `rxReadBrewConfiguration()` block
  (`.../data/ble/RemoteControlBLEReceiveManager.kt`).

### Docs
- `STATUS.md` TODO items 2 and 3 marked done.

---

## 2026-08-29 — PID Service characteristic rework

Mirrors the firmware-side PID Service (`0x1500`) rework. App now implements **6**
PID characteristics (`0x1501`–`0x1506`), value lengths `4 / 3 / 4 / 3 / 3 / 3` B.

### Breaking
- **BLE PID Service (`0x1500`) layout changed** — this build is only compatible
  with firmware carrying the new 6-characteristic table. Older firmware exposes
  `0x1504` I Windup / `0x1506` D LPF / `0x1507` Gain, which this app no longer
  reads or writes; `0x1504` is now interpreted as D Term.

### Removed
- UUID consts `BLE_CHAR_PID_I_TERM_WINDUP_UUID` (`0x1504`),
  `BLE_CHAR_PID_D_TERM_LPF_UUID` (`0x1506`), `BLE_CHAR_PID_GAIN___UUID` (`0x1507`)
  (`app/src/main/java/com/example/blespresso/data/ble/RemoteControlBLEReceiveManager.kt`).
- Flows `dataPIDcfgWindup`, `dataPIDcfgDlpfterm`, `dataPIDcfgGainterm` and writers
  `txWritePidWindup`, `txWritePidDLpf`, `txWritePidGain`
  (`.../data/RemoteControlReceiveManager.kt`, `.../data/ble/RemoteControlBLEReceiveManager.kt`).
- Struct fields `cfgWindup`, `cfgDlpfterm`, `cfgGainterm`
  (`app/src/main/java/com/example/blespresso/presentation/RemoteContolScreenData.kt`).
- ViewModel bounds `PID_WINDUP_*`, `PID_LPF_*`, `PID_GAIN_*` and the matching
  collectors / pass-through fns
  (`.../presentation/RemoteControlViewModel.kt`).
- UI rows "Integral Windup", "LPF Freq. Cut" (+ its "Hz" unit), "System Gain"
  (`.../presentation/ControllerParameterScreen.kt`).
- Dead PID `CTextBox` declarations that were never rendered
  (`.../presentation/RemoteControlScreen.kt`).

### Changed
- D Term BLE char moved `0x1505` -> `0x1504`; `BLE_CHAR_PID_D_TERM_UUID` now ends
  in `...1504` (`.../data/ble/RemoteControlBLEReceiveManager.kt`).
- `rxReadPIDConfiguration()` sequential read queue rebuilt for the 6 UUIDs;
  `characteristicToReadQueueIndex` `6` -> `5`
  (`.../data/ble/RemoteControlBLEReceiveManager.kt`).
- `ViewModel.writePIDConfig()` signature
  `(pTerm, iTerm, iMax, windup, dTerm, dLpf, gain)` ->
  `(pTerm, iTerm, iMax, dTerm, pBoost, iBoost)`
  (`.../presentation/RemoteControlViewModel.kt`).
- `C_PIDConfigData` reduced to `cfgPterm, cfgIterm, cfgImaxterm, cfgDterm,
  cfgPboost, cfgIboost` (`.../presentation/RemoteContolScreenData.kt`).

### Added
- BLE char `0x1505` P Boost -> flow `dataPIDcfgPboost`, writer `txWritePidPBoost`
  (3-digit ASCII), field `cfgPboost`, bounds `PID_PBOOST_MIN/MAX` (0–10),
  UI row "P-Gain Boost".
- BLE char `0x1506` I Boost -> flow `dataPIDcfgIboost`, writer `txWritePidIBoost`
  (3-digit ASCII), field `cfgIboost`, bounds `PID_IBOOST_MIN/MAX` (0–20),
  UI row "I-Gain Boost".
  Reason: the phase-1 P/I gain-boost multipliers are now firmware-configurable
  over BLE (`pid_Pboost` default 1.0, `pid_Iboost` default 6.5).

### Follow-up (not yet done)
- `docs/architecture/flows/controller-param-read/diagram.svg` and
  `.../controller-param-write/diagram.svg` are generated from the `.mermaid`
  sources and are still stale — regenerate.
- `STATUS.md` TODO item 2 (widen I Term / P Boost / I Boost value length
  3 B -> 4 B) is marked superseded; revisit if firmware changes its mind.

### Docs
- Updated: `docs/GATT_Table.md`,
  `docs/ProjectRequirementsDocument.md` (FR-29, §6.2, §13 ranges),
  `docs/ArchitectureAnalysis.md`,
  `docs/architecture/flows/controller-param-read/` (README + `.mermaid`),
  `docs/architecture/flows/controller-param-write/` (README + `.mermaid`),
  `STATUS.md`.
