# Changelog

All notable changes to BLEspresso_app are logged here, newest first.

Format: one dated section per change set. Each entry states **what** changed,
**why**, and any **migration / action required** (flash erase, app update, etc.).
Keep it terse. Link source files with repo-relative paths.

Categories: `Added` `Changed` `Removed` `Fixed` `Docs` `Breaking`.

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
