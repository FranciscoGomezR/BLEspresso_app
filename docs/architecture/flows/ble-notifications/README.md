# Flow: BLE Notifications → RemoteControlScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**Trigger:** The BLE device sends a GATT Notification on the Boiler Water Temperature characteristic (`…1402`) after the app enables notifications on connect.  
**Ends:** `BoilerTemperatureProgressIndicator` recomposes with the new temperature.

---

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    participant BLE as BLE Device<br/>(GATT Server)
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant Flow as data: MutableSharedFlow
    participant VM as RemoteControlViewModel
    participant Screen as RemoteControlScreen<br/>(Composable)
    participant Indicator as BoilerTemperatureProgressIndicator

    Note over BLE,BLEMgr: Connection already established, notifications enabled
    BLE->>BLEMgr: onCharacteristicChanged(uuid=1402, value=ASCII 4-byte)
    activate BLEMgr
    BLEMgr->>BLEMgr: Decode ASCII bytes → Float temperature<br/>e.g. "0935" → 93.5 °C
    BLEMgr->>BLEMgr: BoilerDataResult(temperature, Connected)
    BLEMgr->>Flow: data.emit(Resource.Success(BoilerDataResult))
    deactivate BLEMgr

    activate VM
    VM->>Flow: collect { result }
    Flow-->>VM: Resource.Success(BoilerDataResult)
    VM->>VM: boilerWaterTemp = result.data.waterTemperature
    VM->>VM: connectionState = Connected
    deactivate VM

    Note over Screen: Composable observes viewModel.boilerWaterTemp<br/>Recomposition triggered automatically by mutableStateOf
    Screen->>Indicator: BoilerTemperatureProgressIndicator(<br/>  boilerWaterTemp = viewModel.boilerWaterTemp,<br/>  boilerTargetTemp = viewModel.boilerSetpointTemp<br/>)
    Indicator->>Indicator: Draw arc sweep = (270° / maxValue) × temperature<br/>Draw tick marks coloured to indicate setpoint<br/>Render centre text: "{temp}°C"
```

---

## Notification Enable Path (one-time on connect)

```mermaid
sequenceDiagram
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant GATT as BluetoothGatt

    BLEMgr->>GATT: onMtuChanged callback fires (MTU=517)
    BLEMgr->>BLEMgr: findCharacteristic(SERVICE_UUID, BOILER_WATER_TEMP_UUID)
    BLEMgr->>BLEMgr: enableNotification(characteristic)
    BLEMgr->>GATT: setCharacteristicNotification(characteristic, true)
    BLEMgr->>GATT: writeDescriptor(CCCD, ENABLE_NOTIFICATION_VALUE)
    Note over BLEMgr,GATT: Device now pushes onCharacteristicChanged for every temperature update
```

---

## Data Classes Involved

| Class | Role |
|-------|------|
| `BoilerDataResult` | Carries `waterTemperature: Float` + `connectionState` |
| `Resource<T>` | Wrapper — `Success`, `Loading`, or `Error` |
| `ConnectionState` | Enum tracked on the ViewModel (`Connected`, `Disconnected`, …) |

---

## `BoilerTemperatureProgressIndicator` — Rendering Logic

| Aspect | Detail |
|--------|--------|
| Arc sweep | `(270° / maxValue) × boilerWaterTemp` starting at 135° |
| Tick marks | 151 ticks (0–150); red above 105 °C up to target; primary colour below target; faded above |
| Centre text | `"{boilerWaterTemp}°C"` (large) + `"Target: {boilerTargetTemp}°C"` (small) |
| Target pointer | A larger tick mark drawn at `boilerTargetTemp` angle |
