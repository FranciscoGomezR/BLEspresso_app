"""Build docs/UX/layout_DarkLightMode_preview.html: L3 layout + light/dark theme switch (TODO #13)."""
import json
import os

UX = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # docs/UX
OPTION_B = ("04", "05", "06")


def svg(key):
    opt = "option_b" if key[:2] in OPTION_B else "option_a"
    with open(os.path.join(UX, opt, f"state_{key}.svg"), encoding="utf-8") as f:
        return f.read().strip()


ICONS = {k: svg(k) for k in ("00_heating_on", "02_ready", "03_brewing", "04_brew_preinfuse",
                             "05_brew_infuse", "06_brew_taper", "07_brew_autostop",
                             "08_steaming", "10_step_off", "11_step_on")}

# Theme switch icons (same stroke style as ic_state_*). Shown icon = theme you switch TO.
SUN = ('<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" '
       'stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2.5v2M12 19.5v2'
       'M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4"/>'
       '</svg>')
MOON = ('<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" '
        'stroke-linejoin="round"><path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z"/></svg>')

# code -> (top-bar label, slot text, slot, icon, tint role, pulse, lamp)   README §8.2
STATES = {
    0x00: ("HEATING", "Heating", "boiler", "00_heating_on", "heat", True, False),
    0x01: ("HEATER OFF", "Heater off", "boiler", "00_heating_on", "off", False, False),
    0x02: ("READY", "Ready", "boiler", "02_ready", "ready", False, False),
    0x03: ("BREWING", "Brewing", "brew", "03_brewing", "heat", True, False),
    0x04: ("PRE-INFUSION [BREWING]", "Pre-infusion", "brew", "04_brew_preinfuse", "heat", True, True),
    0x05: ("INFUSION [BREWING]", "Infusion", "brew", "05_brew_infuse", "heat", True, True),
    0x06: ("TAPERING [BREWING]", "Tapering", "brew", "06_brew_taper", "heat", True, True),
    0x07: ("SHOT DONE", "Shot done", "brew", "07_brew_autostop", "ready", False, False),
    0x08: ("STEAMING", "Steaming", "steam", "08_steaming", "heat", True, False),
    0x10: ("STEP OFF", "Step off", "step", "10_step_off", "off", False, False),
    0x11: ("STEP ON", "Step on", "step", "11_step_on", "heat", True, False),
}

HTML = r"""<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Dark Light Preview</title>
<style>
/* Values 1:1 with ui/theme/Color.kt PALETTE_GAGGIA_LIGHT / PALETTE_GAGGIA_DARK */
.light{--bg:#F4EFE6;--bg_i:#F4EFE6;--surface:#DDDFE2;--surface_i:#B8BCC2;--accent:#C8102E;
--accent_i:#C8102E;--tp:#1A1A1A;--ts:#4A4D52;--tb_bg:#FFFFFF;--off:#1A1A1A61;--heat:#E07A1F;
--ready:#2E9E5B;--on_accent:#FFFFFF;--statusbar:#DDDFE2;--sb_text:#1A1A1A}
.dark{--bg:#1A1A1A;--bg_i:#121212;--surface:#2A2C30;--surface_i:#3A3D42;--accent:#E53950;
--accent_i:#E53950;--tp:#F2F2F2;--ts:#B8BCC2;--tb_bg:#3A3D42;--off:#F2F2F261;--heat:#FF9A3D;
--ready:#4CC97E;--on_accent:#FFFFFF;--statusbar:#2A2C30;--sb_text:#F2F2F2}
*{box-sizing:border-box}
body{margin:0;padding:16px;background:#cfcfcf;color:#222;font:14px/1.4 system-ui,sans-serif}
h1{font-size:20px;margin:0 0 4px}
.ctl{display:flex;flex-wrap:wrap;gap:6px;margin:10px 0}
.ctl button{border:1px solid #888;background:#fff;color:#222;border-radius:6px;
padding:4px 8px;font:12px ui-monospace,monospace;cursor:pointer}
.ctl button.sel{background:#222;color:#fff}
.lbl{font-weight:600;margin-right:4px;align-self:center}
.mocks{display:flex;flex-wrap:wrap;gap:20px}
.mock{width:min(380px,100%)}
.mock h2{font-size:15px;margin:8px 0 2px}.mock p{margin:0 0 8px;font-size:12px;opacity:.8}
.phone{background:var(--bg);color:var(--tp);border-radius:22px;overflow:hidden;
border:6px solid #111;height:740px;display:flex;flex-direction:column;transition:background .2s}
.sbar{background:var(--statusbar);color:var(--sb_text);font-size:11px;padding:4px 14px;
display:flex;justify-content:space-between}
.bar{display:flex;align-items:center;gap:10px;padding:10px 14px;background:var(--surface)}
.chip{color:var(--accent_i);font-weight:700;letter-spacing:.1em;font-size:13px;
border:1.5px solid var(--accent_i);border-radius:12px;padding:1px 10px}
.state{font-size:13px;color:var(--tp)}
.toggle{margin-left:auto;width:32px;height:32px;border-radius:50%;border:0;background:none;
color:var(--tp);display:flex;align-items:center;justify-content:center;padding:4px}
.toggle svg{width:24px;height:24px}
.toggle.live{cursor:pointer;outline:2px dashed var(--accent_i);outline-offset:2px}
.toggle.live:hover{background:color-mix(in srgb,var(--tp) 10%,transparent)}
.dialrow{background:var(--bg_i);display:flex;align-items:center;justify-content:center;
flex:0 0 250px}
.dial{width:200px;height:200px;border-radius:50%;
background:conic-gradient(var(--accent_i) 0 var(--pct),var(--surface_i) var(--pct) 100%);
display:flex;align-items:center;justify-content:center}
.dial>div{width:176px;height:176px;border-radius:50%;background:var(--bg_i);
display:flex;flex-direction:column;align-items:center;justify-content:center}
.temp{font-size:34px;font-weight:600}.sp{font-size:12px;color:var(--ts)}
.side{display:flex;flex-direction:column;gap:2px;margin-left:16px}
.slot{display:flex;flex-direction:column;align-items:center;width:52px}
.slot i{display:flex;width:34px;height:34px;align-items:center;justify-content:center;color:var(--off)}
.slot i svg{width:30px;height:30px}
.slot b{font-weight:400;font-size:10px;color:var(--ts);text-align:center}
.slot.on i{color:var(--c)}
.slot.on i svg{filter:drop-shadow(0 0 3px var(--c)) drop-shadow(0 0 6px var(--c))}
.slot.on.lamp i svg{filter:none}
.slot.on.pulse i svg{animation:p 2s ease-in-out infinite}
.slot.on.lamp.pulse i svg{animation:none}
.slot.on b{color:var(--tp);font-weight:600}
@keyframes p{50%{filter:drop-shadow(0 0 1px var(--c))}}
@media (prefers-reduced-motion:reduce){.slot.on.pulse i svg{animation:none}}
.cfg{flex:1;background:var(--bg);padding:8px 14px;display:flex;flex-direction:column;gap:6px}
.cfg .t{font-size:15px;border-bottom:1px solid var(--tp);padding-bottom:2px}
.blk{background:var(--surface);border-radius:6px;color:var(--ts);font-size:11px;
display:flex;align-items:center;justify-content:center}
.blk .tb{background:var(--tb_bg);color:var(--tp);border-radius:4px;padding:2px 10px;margin-left:8px}
.busy{height:18px;text-align:center;font-size:13px;color:var(--tp);margin-top:6px}
.btns{display:flex;gap:10px;margin:8px 0 14px}
.btns span{background:var(--accent);color:var(--on_accent);border-radius:4px;padding:8px 14px;font-size:12px}
.btns.dis span{background:var(--surface);color:var(--off)}
.btns .r{margin-left:auto}
.note{font-size:12px;margin-top:14px;max-width:800px}
</style></head><body>
<h1>Light / dark theme switch (TODO #13) — L3 layout</h1>
<div>Switch = sun/moon icon at the right end of the top bar (main screen only). Icon shows the
theme you switch <b>to</b>. Choice is remembered across sessions.</div>
<div class="ctl" id="st"><span class="lbl">State</span></div>
<div class="ctl" id="md"><span class="lbl">Mode</span></div>
<div class="mocks">
 <div class="mock"><h2>Gaggia light</h2><p>Moon icon = "switch to dark".</p>
  <div class="phone light" data-fixed="1"></div></div>
 <div class="mock"><h2>Gaggia dark</h2><p>Sun icon = "switch to light".</p>
  <div class="phone dark" data-fixed="1"></div></div>
 <div class="mock"><h2>Interactive</h2><p>Tap the outlined icon to toggle this phone.</p>
  <div class="phone light" id="live"></div></div>
</div>
<p class="note">Notes: status bar follows the palette (<code>statusbar</code>); toggle stays
enabled while the machine is busy; Controller screen has no switch but follows the chosen theme.</p>
<script>
const ICONS=__ICONS__, STATES=__STATES__, SUN=__SUN__, MOON=__MOON__;
const MODES=["CLASSIC","PROFILE","STEP FUNCTION"];
const ROLE={heat:"--heat",ready:"--ready",off:"--off"};
const SLOTS=[["boiler","Boiler","00_heating_on"],["brew","Brew","03_brewing"],
 ["steam","Steam","08_steaming"],["step","Step","10_step_off"]];
let cur=0x00, mode=1;
function busy(){return (cur>=0x03&&cur<=0x06)||cur===0x08}
function slot(s){const st=STATES[cur],on=st[2]===s[0],lit=on&&st[4]!=="off";
 const icon=on?ICONS[st[3]]:ICONS[s[2]];
 const cls="slot"+(lit?" on":"")+(lit&&st[5]?" pulse":"")+(lit&&st[6]?" lamp":"");
 return `<span class="${cls}" style="--c:var(${ROLE[st[4]]})"><i>${icon}</i><b>${on?st[1]:s[1]}</b></span>`}
function phone(el){
 const dark=el.classList.contains("dark"), st=STATES[cur], live=el.id==="live";
 const b=busy();
 el.innerHTML=`<div class="sbar"><span>12:30</span><span>&#9679; 87%</span></div>
 <div class="bar"><span class="chip">${MODES[mode]}</span><span class="state">${st[0]}</span>
  <button class="toggle${live?" live":""}" title="${dark?"Switch to light theme":"Switch to dark theme"}">${dark?SUN:MOON}</button></div>
 <div class="dialrow"><div class="dial" style="--pct:${cur===0?"70%":"100%"}"><div>
  <span class="temp">${cur===0?"68":"93"}°C</span><span class="sp">set 93°C</span></div></div>
  <div class="side">${SLOTS.map(slot).join("")}</div></div>
 <div class="cfg"><div class="t">Machine Configuration</div>
  <div class="blk" style="height:60px">Brew / Steam presets <span class="tb">93.0</span><span class="tb">135.0</span></div>
  <div class="blk" style="height:180px">Brew profile table <span class="tb">50.0</span></div>
  <div class="busy">${b?"Machine busy":""}</div>
  <div class="btns${b?" dis":""}"><span>Controller</span><span class="r">Read</span><span>Write</span></div></div>`;
 if(live) el.querySelector(".toggle").onclick=()=>{el.classList.toggle("dark");
  el.classList.toggle("light");phone(el)};
}
function render(){document.querySelectorAll(".phone").forEach(phone);
 document.querySelectorAll("#st button").forEach(x=>x.classList.toggle("sel",+x.dataset.v===cur));
 document.querySelectorAll("#md button").forEach(x=>x.classList.toggle("sel",+x.dataset.v===mode))}
function btns(id,items,cb){const c=document.getElementById(id);items.forEach(([v,t])=>{
 const x=document.createElement("button");x.dataset.v=v;x.textContent=t;
 x.onclick=()=>{cb(v);render()};c.appendChild(x)})}
btns("st",Object.keys(STATES).map(k=>[+k,"0x"+(+k).toString(16).padStart(2,"0")+" "+STATES[k][0]]),v=>cur=v);
btns("md",MODES.map((t,i)=>[i,i+" "+t]),v=>mode=v);
render();
</script>
</body></html>
"""

out = (HTML.replace("__ICONS__", json.dumps(ICONS))
       .replace("__STATES__", json.dumps({str(k): v for k, v in STATES.items()}))
       .replace("__SUN__", json.dumps(SUN)).replace("__MOON__", json.dumps(MOON)))
with open(os.path.join(UX, "layout_DarkLightMode_preview.html"), "w", newline="\n",
          encoding="utf-8") as f:
    f.write(out)
print("ok")
