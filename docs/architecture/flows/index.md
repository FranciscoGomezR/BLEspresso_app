# Architecture Flows Index

All flows are documented in Mermaid format for visualization, versioning (git), and AI agent consumption.

| # | Flow | File | Description |
|---|------|------|-------------|
| 1 | BLE Notifications → UI | [ble-notifications/README.md](ble-notifications/README.md) | Live boiler temperature pushed from BLE device to `BoilerTemperatureProgressIndicator` |
| 2 | READ — RemoteControlScreen | [remote-control-read/README.md](remote-control-read/README.md) | User taps Read; 8 machine-config characteristics are read sequentially from GATT and displayed in text fields |
| 3 | WRITE — RemoteControlScreen | [remote-control-write/README.md](remote-control-write/README.md) | User double-taps Write; only modified fields are encoded and written to GATT |
| 4 | READ — ControllerParameterScreen | [controller-param-read/README.md](controller-param-read/README.md) | Auto-triggered on BLE connect; 7 PID characteristics read sequentially and loaded into text fields |
| 5 | WRITE — ControllerParameterScreen | [controller-param-write/README.md](controller-param-write/README.md) | User double-taps Write; only modified PID fields are encoded and written to GATT |
| 6 | Navigation | [navigation/README.md](navigation/README.md) | Screen-to-screen navigation via Jetpack Navigation Compose (`NavHost`) |
| 7 | StartScreen | [start-screen/README.md](start-screen/README.md) | Entry point; user taps circle button to connect and navigate to main screen |

## Related Documents

| Document | Path | Description |
|----------|------|-------------|
| GATT Table | [../../GATT_Table.md](../../GATT_Table.md) | Full BLE GATT profile — services, characteristics, UUIDs, encoding format |
| Project Requirements | [../../ProjectRequirementsDocument.md](../../ProjectRequirementsDocument.md) | Functional requirements, domain knowledge, architecture overview |
| Architecture Analysis | [../../ArchitectureAnalysis.md](../../ArchitectureAnalysis.md) | System architecture description (layered design) |
