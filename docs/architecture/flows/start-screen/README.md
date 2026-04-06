# Flow: StartScreen

> **Diagram file:** [diagram.mermaid](diagram.mermaid) — pure `flowchart LR` swimlane diagram (`.mermaid` format, no markdown wrapper).

**File:** `StartScreen.kt`  
**Purpose:** Entry point displayed when the app launches. The user taps the large circle button to initiate the BLE connection and navigate to the main control screen.

---

## User Interaction Flow

```mermaid
flowchart TD
    A([App launch]) --> B[Navigation composable\nstartDestination = start_screen]
    B --> C[StartScreen composable]
    C --> D{User sees circle button\n CONNECT TO ESPRESSO\n MACHINE }
    D --> E[User taps circle]
    E --> F["navController.navigate(BlespressoMainScreen)"]
    F --> G["popUpTo(StartScreen, inclusive=true)"]
    G --> H[RemoteControlScreen displayed]
```

---

## Screen Composition

```mermaid
sequenceDiagram
    participant Nav as NavController
    participant Screen as StartScreen Composable

    Nav->>Screen: Compose StartScreen(navController)

    Note over Screen: Full-screen Box with background_indic colour<br/>Centred inner Box: 200dp circle, accent_indic colour<br/>Text: "CONNECT\nTO ESPRESSO\nMACHINE" — bold, 22sp

    Screen->>Screen: .clickable { } registered on circle

    Note over Screen: No BLE operations on this screen.<br/>No ViewModel instantiated here.
```

---

## UI Elements

| Element | Properties |
|---------|------------|
| Outer `Box` | `fillMaxSize`, `background(background_indic)`, `contentAlignment = Center` |
| Circle `Box` | `size(200.dp)`, `clip(CircleShape)`, `background(accent_indic)`, clickable |
| `Text` | "CONNECT\nTO ESPRESSO\nMACHINE", `22.sp`, bold, `textAlign = Center`, `color = textprimary_indic` |

---

## Navigation Action on Tap

```kotlin
navController.navigate(Screen.BlespressoMainScreen.route) {
    popUpTo(Screen.StartScreen.route) { inclusive = true }
}
```

`inclusive = true` removes `StartScreen` from the back stack so the back button does not return here.
