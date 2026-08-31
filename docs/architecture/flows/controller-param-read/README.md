# Flow: READ Button — ControllerParameterScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**Trigger:** The screen is navigated to (composable first shown) **or** the user taps the **Read** button.  
**Ends:** All 6 PID parameter text fields are populated with values read from the BLE device.

> The READ on this screen is largely **automatic**: `LaunchedEffect(viewModel.connectionState)` calls `rxReadPIDConfiguration()` the moment the BLE connection is `Connected`.

---

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Screen as BlespressoCtrlParamScreen<br/>(Composable)
    participant VM as RemoteControlViewModel
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant GATT as BluetoothGatt (BLE Device)

    Note over Screen: Screen enters composition (navigated to)

    Screen->>VM: LaunchedEffect(Unit) → viewModel.initiazeConnection()
    VM->>VM: subscribeToChanges()<br/>subscribeToReadOperation()
    VM->>BLEMgr: startConnection() [if not already connected]

    Note over BLEMgr,GATT: BLE scan → connect → discover services (see Navigation flow)

    GATT-->>Screen: connectionState changes to Connected
    Screen->>Screen: LaunchedEffect(viewModel.connectionState)
    alt connectionState == Connected
        Screen->>VM: viewModel.rxReadPIDConfiguration()
    end

    activate VM
    VM->>VM: async { pidConfigReadComplete.first() }
    VM->>BLEMgr: rxReadPIDConfiguration()
    activate BLEMgr

    Note over BLEMgr: Builds sequential queue (6 PID characteristics)<br/>Order (read first → last):<br/>1. P Term   (…1501)<br/>2. I Term   (…1502)<br/>3. I Max    (…1503)<br/>4. D Term   (…1504)<br/>5. P Boost  (…1505)<br/>6. I Boost  (…1506)

    BLEMgr->>GATT: readCharacteristic(P Term) ← index 5

    loop For each PID characteristic (tail → head)
        GATT-->>BLEMgr: onCharacteristicRead(uuid, ASCII bytes)
        BLEMgr->>BLEMgr: Decode ASCII → Float
        BLEMgr->>BLEMgr: emit(Resource.Success) on matching SharedFlow
        alt More items remain
            BLEMgr->>GATT: readCharacteristic(next)
        else Queue empty (index == -1)
            BLEMgr->>BLEMgr: pidConfigReadComplete.emit(Unit)
        end
    end

    deactivate BLEMgr

    VM->>VM: completed.await() ← unblocks on pidConfigReadComplete
    VM->>VM: _pidConfigLoaded.emit(Unit)
    deactivate VM

    Note over VM: subscribeToReadOperation() coroutines continuously collect PID SharedFlows:

    par Collect PID SharedFlows
        VM->>VM: dataPIDcfgPterm   → pIDConfigData.cfgPterm
    and
        VM->>VM: dataPIDcfgIterm   → pIDConfigData.cfgIterm
    and
        VM->>VM: dataPIDcfgImaxterm → pIDConfigData.cfgImaxterm
    and
        VM->>VM: dataPIDcfgDterm   → pIDConfigData.cfgDterm
    and
        VM->>VM: dataPIDcfgPboost  → pIDConfigData.cfgPboost
    and
        VM->>VM: dataPIDcfgIboost  → pIDConfigData.cfgIboost
    end

    Screen->>Screen: LaunchedEffect(Unit) collects pidConfigLoaded
    Screen->>Screen: cPterm_TB.loadValue(cfgPterm)
    Screen->>Screen: cIterm_TB.loadValue(cfgIterm)
    Screen->>Screen: cIlimit_TB.loadValue(cfgImaxterm)
    Screen->>Screen: cDterm_TB.loadValue(cfgDterm)
    Screen->>Screen: cPboost_TB.loadValue(cfgPboost)
    Screen->>Screen: cIboost_TB.loadValue(cfgIboost)

    Note over Screen: All 6 CTextBox fields show current PID values.<br/>modified = false for each.
```

---

## Manual READ Button

When the user explicitly taps **Read**:

```mermaid
sequenceDiagram
    participant User
    participant Screen as BlespressoCtrlParamScreen
    participant VM as RemoteControlViewModel

    User->>Screen: Tap "Read" button
    Screen->>Screen: onClick → Toast "Reading PID configuration"
    Note over Screen: rxReadPIDConfiguration() is NOT called again<br/>from the button onClick in the current implementation.<br/>The auto-read on connect is the primary mechanism.
```

> The button currently shows a toast only. The actual PID read is initiated automatically on connection.

---

## Key SharedFlows

| SharedFlow | Carries | ViewModel state |
|------------|---------|-----------------|
| `dataPIDcfgPterm` | `CharDataClass` | `pIDConfigData.cfgPterm` |
| `dataPIDcfgIterm` | `CharDataClass` | `pIDConfigData.cfgIterm` |
| `dataPIDcfgImaxterm` | `CharDataClass` | `pIDConfigData.cfgImaxterm` |
| `dataPIDcfgDterm` | `CharDataClass` | `pIDConfigData.cfgDterm` |
| `dataPIDcfgPboost` | `CharDataClass` | `pIDConfigData.cfgPboost` |
| `dataPIDcfgIboost` | `CharDataClass` | `pIDConfigData.cfgIboost` |
| `pidConfigReadComplete` | `Unit` (signal) | triggers `_pidConfigLoaded` |
