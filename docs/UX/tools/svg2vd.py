"""Convert docs/UX/option_a/*.svg to res/drawable/ic_state_*.xml VectorDrawables."""
import glob
import os
import xml.etree.ElementTree as ET

UX = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # docs/UX
SRC = os.path.join(UX, "option_a")
DST = os.path.join(UX, "..", "..", "app", "src", "main", "res", "drawable")
SKIP = ("04_brew_preinfuse", "05_brew_infuse", "06_brew_taper")  # Option B: svg2vd_b.py
NS = "{http://www.w3.org/2000/svg}"


def rect_d(e):
    x, y, w, h = (float(e.get(k)) for k in ("x", "y", "width", "height"))
    r = float(e.get("rx", 0))
    if not r:
        return f"M{x},{y}h{w}v{h}h{-w}z"
    return (f"M{x + r},{y}h{w - 2 * r}a{r},{r} 0 0 1 {r},{r}v{h - 2 * r}"
            f"a{r},{r} 0 0 1 {-r},{r}h{-(w - 2 * r)}a{r},{r} 0 0 1 {-r},{-r}"
            f"v{-(h - 2 * r)}a{r},{r} 0 0 1 {r},{-r}z")


def circle_d(e):
    cx, cy, r = (float(e.get(k)) for k in ("cx", "cy", "r"))
    return f"M{cx - r},{cy}a{r},{r} 0 1 0 {2 * r},0a{r},{r} 0 1 0 {-2 * r},0z"


def walk(node, sw):
    for e in node:
        tag = e.tag.replace(NS, "")
        w = e.get("stroke-width", sw)
        if tag == "g":
            yield from walk(e, w)
            continue
        d = {"path": lambda: e.get("d"), "rect": lambda: rect_d(e),
             "circle": lambda: circle_d(e)}[tag]()
        filled = e.get("fill") == "currentColor"
        yield d, w, filled


def main():
    os.makedirs(DST, exist_ok=True)
    for f in sorted(glob.glob(os.path.join(SRC, "state_*.svg"))):
        if os.path.basename(f)[6:-4] in SKIP:
            continue
        convert(f)


def convert(f):
    root = ET.parse(f).getroot()
    out = ['<?xml version="1.0" encoding="utf-8"?>',
           f'<!-- Generated from docs/UX/option_a/{os.path.basename(f)}. Tint at use site. -->',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
           '    android:width="24dp"', '    android:height="24dp"',
           '    android:viewportWidth="24"', '    android:viewportHeight="24">']
    for d, w, filled in walk(root, root.get("stroke-width")):
        out.append("    <path")
        out.append(f'        android:pathData="{d}"')
        out.append('        android:fillColor="#FF000000"' if filled
                   else '        android:fillColor="#00000000"')
        out += ['        android:strokeColor="#FF000000"',
                f'        android:strokeWidth="{w}"',
                '        android:strokeLineCap="round"',
                '        android:strokeLineJoin="round" />']
    out.append("</vector>")
    name = "ic_" + os.path.basename(f)[:-4] + ".xml"
    with open(os.path.join(DST, name), "w", newline="\n", encoding="utf-8") as o:
        o.write("\n".join(out) + "\n")
    print(name)


if __name__ == "__main__":
    main()
