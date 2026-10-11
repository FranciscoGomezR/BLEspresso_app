"""Build docs/UX/preview.html with inline SVGs (works from file://)."""
import os

UX = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # docs/UX

STATES = [
    ("00_heating_on", "0x00 / 0x01", "BOILER slot: HEATING_ON (lit) / HEATING_OFF (unlit)", "heat", True),
    ("02_ready", "0x02", "BOILER slot: READY", "ready", False),
    ("03_brewing", "0x03", "BREW slot: BREWING", "heat", True),
    ("04_brew_preinfuse", "0x04", "BREW slot: BREW_PREINFUSE", "heat", True),
    ("05_brew_infuse", "0x05", "BREW slot: BREW_INFUSE", "heat", True),
    ("06_brew_taper", "0x06", "BREW slot: BREW_TAPER", "heat", True),
    ("07_brew_autostop", "0x07", "BREW slot: BREW_AUTOSTOP", "ready", False),
    ("08_steaming", "0x08", "STEAMING", "heat", True),
    ("10_step_off", "0x10", "STEP_OFF", "off", False),
    ("11_step_on", "0x11", "STEP_ON", "heat", True),
]


def svg(opt, key):
    with open(os.path.join(UX, opt, f"state_{key}.svg")) as f:
        return f.read().strip()


rows = []
for key, code, name, role, pulse in STATES:
    cells = []
    for opt in ("option_a", "option_b"):
        s = svg(opt, key)
        cells.append(f'<td><span class="ic off">{s}</span></td>')
        cls = f"ic on {role}" + (" pulse" if pulse else "")
        cells.append(f'<td><span class="{cls}">{s}</span></td>')
    rows.append(f"<tr><td class=code>{code}</td><td>{name}</td><td>{role}</td>"
                + "".join(cells) + "</tr>")

HTML = """<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>BLEspresso Status Icons</title>
<style>
/* Values 1:1 with app/src/main/java/.../ui/theme/Color.kt palettes #7 / #8 */
:root{--background:#F4EFE6;--surface:#DDDFE2;--accent:#C8102E;--textprimary:#1A1A1A;
--textsecondary:#4A4D52;--icon_off:#1A1A1A61;--icon_heat:#E07A1F;--icon_ready:#2E9E5B;
--icon_brew:#7A4A2A;--icon_steam:#5B8DB8}
:root[data-theme=dark]{--background:#121212;--surface:#2A2C30;--accent:#E53950;
--textprimary:#F2F2F2;--textsecondary:#B8BCC2;--icon_off:#F2F2F261;--icon_heat:#FF9A3D;
--icon_ready:#4CC97E;--icon_brew:#C08A5E;--icon_steam:#8EC1EC}
body{margin:0;padding:16px;background:var(--background);color:var(--textprimary);
font:14px/1.4 system-ui,sans-serif}
h1{font-size:20px;margin:0 0 4px}p{color:var(--textsecondary);margin:0 0 12px}
.wrap{overflow-x:auto}
table{border-collapse:collapse;background:var(--surface);border-radius:8px}
th,td{padding:8px 12px;text-align:left;white-space:nowrap}
th{font-size:12px;color:var(--textsecondary);text-transform:uppercase}
td.code{font-family:ui-monospace,monospace}
.ic{display:inline-flex;width:40px;height:40px;align-items:center;justify-content:center}
.ic svg{width:36px;height:36px}
.off{color:var(--icon_off)}
.on.heat{color:var(--icon_heat)}.on.ready{color:var(--icon_ready)}
.on.brew{color:var(--icon_brew)}.on.steam{color:var(--icon_steam)}
.on.off{color:var(--icon_off)}
.on:not(.off) svg{filter:drop-shadow(0 0 4px currentColor) drop-shadow(0 0 8px currentColor)}
.pulse svg{animation:p 2s ease-in-out infinite}
@keyframes p{50%{filter:drop-shadow(0 0 1px currentColor)}}
@media (prefers-reduced-motion:reduce){.pulse svg{animation:none}}
button{background:var(--accent);color:#fff;border:0;border-radius:6px;padding:6px 12px;
margin-bottom:12px;cursor:pointer}
.mode{font-weight:600;letter-spacing:.08em;color:var(--accent)}
</style></head><body>
<h1>BLEspresso status icons (TODO #6)</h1>
<p>Mode as text: <span class="mode">CLASSIC</span> · <span class="mode">PROFILE</span>
 · <span class="mode">STEP FUNCTION</span>. State as icon, OFF vs ON (illuminated).</p>
<button onclick="var r=document.documentElement;r.dataset.theme=r.dataset.theme==='dark'?'light':'dark'">
Toggle light / dark</button>
<div class="wrap"><table>
<tr><th>Code</th><th>State</th><th>Role</th><th>A off</th><th>A on</th><th>B off</th><th>B on</th></tr>
ROWS
</table></div>
</body></html>
"""

with open(os.path.join(UX, "preview.html"), "w", newline="\n", encoding="utf-8") as f:
    f.write(HTML.replace("ROWS", "\n".join(rows)))
print("ok")
