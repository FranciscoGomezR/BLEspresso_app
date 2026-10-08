# BLEspresso_app
Project BLEspresso_app_git is an Android app to controll the BLEspresso controller
Everyt time a developer or AID agent works on an item in this list shall update STATUS.md and CHANGELOG.md doc (Link TODO item to its log entry inside CHANGELOG.md for good tracebility)

# TODO LIST
- 1[x] Update project [BLEspresso_app_git] to match new gatt table for PID service (done 2026-08-29):
        app now implements 6 PID characteristics 0x1501-0x1506
        (pid_Pterm/Iterm/Imaxterm/Dterm/Pboost/Iboost). Removed I-Windup (old 0x1504),
        D-LPF (old 0x1506), PID Gain (old 0x1507). D-term moved 0x1505 -> 0x1504.
        Value lengths follow the attribute table below: 4/3/4/3/3/3 bytes.
        Files: RemoteControlBLEReceiveManager.kt, RemoteControlReceiveManager.kt,
        RemoteContolScreenData.kt, RemoteControlViewModel.kt, ControllerParameterScreen.kt,
        RemoteControlScreen.kt + docs/.
        this is the new GATT table for ## Service 2 — PID Service, **Service UUID:** `0x1500`
        ## Service 2 — PID Service
        **Service UUID:** `0x1500`  
        **UUID Base:** `CUSTOM_PID_SERVICE_UUID_BASE` — `{0x22,0x21,0x20,...,0x11,0x00,0x00,0x00,0x00}` (LE byte order)  
        **Registered in:** `ble_cus_init()` → `ble_cus_controller_char_add()`  
        **Number of characteristics:** 6
        ## Service Attribute Table
        | Handle Offset | Attribute Type | UUID | Properties | Len (bytes) | Data Format | `blEspressoProfile` Field | Notes |
        |---|---|---|---|---|---|---|---|
        | S2+0 | Primary Service Declaration (`0x2800`) | `0x1500` | — | 2 | UUID | — | Service boundary |
        | **S2+1** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1501` |
        | **S2+2** | **Characteristic Value** | `0x1501` | Read, Write | 4 | 4-char ASCII (`XXXD`, D=tenth) | `pid_Pterm` | Proportional gain |
        | **S2+3** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1502` |
        | **S2+4** | **Characteristic Value** | `0x1502` | Read, Write | 3 | 3-char ASCII (`XXD`, D=tenth) | `pid_Iterm` | Integral gain |
        | **S2+5** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1503` |
        | **S2+6** | **Characteristic Value** | `0x1503` | Read, Write | 4 | 4-char ASCII (`XXXD`, D=tenth) | `pid_Imaxterm` | Integral limit |
        | **S2+7** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1504` |
        | **S2+8** | **Characteristic Value** | `0x1504` | Read, Write | 3 | 3-char ASCII (`XXD`, D=tenth) | `pid_Dterm` | Derivative gain |
        | **S2+9** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1505` |
        | **S2+10** | **Characteristic Value** | `0x1505` | Read, Write | 3 | 3-char ASCII (`XXD`, D=tenth) | `pid_Pboost` | Phase-1 proportional-gain boost multiplier |
        | **S2+11** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1506` |
        | **S2+12** | **Characteristic Value** | `0x1506` | Read, Write | 3 | 3-char ASCII (`XXD`, D=tenth) | `pid_Iboost` | Phase-1 integral-gain boost multiplier |

- 2[x] check the GATT from BLE device (confirm on ble_cus.c) identify the problem and propose a plan to fix on the Android app . 
        Problem: App tries to read 8 char but failed at 5th char. Also implement a logic that does not abort read operation but continues with next CHAR UUID.
        NOTE: the code was working previoues or it is known to work correctly.
        See: docs/CHANGELOG.md -> "2026-09-01 — Machine-config read UUID fix + read-queue/screen-reentry hardening"
        ## Service 1 — BLEspresso Brew Service 
        **Service UUID:** `0x1400`  
        **Full UUID:** `f364adc9-b000-**1400**-ba50-05ca45bf8abc`  
        **Registered in:** `ble_cus_init()` → `custom_value_char_add()`  [confirmed that is running in the MCU] 
        **Number of characteristics:** 10
        ### Service Attribute Table
        | Handle Offset | Attribute Type | UUID | Properties | Len (bytes) | Data Format | `g_Espresso_user_config_s` Field | Notes |
        |---|---|---|---|---|---|---|---|
        | S1+0 | Primary Service Declaration (`0x2800`) | `0x1400` | — | 2 | UUID | — | Service boundary |
        | **S1+1** | Characteristic Declaration (`0x2803`) | — | Read, Notify | 5 | Props + Handle + UUID | — | Declares `0x1401` |
        | **S1+2** | **Characteristic Value** | `0x1401` | Read, Notify | 10 | ASCII string (space-padded) | — (status string) | Machine status string |
        | **S1+3** | Client Characteristic Configuration (`0x2902`) | — | Read, Write | 2 | `0x0000`=off `0x0001`=notify | — | Enables/disables notifications for `0x1401` |
        | **S1+4** | Characteristic Declaration (`0x2803`) | — | Read, Notify | 5 | Props + Handle + UUID | — | Declares `0x1402` |
        | **S1+5** | **Characteristic Value** | `0x1402` | Read, Notify | 4 | `XXXD` ASCII (D=tenth °C) | `boilerTempDegC` | Boiler water temperature |
        | **S1+6** | Client Characteristic Configuration (`0x2902`) | — | Read, Write | 2 | `0x0000`=off `0x0001`=notify | — | Enables/disables temp notifications (1 s period) |
        | **S1+7** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1403` |
        | **S1+8** | **Characteristic Value** | `0x1403` | Read, Write | 4 | `XXXD` ASCII (D=tenth °C) | `boilerTempSetpointDegC` | Active boiler setpoint |
        | **S1+9** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1404` |
        | **S1+10** | **Characteristic Value** | `0x1404` | Read, Write | 4 | `XXXD` ASCII (D=tenth °C) | `brewTempDegC` | Brew preset setpoint |
        | **S1+11** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1405` |
        | **S1+12** | **Characteristic Value** | `0x1405` | Read, Write | 4 | `XXXD` ASCII (D=tenth °C) | `steamTempDegC` | Steam preset setpoint |
        | **S1+13** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1406` |
        | **S1+14** | **Characteristic Value** | `0x1406` | Read, Write | 3 | `XXD` ASCII (D=tenth %) | `profPreInfusePwr` | Pre-infusion pump power |
        | **S1+15** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1407` |
        | **S1+16** | **Characteristic Value** | `0x1407` | Read, Write | 3 | `XXD` ASCII (D=tenth s) | `profPreInfuseTmr` | Pre-infusion duration |
        | **S1+17** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1408` |
        | **S1+18** | **Characteristic Value** | `0x1408` | Read, Write | 4 | `XXXD` ASCII (D=tenth %) | `profInfusePwr` | Infusion pump power |
        | **S1+19** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x1409` |
        | **S1+20** | **Characteristic Value** | `0x1409` | Read, Write | 3 | `XXD` ASCII (D=tenth s) | `profInfuseTmr` | Infusion duration |
        | **S1+21** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x140A` |
        | **S1+22** | **Characteristic Value** | `0x140A` | Read, Write | 4 | `XXXD` ASCII (D=tenth %) | `profTaperingPwr` | Declining pressure power |
        | **S1+23** | Characteristic Declaration (`0x2803`) | — | Read, Write | 5 | Props + Handle + UUID | — | Declares `0x140B` |
        | **S1+24** | **Characteristic Value** | `0x140B` | Read, Write | 3 | `XXD` ASCII (D=tenth s) | `profTaperingTmr` | Declining pressure duration |
        Logs when pressing "Read" Button: [attached]

- 3[x] After fixing item 2, reading still misbehaves when navigating between screens: entering
        ControllerParameterScreen right after using RemoteControlScreen's Read button causes the
        PID (0x1500) table to get requested twice, BLE data callbacks fire 2-3x each for a single
        notification, and the ControllerParameterScreen "Read" button does nothing when pressed.
        Root cause: `RemoteControlViewModel.initiazeConnection()` has no re-entry guard and is
        called from a `LaunchedEffect(Unit)` on every screen that mounts it; since the ViewModel is
        shared across screens, each screen visit re-subscribes to every BLE flow (stacking
        duplicate collectors on top of ones still running) and re-triggers `startConnection()` on
        an already-connected device, which flaps `connectionState` and makes
        `LaunchedEffect(connectionState)` in ControllerParameterScreen re-fire `rxReadPIDConfiguration()`
        a second time. Separately, the "Read" button's `onClick` only showed a Toast and never
        called `viewModel.rxReadPIDConfiguration()`.
        Fix: guard `initiazeConnection()` with an `isConnectionInitialized` flag so subscribe/connect
        only runs once per ViewModel lifetime; wire the "Read" button to actually call
        `viewModel.rxReadPIDConfiguration()`.
        Also root-caused/fixed as part of item 2's read-queue rework: two independent read
        operations (machine config vs PID config) sharing one GATT read queue clobbered each
        other when triggered close together, and a transient GATT-busy dispatch was treated as a
        permanent per-characteristic failure instead of being retried.
        See: docs/CHANGELOG.md -> "2026-09-01 — Machine-config read UUID fix + read-queue/screen-reentry hardening"

- 4[x] Check that changes made on the app to resolve TODO item 2 and item 3 did not break the logic for the WRITE button in both screen.kt or if the code for these buttons need to be update. Read CHANGELOG.md to support assessment.
        Result: Write button wiring in RemoteControlScreen.kt and ControllerParameterScreen.kt is
        unchanged and correct - both still call ViewModel.writeMachineConfig()/writePIDConfig(),
        which now point at the item-2-corrected UUIDs. No update needed there.
        See: docs/CHANGELOG.md -> "2026-09-01 — Machine-config read UUID fix + read-queue/screen-reentry hardening"

- 4.1[x] Risk found while assessing item 4: item 2's fix made `txNewBoilerTargetTemp()` fire TWO
        back-to-back `gatt.writeCharacteristic()` calls (0x1403 active setpoint, then 0x1404 brew
        preset) with no serialization. `writeCharacteristicsDevice()` discarded the boolean return
        of `gatt.writeCharacteristic()` - Android's GATT stack allows only one outstanding
        operation at a time, so if the 0x1404 write dispatched while the 0x1403 write was still in
        flight, it silently failed (no exception, no log, `onCharacteristicWrite` for 0x1404 never
        fires) while the "Write" button still showed "Sending modified configuration" as if it
        succeeded. Same class of bug as the read-side BUSY issue fixed in item 2/3, just never
        applied to writes.
        Fix: writeCharacteristicsDevice() now returns a WriteDispatchResult (mirrors
        ReadDispatchResult); BUSY dispatches retry (150ms backoff, 5x cap) instead of being
        dropped; txNewBoilerTargetTemp() chains the 0x1404 write to fire only after the 0x1403
        write actually dispatches.
        See: docs/CHANGELOG.md -> "2026-09-03 — Write-path BUSY-drop fix (boiler temp + preset sync)"
  
- 5[x]   Sync with firmware TODO #23 (gaggia_controller_git): 0x1403 active boiler setpoint is now
        READ + NOTIFY (write removed). Firmware sets it from 0x1404 brewTempDegC / 0x1405
        steamTempDegC on BREW/STEAM switch change and notifies it.
        Changes needed:
        - txNewBoilerTargetTemp(): drop 0x1403 write (now rejected: write not permitted);
          write 0x1404 brew preset only. 
        - Rename -> 0x1404 = brew_temp_preset; 0x1405 = steam_temp_preset.
        - Enable CCCD notify on 0x1403 (same as 0x1402 water temp); handle it in
          onCharacteristicChanged and emit to dataToRead flow.
        - Keep 0x1403 READ in initial sync sequence.
        - GATT handles shift: new 0x1403 CCCD adds one attribute, so all Brew-service attributes
          after it move +1. Lookup by UUID is fine; clear cached GATT (refresh / re-pair)
          if the phone cached the old table.
        - Upon a 0x1403 notification arrives, it needs to be refresh in the boilerTemperatureProgressIndicator
        - UI: active setpoint read-only display by boilerTemperatureProgressIndicator(boilerTargetTemp:Float). propose a common name to sync 0x1403 and boilerTargetTemp paramter of boilerTemperatureProgressIndicator().
        - Extra context info to be considered: 0x1401 will be a read + notify char that will notify to the APP 
          the status of the machine. at the end the app has to subscribe to 0x1401, 0x1402 & 0x1403 notifications. Currently RemoteControlScreen does not have anything implemented yet.
          nothing to do on regard this sub-point, just informative.
        Done: common name boilerTempSetpoint (0x1403 const, VM state, indicator param); presets
        brewTempPreset / steamTempPreset; CCCD queue [0x1402, 0x1403] (add 0x1401 in #24);
        brew textbox now loaded from 0x1404.
        See: docs/CHANGELOG.md -> "2026-10-07 — Sync with firmware TODO #23: 0x1403 READ + NOTIFY (STATUS TODO #5)"

- []    Block WRITE buttons / tx* calls while machine brewing or steaming. Steaming can be
          inferred from 0x1403 notify (setpoint == steam preset); brewing needs firmware
          TODO #24 status notify (0x1401).
- []    what onMtuChanged does? explain
- []


- [ ] (SUPERSEDED — keep 3 B per the attribute table above; do not apply) change PID controller parameter Length mentioned below: 
        | UUID | Name | Char Declaration | Char Value | CCCD | CUDD | Properties | Val Len | Default |
        |---|---|---|---|---|---|---|---|---|
        | `0x1502` | I Term | S2+3 | S2+4 | — | — | R, W | 3 B | From NVM / `pid_Iterm` (0.3) |
        | `0x1505` | P Boost | S2+9 | S2+10 | — | — | R, W | 3 B | From NVM / `pid_Pboost` (1.0) |
        | `0x1506` | I Boost | S2+11 | S2+12 | — | — | R, W | 3 B | From NVM / `pid_Iboost` (6.5) |
        to
        | UUID | Name | Char Declaration | Char Value | CCCD | CUDD | Properties | Val Len | Default |
        |---|---|---|---|---|---|---|---|---|
        | `0x1502` | I Term | S2+3 | S2+4 | — | — | R, W | 4 B | From NVM / `pid_Iterm` (0.3) |
        | `0x1505` | P Boost | S2+9 | S2+10 | — | — | R, W | 4 B | From NVM / `pid_Pboost` (1.0) |
        | `0x1506` | I Boost | S2+11 | S2+12 | — | — | R, W | 4 B | From NVM / `pid_Iboost` (6.5) |
        affected modules: ble_cus.h/ble_cus.c, ble_cus_init(), ble_cus_controller_char_add()