"""Convert selected docs/UX/option_b SVGs (lamp + segmented ring) to VectorDrawables."""
import math
import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(__file__))
from svg2vd import rect_d, circle_d  # noqa: E402  (reuse shape helpers)

from svg2vd import UX, DST, SKIP  # noqa: E402

SRC = os.path.join(UX, "option_b")
NS = "{http://www.w3.org/2000/svg}"
KEYS = SKIP  # the Option B selections skipped by svg2vd.py


def arc_d(e):
    """Dashed ring segment (rotate -90, dasharray len, dashoffset) -> SVG arc path."""
    cx, cy, r = (float(e.get(k)) for k in ("cx", "cy", "r"))
    dash = float(e.get("stroke-dasharray").split()[0])
    off = -float(e.get("stroke-dashoffset", 0))
    a0 = -math.pi / 2 + off / r
    a1 = a0 + dash / r
    x0, y0 = cx + r * math.cos(a0), cy + r * math.sin(a0)
    x1, y1 = cx + r * math.cos(a1), cy + r * math.sin(a1)
    return f"M{x0:.3f},{y0:.3f}A{r},{r} 0 0 1 {x1:.3f},{y1:.3f}"


def path_xml(d, ind, w=None, filled=False, fill_a=None, stroke=True, stroke_a=None):
    out = [f"{ind}<path", f'{ind}    android:pathData="{d}"',
           f'{ind}    android:fillColor="{"#FF000000" if filled else "#00000000"}"']
    if fill_a:
        out.append(f'{ind}    android:fillAlpha="{fill_a}"')
    if stroke:
        out += [f'{ind}    android:strokeColor="#FF000000"', f'{ind}    android:strokeWidth="{w}"']
        if stroke_a:
            out.append(f'{ind}    android:strokeAlpha="{stroke_a}"')
        out += [f'{ind}    android:strokeLineCap="round"', f'{ind}    android:strokeLineJoin="round"']
    out[-1] += " />"
    return out


def walk(node, sw, ind):
    out = []
    for e in node:
        tag = e.tag.replace(NS, "")
        w = e.get("stroke-width", sw)
        if tag == "g":
            m = re.match(r"translate\(([\d.]+) ([\d.]+)\) scale\(([\d.]+)\)", e.get("transform"))
            tx, ty, s = m.groups()
            out += [f"{ind}<group", f'{ind}    android:translateX="{tx}"',
                    f'{ind}    android:translateY="{ty}"', f'{ind}    android:scaleX="{s}"',
                    f'{ind}    android:scaleY="{s}">']
            out += walk(e, w, ind + "    ")
            out.append(f"{ind}</group>")
            continue
        filled = e.get("fill") == "currentColor"
        stroke = e.get("stroke") != "none"
        if tag == "circle" and e.get("stroke-dasharray"):
            d = arc_d(e)
        else:
            d = {"path": lambda: e.get("d"), "rect": lambda: rect_d(e),
                 "circle": lambda: circle_d(e)}[tag]()
        out += path_xml(d, ind, w, filled, e.get("fill-opacity"), stroke,
                        e.get("stroke-opacity"))
    return out


for key in KEYS:
    f = os.path.join(SRC, f"state_{key}.svg")
    root = ET.parse(f).getroot()
    out = ['<?xml version="1.0" encoding="utf-8"?>',
           f'<!-- Generated from docs/UX/option_b/state_{key}.svg. Tint at use site. -->',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
           '    android:width="24dp"', '    android:height="24dp"',
           '    android:viewportWidth="24"', '    android:viewportHeight="24">']
    out += walk(root, root.get("stroke-width"), "    ")
    out.append("</vector>")
    name = f"ic_state_{key}.xml"
    with open(os.path.join(DST, name), "w", newline="\n", encoding="utf-8") as o:
        o.write("\n".join(out) + "\n")
    print(name)
