# Icon selection — machine status (0x1401)

Fill the Icon column with the chosen file, e.g. `option_b/state_00_heating_on.svg`.

Boiler slot: 0x00 / 0x01 / 0x02 share one icon position; icon swaps on notify.
Brew slot: 0x03..0x07 share one icon position; icon swaps on notify.
Step slot: 0x10 / 0x11 share one icon position; icon swaps on notify.

| Code | State | Icon |
|---|---|---|
| 0x00 | HEATING_ON (boiler slot) | `option_a/state_00_heating_on.svg` |
| 0x01 | HEATING_OFF (boiler slot) | `option_a/state_00_heating_on.svg` (unlit, `icon_off`) |
| 0x02 | READY (boiler slot) | `option_a/state_02_ready.svg` |
| 0x03 | BREWING (classic) (brew slot) | `option_a/state_03_brewing.svg` |
| 0x04 | BREW_PREINFUSE (brew slot) | `option_b/state_04_brew_preinfuse.svg` |
| 0x05 | BREW_INFUSE (brew slot) | `option_b/state_05_brew_infuse.svg` |
| 0x06 | BREW_TAPER (brew slot) | `option_b/state_06_brew_taper.svg` |
| 0x07 | BREW_AUTOSTOP (brew slot) | `option_a/state_07_brew_autostop.svg` |
| 0x08 | STEAMING | `option_a/state_08_steaming.svg` |
| 0x10 | STEP_OFF (step slot) | `option_a/state_10_step_off.svg` |
| 0x11 | STEP_ON (step slot) | `option_a/state_11_step_on.svg` |
