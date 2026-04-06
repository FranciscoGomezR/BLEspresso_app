# Flow: READ Button — RemoteControlScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**Trigger:** User taps the **Read** button in the Machine Configuration section of `RemoteControlScreen`.  
**Ends:** All 8 machine-configuration text fields are populated with values read from the BLE device.

---

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Screen as RemoteControlScreen<br/>(Composable)
    participant VM as RemoteControlViewModel
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant GATT as BluetoothGatt (BLE Device)

    User->>Screen: Tap "Read" button
    Screen->>VM: viewModel.rxReadMachineConfiguration()

    activate VM
    VM->>VM: async { machineConfigReadComplete.first() }
    VM->>BLEMgr: rxReadMachineConfiguration()
    activate BLEMgr

    Note over BLEMgr: Builds sequential read queue (8 characteristics)<br/>Order (read first → last):<br/>1. Boiler Temp Setpoint (…1403)<br/>2. Steam Temp Setpoint  (…140A)<br/>3. Pre-Infusion Power   (…1404)<br/>4. Pre-Infusion Time    (…1405)<br/>5. Infusion Power       (…1406)<br/>6. Infusion Time        (…1407)<br/>7. Declining Pwr        (…1408)<br/>8. Declining Time       (…1409)

    BLEMgr->>GATT: readCharacteristic(char[7]) ← Boiler Setpoint

    loop For each characteristic in queue (tail → head)
        GATT-->>BLEMgr: onCharacteristicRead(uuid, value)
        BLEMgr->>BLEMgr: Decode ASCII bytes → Float
        BLEMgr->>BLEMgr: emit(Resource.Success(data)) on matching SharedFlow
        alt More items in queue
            BLEMgr->>GATT: readCharacteristic(next char)
        else Queue empty (index == -1)
            BLEMgr->>BLEMgr: machineConfigReadComplete.emit(Unit)
        end
    end

    deactivate BLEMgr

    VM->>VM: completed.await() ← unblocks when machineConfigReadComplete fires
    VM->>VM: _machineConfigLoaded.emit(Unit)
    deactivate VM

    Note over VM: Individual SharedFlows are collected continuously<br/>by subscribeToReadOperation() coroutines:

    par Collect each characteristic SharedFlow
        VM->>VM: dataToRead → boilerSetpointTemp
    and
        VM->>VM: dataSteamSetpoint → steamTempSetpoint
    and
        VM->>VM: dataBrewCfgPreInfusionPwr → brewConfigData.PreInfussionPwr
    and
        VM->>VM: dataBrewCfgPreInfusionTmr → brewConfigData.PreInfussionTmr
    and
        VM->>VM: dataBrewCfg___InfusionPwr → brewConfigData.InfussionPwr
    and
        VM->>VM: dataBrewCfg___InfusionTmr → brewConfigData.InfussionTmr
    and
        VM->>VM: dataBrewCfgDeclining__Pwr → brewConfigData.DecliningPwr
    and
        VM->>VM: dataBrewCfgDeclining__Tmr → brewConfigData.DecliningTmr
    end

    Screen->>Screen: LaunchedEffect collects machineConfigLoaded
    Screen->>Screen: cBoilerTB.loadValue(boilerSetpointTemp)
    Screen->>Screen: cSteamTempTB.loadValue(steamTempSetpoint)
    Screen->>Screen: cPreinfusionPWR_TB.loadValue(PreInfussionPwr)
    Screen->>Screen: cPreinfusionTMR_TB.loadValue(PreInfussionTmr)
    Screen->>Screen: cInfusionPWR_TB.loadValue(InfussionPwr)
    Screen->>Screen: cInfusionTMR_TB.loadValue(InfussionTmr)
    Screen->>Screen: cDeclinPrPWR_TB.loadValue(DecliningPwr)
    Screen->>Screen: cDeclinPrTMR_TB.loadValue(DecliningTmr)
    Note over Screen: All 8 BasicTextFields show values from device.<br/>CTextBox.modified = false for each field.
```

---

## Auto-Read on Connect

`RemoteControlScreen` also triggers a read automatically when the BLE device first connects:

```mermaid
sequenceDiagram
    participant Screen as RemoteControlScreen
    participant VM as RemoteControlViewModel

    Screen->>Screen: LaunchedEffect(bleConnectionState)
    Note right of Screen: Fires when connectionState changes
    alt bleConnectionState == Connected
        Screen->>VM: viewModel.rxReadMachineConfiguration()
        Note right of VM: Same flow as manual Read button
    end
```

---

## Key Classes / SharedFlows

| SharedFlow | Carries | ViewModel state updated |
|------------|---------|------------------------|
| `dataToRead` | `BoilerSetpoint` | `boilerSetpointTemp` |
| `dataSteamSetpoint` | `BoilerSetpoint` | `steamTempSetpoint` |
| `dataBrewCfgPreInfusionPwr` | `CharDataClass` | `brewConfigData.PreInfussionPwr` |
| `dataBrewCfgPreInfusionTmr` | `CharDataClass` | `brewConfigData.PreInfussionTmr` |
| `dataBrewCfg___InfusionPwr` | `CharDataClass` | `brewConfigData.InfussionPwr` |
| `dataBrewCfg___InfusionTmr` | `CharDataClass` | `brewConfigData.InfussionTmr` |
| `dataBrewCfgDeclining__Pwr` | `CharDataClass` | `brewConfigData.DecliningPwr` |
| `dataBrewCfgDeclining__Tmr` | `CharDataClass` | `brewConfigData.DecliningTmr` |
| `machineConfigReadComplete` | `Unit` (signal) | triggers `_machineConfigLoaded` |
