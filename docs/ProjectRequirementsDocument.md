# Project Requirements Document
## BLEspresso Android Application

| Field        | Detail                          |
|--------------|---------------------------------|
| **Project**  | BLEspresso App                  |
| **Platform** | Android                         |
| **Language** | Kotlin                          |
| **Version**  | 1.0                             |
| **Date**     | April 4, 2026                   |

---

## 1. Project Overview

BLEspresso is an Android application that provides wireless remote control of an espresso machine controller over Bluetooth Low Energy (BLE). The app allows the user to monitor live boiler temperature, configure brew parameters (pre-infusion, infusion, declining-pressure profiles), and tune the machine's PID controller — all without a physical connection to the machine.

---

## 2. Goals and Objectives

- Provide a real-time dashboard showing the boiler water temperature and target setpoints.
- Allow the user to read and write machine configuration parameters over BLE.
- Allow the user to read and write PID controller tuning parameters over BLE.
- Handle BLE connection lifecycle (scan, connect, reconnect, disconnect) transparently.
- Enforce valid parameter ranges before transmitting any value to the device.

---

## 3. Scope

### In Scope
- BLE device discovery and connection management.
- Real-time boiler temperature monitoring (live notify characteristic).
- Boiler water temperature setpoint configuration.
- Steam temperature setpoint configuration.
- Brew profile configuration (pre-infusion, infusion, declining-pressure phases).
- PID controller parameter configuration.
- Multi-screen navigation (Start → Main Control → PID Controller Parameters).
- Android runtime permission handling.

### Out of Scope
- Firmware updates for the BLEspresso controller.
- Cloud connectivity or remote access beyond local BLE.
- User accounts or data persistence across device reinstalls.
- Support for multiple simultaneous espresso machines.

---

## 4. Stakeholders

| Role              | Responsibility                                   |
|-------------------|--------------------------------------------------|
| Developer         | Design, implement, and maintain the Android app. |
| End User          | Operate the espresso machine via the app.        |
| Firmware Engineer | Define BLE GATT service/characteristic contracts.|

---

## 5. Functional Requirements

### 5.1 Application Startup

| ID    | Requirement |
|-------|-------------|
| FR-01 | The app shall display a Start Screen when launched. |
| FR-02 | The app shall prompt the user to enable Bluetooth if it is disabled, both at startup and whenever the app returns to the foreground. |
| FR-03 | The Start Screen shall provide navigation to the Main Control Screen. |

### 5.2 Bluetooth Permissions

| ID    | Requirement |
|-------|-------------|
| FR-04 | The app shall request the following Android runtime permissions before starting BLE operations: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`. |
| FR-05 | BLE operations shall not begin until all required permissions are granted. |

### 5.3 BLE Connection Management

| ID    | Requirement |
|-------|-------------|
| FR-06 | The app shall scan for a BLE peripheral advertising the device name **"BLEspresso"**. |
| FR-07 | The scan shall use `SCAN_MODE_LOW_LATENCY` for fast device discovery. |
| FR-08 | Upon finding the target device, the app shall stop scanning and initiate a GATT connection. |
| FR-09 | The app shall attempt GATT connection up to **3 times** before reporting a connection error. |
| FR-10 | After a successful connection, the app shall discover GATT services and characteristics automatically. |
| FR-11 | If the app is brought back to the foreground while in a `Disconnected` state (and all permissions are granted), it shall automatically attempt to reconnect. |
| FR-12 | The app shall display a status message (e.g., "Scanning for BLEspresso Machine", "Discovering Services…", "Device Disconnected") to indicate the current connection state. |

### 5.4 Main Control Screen

| ID    | Requirement |
|-------|-------------|
| FR-13 | The screen shall display the live boiler water temperature, updated via BLE notifications. |
| FR-14 | The screen shall display a visual temperature progress indicator. |
| FR-15 | The user shall be able to read the current boiler temperature setpoint from the device. |
| FR-16 | The user shall be able to enter and write a new boiler temperature setpoint to the device. |
| FR-17 | The boiler temperature setpoint shall be constrained to **0 – 150 °C**. |
| FR-18 | The user shall be able to read the current steam temperature setpoint from the device. |
| FR-19 | The user shall be able to enter and write a new steam temperature setpoint to the device. |
| FR-20 | The steam temperature setpoint shall be constrained to **100 – 145 °C**. |
| FR-21 | The screen shall display the brew profile configuration section with the following parameters: |
|       | - Pre-Infusion Power (0 – 50 %) |
|       | - Pre-Infusion Time (0 – 15 s) |
|       | - Infusion Power (0 – 100 %) |
|       | - Infusion Time (1 – 15 s) |
|       | - Declining Pressure Power (70 – 100 %) |
|       | - Declining Pressure Time (0 – 15 s) |
| FR-22 | The user shall be able to read all brew configuration parameters from the device with a single action. |
| FR-23 | The user shall be able to write all brew configuration parameters to the device with a single action. |
| FR-24 | Text fields that have been modified since the last BLE read shall be visually distinguished from unmodified fields. |
| FR-25 | Input values that exceed the defined min/max range shall be automatically clamped to the boundary value on keyboard confirmation. |
| FR-26 | The screen shall provide navigation to the PID Controller Parameter Screen. |

### 5.5 PID Controller Parameter Screen

| ID    | Requirement |
|-------|-------------|
| FR-27 | The screen shall automatically initiate a BLE connection (or reuse an existing one) when first displayed. |
| FR-28 | Upon connecting, the screen shall automatically read all PID parameters from the device. |
| FR-29 | The screen shall display and allow editing of the following PID parameters: |
|       | - Proportional term P (0 – 100) |
|       | - Integral term I (0 – 20) |
|       | - Integral limit I_Max (0 – 500) |
|       | - Derivative term D (0 – 10) |
|       | - Phase-1 P-gain boost P_Boost (0 – 10) |
|       | - Phase-1 I-gain boost I_Boost (0 – 20) |
| FR-30 | The user shall be able to write all PID parameters to the device with a single action. |
| FR-31 | The screen shall provide navigation back to the Main Control Screen. |

### 5.6 Input Validation

| ID    | Requirement |
|-------|-------------|
| FR-32 | All numeric text fields shall accept only digit and decimal-point characters. |
| FR-33 | On keyboard confirmation (`Done` action), values shall be clamped to the parameter's defined [min, max] range. |
| FR-34 | On first tap, a text field shall clear its previous value to allow fresh entry. |

---

## 6. BLE GATT Profile

### 6.1 Machine Control Service

**Service UUID:** `00001400-00b0-4240-ba50-05ca45bf8abc`

| Characteristic | UUID | Direction | Description |
|----------------|------|-----------|-------------|
| BLEspresso Status | `00001401-…` | Read / Notify | Device operational status |
| Boiler Water Temperature | `00001402-…` | Read / Notify | Live boiler water temp (°C, float) |
| Boiler Temp Setpoint | `00001403-…` | Read / Write | Target boiler temperature |
| Steam Temp Setpoint | `0000140a-…` | Read / Write | Target steam temperature |
| Pre-Infusion Power | `00001404-…` | Read / Write | Power during pre-infusion phase (%) |
| Pre-Infusion Time | `00001405-…` | Read / Write | Duration of pre-infusion phase (s) |
| Infusion Power | `00001406-…` | Read / Write | Power during infusion phase (%) |
| Infusion Time | `00001407-…` | Read / Write | Duration of infusion phase (s) |
| Declining Pressure Power | `00001408-…` | Read / Write | Power during declining-pressure phase (%) |
| Declining Pressure Time | `00001409-…` | Read / Write | Duration of declining-pressure phase (s) |

### 6.2 PID Controller Service

**Service UUID:** `00001500-00b0-4240-ba50-05ca45bf8abc`

| Characteristic | UUID | Direction | Description |
|----------------|------|-----------|-------------|
| PID P Term | `00001501-…` | Read / Write | Proportional gain (`pid_Pterm`) |
| PID I Term | `00001502-…` | Read / Write | Integral gain (`pid_Iterm`) |
| PID I Max Term | `00001503-…` | Read / Write | Integral accumulator limit (`pid_Imaxterm`) |
| PID D Term | `00001504-…` | Read / Write | Derivative gain (`pid_Dterm`) |
| PID P Boost | `00001505-…` | Read / Write | Phase-1 proportional-gain boost multiplier (`pid_Pboost`) |
| PID I Boost | `00001506-…` | Read / Write | Phase-1 integral-gain boost multiplier (`pid_Iboost`) |

> All UUIDs share the suffix `-00b0-4240-ba50-05ca45bf8abc`.

---

## 7. Non-Functional Requirements

### 7.1 Performance

| ID     | Requirement |
|--------|-------------|
| NFR-01 | The BLE scan shall use low-latency mode to minimise discovery time. |
| NFR-02 | Boiler temperature notifications shall be reflected in the UI without perceptible delay (< 500 ms from notification receipt). |
| NFR-03 | BLE read/write operations shall be queued to prevent GATT congestion. |

### 7.2 Reliability

| ID     | Requirement |
|--------|-------------|
| NFR-04 | The app shall retry a failed GATT connection up to 3 times automatically. |
| NFR-05 | The app shall handle unexpected BLE disconnects gracefully and notify the user. |

### 7.3 Usability

| ID     | Requirement |
|--------|-------------|
| NFR-06 | The UI shall clearly indicate the current connection state at all times. |
| NFR-07 | Modified parameter fields shall be visually differentiated from unmodified ones. |
| NFR-08 | The numeric keyboard shall be dismissed when the user confirms input (Done action). |
| NFR-09 | The app shall prevent accidental double-tap actions on write buttons. |

### 7.4 Security

| ID     | Requirement |
|--------|-------------|
| NFR-10 | The app shall not request or store any personal user data. |
| NFR-11 | BLE communication is local only; no data is transmitted over the internet. |

---

## 8. Android Platform Requirements

| Attribute | Value |
|-----------|-------|
| Minimum SDK | API 21 (Android 5.0 Lollipop) |
| Target SDK | API 32 (Android 12L) |
| Compile SDK | API 32 |
| Language | Kotlin |
| UI Toolkit | Jetpack Compose (Material) |

---

## 9. Required Android Permissions

| Permission | Reason |
|------------|--------|
| `BLUETOOTH` | Legacy BLE API access |
| `BLUETOOTH_ADMIN` | Legacy BLE scan/connect management |
| `BLUETOOTH_SCAN` | BLE device scanning (API 31+) |
| `BLUETOOTH_CONNECT` | GATT connection (API 31+) |
| `ACCESS_COARSE_LOCATION` | Required by BLE scanning (pre-API 31) |
| `ACCESS_FINE_LOCATION` | Required by BLE scanning (pre-API 31) |

---

## 10. Architecture

The application follows the **MVVM (Model-View-ViewModel)** pattern with a clean separation of layers:

```
┌──────────────────────────────────────────────────────┐
│  Presentation Layer                                  │
│  Composable Screens: StartScreen, RemoteControlScreen│
│                      BlespressoCtrlParamScreen        │
│  ViewModels:         RemoteControlViewModel           │
├──────────────────────────────────────────────────────┤
│  Domain / Interface Layer                            │
│  RemoteControlReceiveManager (interface)             │
├──────────────────────────────────────────────────────┤
│  Data Layer                                          │
│  RemoteControlBLEReceiveManager (BLE implementation) │
│  Data models: BoilerDataResult, BoilerSetpoint,      │
│               CharDataClass, C_BrewConfigData,       │
│               C_PIDConfigData                        │
├──────────────────────────────────────────────────────┤
│  Dependency Injection                                │
│  Hilt / Dagger — AppModule                           │
└──────────────────────────────────────────────────────┘
```

### Data Flow — Device → UI

1. BLE notification / read callback fires in `RemoteControlBLEReceiveManager`.
2. Data is emitted to a `MutableSharedFlow` (e.g., `data`, `dataToRead`, `dataSteamSetpoint`, `dataBrewCfg*`, `dataPIDcfg*`).
3. `RemoteControlViewModel` collects the flow using `collectLatest` and updates `mutableStateOf` variables.
4. Composables observe the ViewModel state and recompose automatically.

### Data Flow — UI → Device

1. User edits a text field (`CTextBox`) on a Composable screen.
2. On write-button tap, the screen passes values to the ViewModel (e.g., `viewModel.txNewBrewConfigData`).
3. The ViewModel calls the appropriate manager function (e.g., `remoteControlReceiveManager.txfcnWriteBrewConfiguration()`).
4. `RemoteControlBLEReceiveManager` queues and executes `writeCharacteristic()` calls over GATT.

---

## 11. Navigation Structure

```
StartScreen
    └── RemoteControlScreen  (Main Control)
            └── BlespressoCtrlParamScreen  (PID Parameters)
                    └── (back) RemoteControlScreen
```

Navigation is implemented with **Jetpack Navigation Compose** (`NavHost`).

---

## 12. Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose UI | per `compose_version` | Declarative UI |
| Jetpack Compose Material | per `compose_version` | Material Design components |
| AndroidX Navigation Compose | 2.5.2 | Screen navigation |
| Hilt Android | 2.38.1 | Dependency injection |
| AndroidX Hilt Navigation Compose | 1.0.0 | ViewModel injection in Compose |
| Accompanist Permissions | 0.21.1-beta | Runtime permission helpers |
| AndroidX Lifecycle Runtime KTX | 2.3.1 | Lifecycle-aware coroutines |
| Kotlin Coroutines / Flow | (transitive) | Async data streaming |

---

## 13. Parameter Reference Table

| Parameter | Min | Max | Unit | Default |
|-----------|-----|-----|------|---------|
| Boiler Temp Setpoint | 0 | 150 | °C | — |
| Steam Temp Setpoint | 100 | 145 | °C | — |
| Pre-Infusion Power | 0 | 50 | % | 35.0 |
| Pre-Infusion Time | 0 | 15 | s | 5.0 |
| Infusion Power | 0 | 100 | % | 99.0 |
| Infusion Time | 1 | 15 | s | 5.0 |
| Declining Pressure Power | 70 | 100 | % | 80.0 |
| Declining Pressure Time | 0 | 15 | s | 10.0 |
| PID — P Term | 0 | 100 | — | — |
| PID — I Term | 0 | 20 | — | 0.3 |
| PID — I Max (limit) | 0 | 500 | — | — |
| PID — D Term | 0 | 10 | — | — |
| PID — P Boost | 0 | 10 | — | 1.0 |
| PID — I Boost | 0 | 20 | — | 6.5 |

---

## 14. Connection State Machine

| State | Description |
|-------|-------------|
| `Uninitialized` | App started; no BLE operation has begun. |
| `CurrentlyInitializing` | Scanning, connecting, or discovering services. |
| `Connected` | GATT connected and services discovered. |
| `Disconnected` | Previously connected but link was lost or closed. |

---

*End of Project Requirements Document*
