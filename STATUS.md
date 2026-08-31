# BLEspresso_app
Project BLEspresso_app_git is an Android app to controll the BLEspresso controller

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

- 2[] check the GATT from BLE device (confirm on ble_cus.c) identify the problem and propose a plan to fix on the Android app . 
        Problem: App tries to read 8 char but failed at 5th char. Also implement a logic that does not abort read operation but continues with next CHAR UUID.
        NOTE: the code was working previoues or it is known to work correctly.
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