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

- 6[x]  based on item #7 propose, the Ui style of the app. and the gaggia's brand style.
        propose the best suitable icon/image format to be display in the app.
        comsidere the option of using the idea of "iluminiation" on an icon to show an element is ON or there is power apply to it.
        For the colors, make the color code flexible/adjustable by the theme color. And try to match the color been used by gaggia brand.
          also create "//COLOR PALETTE " where the proposed color can be set and shall be applicable to icons, backgorund, text, textboxes and boilerTemperatureProgressIndicator()
          Goal: have three theme/palette colors 1st current one, 2nd light mode proposed by you, 3rd dark mode proposed by you.
        to display the mode the machine is currenlt in, i think text is the best option:
          mode : 0 CLASSIC, 1 PROFILE, 2 STEP FUNCTION
        For the icons/images, propose two options:
          state: 0x00 HEATING_ON, 0x01 HEATING_OFF, 0x02 READY, 0x03 BREWING (classic),
                 0x04 BREW_PREINFUSE, 0x05 BREW_INFUSE, 0x06 BREW_TAPER,
                 0x07 BREW_AUTOSTOP (profile), 0x08 STEAMING,
                 0x10 STEP_OFF, 0x11 STEP_ON
        Save icons/images inside docs/... and inside a new folder named: UX
        Done: docs/UX/README.md (style, palette, SVG->VectorDrawable, illumination, state table),
        option_a/ (line glyphs) + option_b/ (panel lamps), preview.html.
        Selected: Option A, except 0x04/0x05/0x06 = Option B (see docs/UX/icon_selection.md).
        Color.kt: palette #1/#3 (current, active), #7 Gaggia light, #8 Gaggia dark; new vals
        textbox_*, icon_* in each; all BasicTextFields use textbox_*.
        See: docs/CHANGELOG.md -> "2026-10-09 — UX proposal + Gaggia palettes (STATUS TODO #6)"

- 7[x]  Sync with firmware TODO #25 (gaggia_controller_git): 0x1401 machine status
        is READ + NOTIFY, 2 B binary (was 10 B unused).
        SCOPE: BLE + ViewModel logic only. NO UI changes (UI = TODO #9).
        Payload: byte0 = mode, byte1 = state (unsigned).
          mode : 0 CLASSIC, 1 PROFILE, 2 STEP
          state: 0x00 HEATING_ON, 0x01 HEATING_OFF, 0x02 READY, 0x03 BREWING (classic),
                 0x04 BREW_PREINFUSE, 0x05 BREW_INFUSE, 0x06 BREW_TAPER,
                 0x07 BREW_AUTOSTOP (profile), 0x08 STEAMING,
                 0x10 STEP_OFF, 0x11 STEP_ON
        Notify on change only (firmware hysteresis, no flicker). 0x1403 notify precedes
        0x1401 on setpoint change.
        CONTRACT (output consumed by TODO #9 / #10 — keep exactly):
          data class MachineStatus(val mode: Int, val state: Int)   // raw unsigned codes
          RemoteControlViewModel: var machineStatus by mutableStateOf<MachineStatus?>(null)
          null = not received yet. Unknown codes passed through raw (UI decides).
          No label / icon / color mapping in data or VM layer.
        Changes needed:
        - RemoteControlBLEReceiveManager.kt: add BLE_CHAR_BLESPRESSO_STATUS__UUID to
          CCCD_NOTIFY_UUIDS (0x1401, 0x1402, 0x1403); fix comment "TODO #24" -> "#25".
        - READ 0x1401 once in initial sync: appended to rxReadMachineConfiguration() queue.
        - onCharacteristicChanged / onCharacteristicRead: len == 2 -> MachineStatus(mode, state)
          (bytes as unsigned: toInt() and 0xFF); other len -> ignore + log.
        - Emit via new flow dataMachineStatus (dataToRead is 0x1403-only); VM sets machineStatus.
          Reset to null on data-flow Loading / Error (disconnect, reconnect).
        - Log.d every update: "0x1401 mode=<m> state=0x<ss>".
        - GATT handles unchanged (0x1401 CCCD existed); only value length changes.
        DONE WHEN: build passes; Logcat shows mode/state on connect (initial READ) and on each
        machine change; screen unchanged.
        CHANGELOG: add entry only after code is implemented.
        Code done (2026-10-11), build passes: MachineStatus (BoilerDataCharacteristics.kt),
        dataMachineStatus flow (interface + BLE manager), emitMachineStatus() helper, 0x1401 in
        CCCD list + machine-config read queue, VM machineStatus (private set).
        Verified on hardware 2026-10-11: CCCD 0x1401 status=0; initial read mode=1 state=0x00;
        profile shot notify 0x04 -> 0x05 -> 0x06 -> 0x07 -> 0x00, one notify per change.
        See: docs/CHANGELOG.md -> "2026-10-11 — 0x1401 machine status logic (STATUS TODO #7)"

- 8[x]  Read current UX layout for RemoteControlScreen and propose three different layouta on 
        where to add the slots and text display to display icons and machine state. Use and .html to show a preview.
        Done: docs/UX/layout_preview.html (L1 strip, L2 dial corners, L3 header + side column),
        STEAMING own slot; summary in docs/UX/README.md §7. Selected: L3, side-by-side variant
        (dial + 16 dp + slot column centred as group; min width 348 dp; pinned-right rejected).
        Buttons: Controller in Read/Write row, left; Read + Write right; separate Controller row
        removed. Button row directly under brew table, ~14 dp vertical padding, not at screen bottom.

- 9[x]  Integrate 0x1401 machine status with UX on RemoteControlScreen.
        DEPENDS ON: #7 (MachineStatus contract). May start earlier with a fake MachineStatus
        (@Preview / hardcoded VM value); merge only after #7 works.
        INPUT: docs/UX/README.md §8 ONLY (palette, icons, slots, glow/pulse, layout, buttons,
        do-nots). Other docs/UX files = visual reference only.
        Consumes: viewModel.machineStatus (MachineStatus?) — no BLE code changes here.
        Integration seam: README §8.0 (stateless composables + one mapping function).
        Includes: switch active palette to #7 Gaggia light (README §8.1b).
        Includes: new Color.kt val on_accent; button text = on_accent on RemoteControlScreen
        and ControllerParameterScreen (contrast on Gaggia red, README §8.1b).
        Decisions fixed in README §8: state labels + contentDescription (§8.2), glow on glyph
        only for 0x04–0x06 lamp icons (§8.4), Gaggia hex approximations accepted.
        Confirmed as written: idle slot labels (§8.3), sizes 13 sp / 10 sp / 30 dp (§8.3, §8.5),
        glow 0.4 alpha / ~18 dp / 2 s pulse (§8.4), "—" + all slots idle when no status (§8).
        DONE WHEN: README §8.7 checklist passes.
        CHANGELOG: add entry only after code is implemented.
        Code done (2026-10-11), build passes with palette #1/#3, #7 (active) and #8:
        - Color.kt: #7 active; on_accent added to all 3 palettes.
        - New presentation/MachineStatusUi.kt: state map (§8.2), StatusTopBar(status),
          StatusSlotColumn(status), glow/pulse modifier, @Previews (all states + null).
        - RemoteControlScreen.kt: top bar; dial + 16 dp + slot column centred; Controller moved
          into Read/Write row (left), old Controller row deleted; row padding 14 dp; button
          text on_accent.
        - ControllerParameterScreen.kt: button text on_accent.
        Verified 2026-10-11: Android Studio previews OK; changes confirmed on hardware.
        See: docs/CHANGELOG.md -> "2026-10-11 — 0x1401 machine status on screen (STATUS TODO #9)"

- 10[x] Block WRITE, READ and CONTROLLER buttons / tx* calls while machine brewing or steaming.
        DEPENDS ON: #9 (status visible on screen). Uses machineStatus from #7.
        Spec: docs/UX/README.md §8.8. Decisions:
        - Busy = state 0x03..0x06 or 0x08. Not busy: 0x00-0x02, 0x07 SHOT DONE, 0x10/0x11,
          unknown, null.
        - RemoteControlScreen: Read, Write, Controller disabled. ControllerParameterScreen: Read,
          Write disabled (Main stays enabled). All textboxes read-only.
        - Inline "Machine busy" text above the button row (both screens).
        - VM guards writeMachineConfig / writePIDConfig. Reads NOT guarded in VM (auto read on
          connect fetches 0x1401); Read blocked at the button.
        DONE WHEN: during 0x03-0x06 / 0x08 buttons greyed, textboxes not editable, double-tap
        Write sends nothing, "Machine busy" shown; re-enabled at 0x07 / heating / null.
        Code done (2026-10-12), build passes: RemoteControlViewModel.isMachineBusy + write guards;
        both screens: enabled/readOnly/message; Write tap gestures guarded (pointerInput(isBusy)).
        Verified on hardware 2026-10-12.
        See: docs/CHANGELOG.md -> "2026-10-12 — Busy lock while brewing / steaming (STATUS TODO #10)"

- 11[x] Color.kt `activePalette` is Compose state; all color getters
        make sure the whole app follows `activePalette`. In preparation for Item #13
        Audit 2026-10-12: leaks = empty Material colors in Theme.kt (unstyled permission / error
        text + "Try again" button used Material defaults) and no root background.
        Code done, build passes: Theme.kt builds MaterialTheme.colors from activePalette, root
        Surface(background) + LocalContentColor = textprimary; unused darkColors/lightColors and
        isSystemInDarkTheme param removed. Exception kept: dial >105 °C red band.
        Rule in docs/UX/README.md §2.
        Verified on device 2026-10-12 with PALETTE_GAGGIA_DARK: no leftover colors.
        See: docs/CHANGELOG.md -> "2026-10-12 — Whole app follows activePalette (STATUS TODO #11)"

- 12[x] Housekeeping: copy icon/preview/drawable generator scripts to docs/UX/tools/
        (gen_icons.py, gen_preview.py, gen_layout.py, svg2vd.py, svg2vd_b.py; currently only in
        the Claude session scratchpad). Make paths relative to the script folder.
        Done: scripts in docs/UX/tools/ + tools/README.md (run order). Paths relative;
        svg2vd.py skips 0x04–0x06 (Option B, handled by svg2vd_b.py). Re-run reproduces all
        current SVGs, previews and drawables byte-identical.
      
- 13[]  add a swtich at the top right corner to switch theme colors (light <-> dark).
        code in app only, ignore system.

- 14[]  Propose a plan to decouple: Permissions (permissionState.allPermissionsGranted) and BLE connection status (bleConnectionState == ConnectionState.CurrentlyInitializing); from RemoteControlScreen.kt
        assess if creating a new screen is an elegant/easy implementation. Also, when user pressed "CONNECT TO ESPRESSO MACHINE" and there is no BLE devices found after 10seconds, it shall prompt a message, asking for permision to pass BLe connection, so user can experience the app without interaction; for this particular case; app shall inserts "0" in all paramater from the app and BLE CHAR. 
        Block WRITE, READ in RemoteControlScreen.kt and ControllerParameterScreen.kt

- 15[]  App shall show at the bottom and at all screens: [App_version] and [Company_Name]

- 16[]  Assess current application's code structure for: BLE functions, Kotlin and android. propose a plan to re-arrange the code to aligned it against good practices and to make it more efficient.


- []
- []    what onMtuChanged does? explain



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