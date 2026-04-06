# BLEspresso GATT Profile

Full GATT table derived from `RemoteControlBLEReceiveManager.kt` and `ProjectRequirementsDocument.md`.

All UUIDs share the suffix `-00b0-4240-ba50-05ca45bf8abc`.

---

## Machine Control Service

**Service UUID:** `00001400-00b0-4240-ba50-05ca45bf8abc`

| # | Characteristic Name         | Full UUID                                    | Direction       | Format          | Description                                      |
|---|-----------------------------|----------------------------------------------|-----------------|-----------------|--------------------------------------------------|
| 1 | BLEspresso Status           | `00001401-00b0-4240-ba50-05ca45bf8abc`       | Read / Notify   | —               | Device operational status                        |
| 2 | Boiler Water Temperature    | `00001402-00b0-4240-ba50-05ca45bf8abc`       | Read / Notify   | ASCII 4 digits  | Live boiler water temperature in °C (e.g. `0935` = 93.5 °C) |
| 3 | Boiler Temp Setpoint        | `00001403-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 4 digits  | Target boiler temperature (0 – 150 °C)          |
| 4 | Pre-Infusion Power          | `00001404-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 3 digits  | Pre-infusion pump power % (0 – 50 %; e.g. `350` = 35.0 %) |
| 5 | Pre-Infusion Time           | `00001405-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 3 digits  | Pre-infusion phase duration in seconds (0 – 15 s; e.g. `050` = 5.0 s) |
| 6 | Infusion Power              | `00001406-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 4 digits  | Full infusion pump power % (0 – 100 %; e.g. `0990` = 99.0 %) |
| 7 | Infusion Time               | `00001407-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 3 digits  | Full infusion phase duration in seconds (1 – 15 s) |
| 8 | Declining Pressure Power    | `00001408-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 4 digits  | Declining-pressure pump power % (70 – 100 %; e.g. `0800` = 80.0 %) |
| 9 | Declining Pressure Time     | `00001409-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 3 digits  | Declining-pressure phase duration in seconds (0 – 15 s; e.g. `100` = 10.0 s) |
| 10| Steam Temp Setpoint         | `0000140a-00b0-4240-ba50-05ca45bf8abc`       | Read / Write    | ASCII 4 digits  | Target steam temperature (100 – 145 °C; e.g. `1400` = 140.0 °C) |

---

## PID Controller Service

**Service UUID:** `00001500-00b0-4240-ba50-05ca45bf8abc`

| # | Characteristic Name         | Full UUID                                    | Direction    | Format          | Description                                              |
|---|-----------------------------|----------------------------------------------|--------------|-----------------|----------------------------------------------------------|
| 1 | PID Proportional (P)        | `00001501-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 4 digits  | Proportional gain (0 – 100; e.g. `0125` = 12.5)         |
| 2 | PID Integral (I)            | `00001502-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 3 digits  | Integral gain (0 – 20; e.g. `015` = 1.5)                |
| 3 | PID Integral Max (I_Max)    | `00001503-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 4 digits  | Integral accumulator clamp (0 – 500; e.g. `0500` = 50.0)|
| 4 | PID Integral Windup         | `00001504-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 1 digit   | Anti-windup flag (0 – 1; `1` = enabled, `0` = disabled) |
| 5 | PID Derivative (D)          | `00001505-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 3 digits  | Derivative gain (0 – 10; e.g. `025` = 2.5)              |
| 6 | PID Derivative LPF (D_LPF) | `00001506-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 4 digits  | Derivative low-pass filter cutoff in Hz (5 – 250; e.g. `0100` = 10.0 Hz) |
| 7 | PID Gain                    | `00001507-00b0-4240-ba50-05ca45bf8abc`       | Read / Write | ASCII 4 digits  | Overall controller gain multiplier (0 – 100; e.g. `0010` = 1.0) |

---

## Encoding / Decoding Convention

All values are transmitted as **ASCII digit strings** — no binary floating-point.

### Read (Device → App)
Each received byte is decoded as an ASCII digit:

```
value = (byte[0] - 48) * W₀ + (byte[1] - 48) * W₁ + … + (byte[n-1] - 48) * Wₙ
```

where the digit weights `Wₙ` depend on the number of expected digits:

| Format   | Weights (MSB → LSB) |
|----------|---------------------|
| 4-digit  | 100.0, 10.0, 1.0, 0.1 |
| 3-digit  | 10.0, 1.0, 0.1        |
| 1-digit  | 1.0                   |

### Write (App → Device)
`dataConvertionFloatToCharacteristic()` converts a Float to a fixed-width ASCII string before writing.

| Value     | Digits | Encoded bytes |
|-----------|--------|---------------|
| 93.5 °C   | 4      | `0935`        |
| 35.0 %    | 3      | `350`         |
| 99.0 %    | 4      | `0990`        |
| 1 (on)    | 1      | `1`           |

### Notification (Live Temperature)
Boiler Water Temperature (UUID `…1402`) pushes ASCII 4-digit notifications.  
The `onCharacteristicChanged` callback decodes each byte the same way as reads.
