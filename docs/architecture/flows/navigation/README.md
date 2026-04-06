# Flow: Navigation

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**File:** `Navigation.kt`  
**Pattern:** Jetpack Navigation Compose — `NavHost` with three destinations.

---

## Screen Route Map

```mermaid
flowchart LR
    Start([App Launch]) --> SS[StartScreen\nroute: start_screen]
    SS -->|"User taps circle button\nnavigatePopTo start_screen inclusive"| RCS[RemoteControlScreen\nroute: BLE_espresso_main_screen]
    RCS -->|"Tap Controller button\nnavigatePopTo main_screen inclusive"| CPS[BlespressoCtrlParamScreen\nroute: BLE_espresso_CtrlParameters_screen]
    CPS -->|"Tap Main button\nnavigatePopTo main_screen inclusive"| RCS
```

---

## Navigation Composition

```mermaid
sequenceDiagram
    participant App as MainActivity / BLEspresso_Application
    participant Nav as Navigation composable
    participant NH as NavHost

    App->>Nav: Navigation(onBluetoothStateChanged)
    Nav->>NH: NavHost(startDestination = start_screen)
    NH-->>Nav: Register composable(start_screen) → StartScreen
    NH-->>Nav: Register composable(BLE_espresso_main_screen) → RemoteControlScreen
    NH-->>Nav: Register composable(BLE_espresso_CtrlParameters_screen) → BlespressoCtrlParamScreen
    Note over NH: NavController manages back stack
```

---

## Route Definitions

```mermaid
classDiagram
    class Screen {
        <<sealed class>>
        +route: String
    }
    class StartScreen {
        route = "start_screen"
    }
    class BlespressoMainScreen {
        route = "BLE_espresso_main_screen"
    }
    class BlespressoCtrlParamScreen {
        route = "BLE_espresso_CtrlParameters_screen"
    }
    Screen <|-- StartScreen
    Screen <|-- BlespressoMainScreen
    Screen <|-- BlespressoCtrlParamScreen
```

---

## Back Stack Behaviour

All navigations use `popUpTo` with `inclusive = true`, so the source screen is removed from the back
stack on each transition. The Android system back button effectively exits the app from `RemoteControlScreen`
and returns to `RemoteControlScreen` from `BlespressoCtrlParamScreen`.

| From | To | popUpTo | inclusive |
|------|----|---------|-----------|
| StartScreen | RemoteControlScreen | StartScreen | true |
| RemoteControlScreen | ControllerParamScreen | RemoteControlScreen | true |
| ControllerParamScreen | RemoteControlScreen | RemoteControlScreen | true |

---

## BLE Lifecycle vs. Navigation

| Screen | BLE action on entry |
|--------|---------------------|
| `StartScreen` | None — no BLE operations |
| `RemoteControlScreen` | `initiazeConnection()` on `Uninitialized`; `reconnect()` on `Disconnected` (via `DisposableEffect ON_START`) |
| `BlespressoCtrlParamScreen` | `initiazeConnection()` via `LaunchedEffect(Unit)`; auto-reads PID config on `Connected` |
