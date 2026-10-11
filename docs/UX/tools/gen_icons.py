"""Generate docs/UX icon sets (option A line glyphs, option B panel lamps)."""
import os

OUT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # docs/UX

CUP = ('<path d="M5 11h11v3.5a4.5 4.5 0 0 1-4.5 4.5h-2A4.5 4.5 0 0 1 5 14.5z"/>'
       '<path d="M16 12h1.5a2 2 0 0 1 0 4H16"/><path d="M4 21h14"/>')
FLAME = ('<path d="M12 3c.5 3 4.5 5 4.5 10a4.5 4.5 0 0 1-9 0c0-2.5 1.5-3.5 2.5-5'
         ' .5 2 1.5 3 2.5 3 .5-3-1-5-.5-8z"/>')


def drop(cx, cy, s=1.0):
    return (f'<path d="M{cx} {cy - 2.6 * s}c{1.3 * s:.2f} {1.7 * s:.2f} {1.9 * s:.2f} '
            f'{2.7 * s:.2f} {1.9 * s:.2f} {3.5 * s:.2f}a{1.9 * s:.2f} {1.9 * s:.2f} 0 0 1 '
            f'{-3.8 * s:.2f} 0c0 {-0.8 * s:.2f} {0.6 * s:.2f} {-1.8 * s:.2f} {1.9 * s:.2f} '
            f'{-3.5 * s:.2f}z" fill="currentColor"/>')


BOILER = ('<rect x="5" y="8" width="14" height="12" rx="3.5"/>'
          '<path d="M5.5 14c1.6-1.2 3-1.2 4.5 0s2.9 1.2 4.5 0 2.9-1.2 4 0"/>')
WAVES = ('<path d="M9 6c-.8-.8.8-1.4 0-2.2"/><path d="M12 6c-.8-.8.8-1.4 0-2.2"/>'
         '<path d="M15 6c-.8-.8.8-1.4 0-2.2"/>')
GROUP = '<path d="M6 3h11"/>'
STEP = '<path d="M3 3v18h18"/><path d="M6 17h4V8h10"/>'

GLYPHS = {
    "00_heating_on": BOILER + WAVES,
    "02_ready": '<rect x="5" y="8" width="14" height="12" rx="3.5"/>'
    '<path d="M8.6 14.2l2.4 2.4 4.4-4.8"/>',
    "03_brewing": GROUP + CUP + '<path d="M10.5 5v4"/>',
    "04_brew_preinfuse": GROUP + CUP + drop(10.5, 7.2, 0.8),
    "05_brew_infuse": GROUP + CUP + drop(7.5, 7.2, 0.8) + drop(10.5, 7.2, 0.8)
    + drop(13.5, 7.2, 0.8),
    "06_brew_taper": GROUP + CUP + drop(9, 7.2, 0.8) + drop(12, 7.6, 0.5),
    "07_brew_autostop": GROUP + CUP + '<path d="M9 9.5c-.8-.8.8-1.5 0-2.3s.8-1.5 0-2.3"/>'
    '<path d="M12 9.5c-.8-.8.8-1.5 0-2.3s.8-1.5 0-2.3"/>',
    "08_steaming": '<path d="M10.8 3L9 13"/><path d="M12.6 3v10.5"/><path d="M14.4 3l1.6 10"/>'
    '<path d="M8.2 13.6a2.5 2.5 0 0 0-2.1 4.2 2.6 2.6 0 0 0 3.6 3 2.8 2.8 0 0 0 5.2 0'
    ' 2.6 2.6 0 0 0 3.6-3 2.5 2.5 0 0 0-2.1-4.2"/>',
    "10_step_off": STEP + '<path d="M4 4l16 16"/>',
    "11_step_on": STEP + '<circle cx="10" cy="8" r="1.5" fill="currentColor"/>',
}

# Option B: brew phase -> number of lit ring segments (of 3)
PHASE = {"04_brew_preinfuse": 1, "05_brew_infuse": 2, "06_brew_taper": 3}

HEAD = ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" '
        'fill="none" stroke="currentColor" stroke-width="{sw}" stroke-linecap="round" '
        'stroke-linejoin="round">')


def option_a(g):
    return HEAD.format(sw="1.6") + g + "</svg>\n"


def option_b(name, g):
    body = '<circle cx="12" cy="12" r="10" fill="currentColor" fill-opacity=".14" stroke="none"/>'
    n = PHASE.get(name)
    if n is None:
        body += '<circle cx="12" cy="12" r="11" stroke-width="1.2"/>'
    else:
        circ = 2 * 3.14159 * 11
        seg = circ / 3
        for i in range(3):
            op = "1" if i < n else ".25"
            body += (f'<circle cx="12" cy="12" r="11" stroke-width="1.6" stroke-opacity="{op}" '
                     f'stroke-dasharray="{seg - 2:.2f} {circ - seg + 2:.2f}" '
                     f'stroke-dashoffset="{-i * seg:.2f}" transform="rotate(-90 12 12)"/>')
    body += f'<g transform="translate(5.5 5.5) scale(.54)" stroke-width="2.4">{g}</g>'
    return HEAD.format(sw="1.6") + body + "</svg>\n"


for opt, fn in (("option_a", lambda k, g: option_a(g)), ("option_b", option_b)):
    os.makedirs(os.path.join(OUT, opt), exist_ok=True)
    for key, g in GLYPHS.items():
        with open(os.path.join(OUT, opt, f"state_{key}.svg"), "w", newline="\n") as f:
            f.write(fn(key, g))
print("ok")
