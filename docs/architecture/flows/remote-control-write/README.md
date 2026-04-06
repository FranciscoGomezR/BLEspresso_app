# Flow: WRITE Button — RemoteControlScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**Trigger:** User **double-taps** the **Write** button in the Machine Configuration section of `RemoteControlScreen`.  
**Ends:** Only modified fields are written to the BLE device via GATT `writeCharacteristic`.

> **Note:** A single tap shows a toast ("click") as feedback. Only a **double-tap** commits the write. This guards against accidental writes.

---

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Screen as RemoteControlScreen<br/>(Composable)
    participant VM as RemoteControlViewModel
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant GATT as BluetoothGatt (BLE Device)

    User->>Screen: Edit one or more CTextBox fields
    Note over Screen: CTextBox.onValueChange() marks modified=true<br/>CTextBox.onDone() clamps value to [min, max]

    User->>Screen: Double-tap "Write" button
    Screen->>Screen: detectTapGestures → onDoubleTap

    Screen->>Screen: For each of 8 fields:<br/>pass value if (modified && text.isNotBlank()) else null

    Screen->>VM: viewModel.writeMachineConfig(<br/>  boilerTemp, steamTemp,<br/>  preInfPwr, preInfTmr,<br/>  infPwr, infTmr,<br/>  declPwr, declTmr<br/>)

    activate VM
    VM->>VM: For each non-null argument → call txXxx()
    VM->>BLEMgr: txNewBoilerTargetTemp(boilerTemp)    [if modified]
    VM->>BLEMgr: txNewSteamTempSetpoint(steamTemp)    [if modified]
    VM->>BLEMgr: txWritePreInfusionPwr(preInfPwr)     [if modified]
    VM->>BLEMgr: txWritePreInfusionTmr(preInfTmr)    [if modified]
    VM->>BLEMgr: txWriteInfusionPwr(infPwr)           [if modified]
    VM->>BLEMgr: txWriteInfusionTmr(infTmr)           [if modified]
    VM->>BLEMgr: txWriteDecliningPwr(declPwr)         [if modified]
    VM->>BLEMgr: txWriteDecliningTmr(declTmr)         [if modified]
    VM-->>Screen: returns anyModified: Boolean
    deactivate VM

    activate BLEMgr
    Note over BLEMgr: writeCharacteristic(serviceUuid, charUuid, value, digits)<br/>1. dataConvertionFloatToCharacteristic(value, sb, digits)<br/>   Encodes Float → ASCII digit string (e.g. 93.5 → "0935")<br/>2. findCharacteristic(serviceUuid, charUuid)<br/>3. writeCharacteristicsDevice(char, payload.toByteArray())
    BLEMgr->>GATT: gatt.writeCharacteristic(characteristic)<br/>WRITE_TYPE_NO_RESPONSE
    GATT-->>BLEMgr: onCharacteristicWrite(uuid, GATT_SUCCESS)
    BLEMgr->>BLEMgr: Log write success per UUID
    deactivate BLEMgr

    alt anyModified == true
        Screen->>Screen: Reset modified=false for all 8 CTextBoxes
        Screen->>User: Toast "Sending modified configuration"
    else anyModified == false
        Screen->>User: Toast "No fields have been modified"
    end
```

---

## Value Encoding Details

| Characteristic | UUID suffix | Digits | Example |
|----------------|-------------|--------|---------|
| Boiler Temp Setpoint | `…1403` | 4 | `93.5 → "0935"` |
| Steam Temp Setpoint  | `…140A` | 4 | `140.0 → "1400"` |
| Pre-Infusion Power   | `…1404` | 3 | `35.0 → "350"` |
| Pre-Infusion Time    | `…1405` | 3 | `5.0 → "050"` |
| Infusion Power       | `…1406` | 4 | `99.0 → "0990"` |
| Infusion Time        | `…1407` | 3 | `5.0 → "050"` |
| Declining Pwr        | `…1408` | 4 | `80.0 → "0800"` |
| Declining Time       | `…1409` | 3 | `10.0 → "100"` |

---

## Input Validation (CTextBox)

```mermaid
flowchart TD
    A[User taps field] --> B{clickedOnce?}
    B -- No --> C[Clear field, set clickedOnce=true]
    B -- Yes --> D{Input all digits or decimal?}
    D -- No --> E[Reject character]
    D -- Yes --> F[Accept, set modified=true if changed]
    F --> G[User presses Done on keyboard]
    G --> H[onDone: clamp to minInValue – maxInValue]
    H --> I[clickedOnce = false]
```
