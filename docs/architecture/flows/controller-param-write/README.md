# Flow: WRITE Button — ControllerParameterScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**Trigger:** User **double-taps** the **Write** button in `BlespressoCtrlParamScreen`.  
**Ends:** Only fields that were modified since the last read are written to the BLE device via GATT.

> **Note:** Single tap shows "click" toast as confirmation feedback. Only a **double-tap** triggers the write to the device.

---

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Screen as BlespressoCtrlParamScreen<br/>(Composable)
    participant VM as RemoteControlViewModel
    participant BLEMgr as RemoteControlBLEReceiveManager
    participant GATT as BluetoothGatt (BLE Device)

    User->>Screen: Edit one or more CTextBox fields
    Note over Screen: cXxx_TB.onValueChange() sets modified=true<br/>cXxx_TB.onDone() clamps to [minInValue, maxInValue]

    User->>Screen: Double-tap "Write" button
    Screen->>Screen: detectTapGestures → onDoubleTap

    Screen->>Screen: For each of 6 PID fields:<br/>pass value if (modified && text.isNotBlank()) else null

    Screen->>VM: viewModel.writePIDConfig(<br/>  pTerm, iTerm, iMax,<br/>  dTerm, pBoost, iBoost<br/>)

    activate VM
    VM->>VM: For each non-null arg → call matching txWritePid*()
    VM->>BLEMgr: txWritePidPTerm(pTerm)   [if modified]
    VM->>BLEMgr: txWritePidITerm(iTerm)   [if modified]
    VM->>BLEMgr: txWritePidIMax(iMax)     [if modified]
    VM->>BLEMgr: txWritePidDTerm(dTerm)   [if modified]
    VM->>BLEMgr: txWritePidPBoost(pBoost) [if modified]
    VM->>BLEMgr: txWritePidIBoost(iBoost) [if modified]
    VM-->>Screen: returns anyModified: Boolean
    deactivate VM

    activate BLEMgr
    Note over BLEMgr: writeCharacteristic(PID_SERVICE_UUID, charUuid, value, digits)<br/>Encodes Float → ASCII (e.g. 12.5 → "0125")<br/>Calls writeCharacteristicsDevice(char, payload)<br/>Uses WRITE_TYPE_NO_RESPONSE
    BLEMgr->>GATT: gatt.writeCharacteristic(characteristic)
    GATT-->>BLEMgr: onCharacteristicWrite(uuid, GATT_SUCCESS)
    BLEMgr->>BLEMgr: Log write success per UUID
    deactivate BLEMgr

    alt anyModified == true
        Screen->>Screen: Reset modified=false for all 6 CTextBoxes
        Screen->>User: Toast "Sending modified PID configuration"
    else anyModified == false
        Screen->>User: Toast "No fields have been modified"
    end
```

---

## Value Encoding Details

| Characteristic | UUID suffix | Digits | Example |
|----------------|-------------|--------|---------|
| PID P Term     | `…1501` | 4 | `12.5 → "0125"` |
| PID I Term     | `…1502` | 3 | `1.5 → "015"` |
| PID I Max      | `…1503` | 4 | `50.0 → "0500"` |
| PID D Term     | `…1504` | 3 | `2.5 → "025"` |
| PID P Boost    | `…1505` | 3 | `1.0 → "010"` |
| PID I Boost    | `…1506` | 3 | `6.5 → "065"` |

---

## Parameter Ranges

| Field | Min | Max |
|-------|-----|-----|
| P (Proportional) | 0 | 100 |
| I (Integral) | 0 | 20 |
| I_Max (Integral limit) | 0 | 500 |
| D (Derivative) | 0 | 10 |
| P_Boost (Phase-1 P-gain boost) | 0 | 10 |
| I_Boost (Phase-1 I-gain boost) | 0 | 20 |
