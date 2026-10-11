# docs/UX/tools — generators

Python 3, no dependencies. Run from any folder; paths resolve relative to this folder.

| Script | Output |
|---|---|
| `gen_icons.py` | `docs/UX/option_a/*.svg`, `docs/UX/option_b/*.svg` (edit glyphs in `GLYPHS`) |
| `gen_preview.py` | `docs/UX/preview.html` (icon grid, both options) |
| `gen_layout.py` | `docs/UX/layout_preview.html` (L1–L3 layout mocks) |
| `svg2vd.py` | `app/src/main/res/drawable/ic_state_*.xml` from Option A (skips `SKIP`) |
| `svg2vd_b.py` | same, for the Option B selections (`SKIP` in `svg2vd.py`: 0x04–0x06) |

After changing an icon, run in order:
```
python gen_icons.py
python gen_preview.py
python gen_layout.py
python svg2vd.py
python svg2vd_b.py
```
