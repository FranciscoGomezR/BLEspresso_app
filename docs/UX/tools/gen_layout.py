"""Build docs/UX/layout_preview.html: 3 RemoteControlScreen status layouts (TODO #8)."""
import json
import os

UX = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # docs/UX


def svg(key):
    opt = "option_b" if key[:2] in ("04", "05", "06") else "option_a"
    with open(os.path.join(UX, opt, f"state_{key}.svg")) as f:
        return f.read().strip()


ICONS = {k: svg(k) for k in ("00_heating_on", "02_ready", "03_brewing", "04_brew_preinfuse",
                             "05_brew_infuse", "06_brew_taper", "07_brew_autostop",
                             "08_steaming", "10_step_off", "11_step_on")}

# code -> (name, label, slot, icon, role, pulse)
STATES = {
    0x00: ("HEATING_ON", "Heating", "boiler", "00_heating_on", "heat", True),
    0x01: ("HEATING_OFF", "Heater off", "boiler", "00_heating_on", "off", False),
    0x02: ("READY", "Ready", "boiler", "02_ready", "ready", False),
    0x03: ("BREWING", "Brewing", "brew", "03_brewing", "heat", True),
    0x04: ("BREW_PREINFUSE", "Pre-infusion", "brew", "04_brew_preinfuse", "heat", True),
    0x05: ("BREW_INFUSE", "Infusion", "brew", "05_brew_infuse", "heat", True),
    0x06: ("BREW_TAPER", "Declining", "brew", "06_brew_taper", "heat", True),
    0x07: ("BREW_AUTOSTOP", "Shot done", "brew", "07_brew_autostop", "ready", False),
    0x08: ("STEAMING", "Steaming", "steam", "08_steaming", "heat", True),
    0x10: ("STEP_OFF", "Step off", "step", "10_step_off", "off", False),
    0x11: ("STEP_ON", "Step on", "step", "11_step_on", "heat", True),
}
# idle icon per slot when the slot is not the active one
IDLE = {"boiler": "00_heating_on", "brew": "03_brewing", "steam": "08_steaming",
        "step": "10_step_off"}

HTML = r"""<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Status Layout Proposals</title>
<style>
/* Palettes 1:1 with ui/theme/Color.kt: #1/#3 current, #7 Gaggia light, #8 Gaggia dark */
:root,[data-pal=p1]{--bg:#EEEEEE;--bg_i:#EEEEEE;--surface:#C3C8D3;--accent:#FFBD76;
--accent_i:#EA9216;--tp:#313841;--ts:#3A4750;--tb_bg:#C3C8D3;--off:#31384161;
--heat:#E07A1F;--ready:#2E9E5B;--brew:#7A4A2A;--steam:#5B8DB8}
[data-pal=p7]{--bg:#F4EFE6;--bg_i:#F4EFE6;--surface:#DDDFE2;--accent:#C8102E;
--accent_i:#C8102E;--tp:#1A1A1A;--ts:#4A4D52;--tb_bg:#FFFFFF;--off:#1A1A1A61;
--heat:#E07A1F;--ready:#2E9E5B;--brew:#7A4A2A;--steam:#5B8DB8}
[data-pal=p8]{--bg:#1A1A1A;--bg_i:#121212;--surface:#2A2C30;--accent:#E53950;
--accent_i:#E53950;--tp:#F2F2F2;--ts:#B8BCC2;--tb_bg:#3A3D42;--off:#F2F2F261;
--heat:#FF9A3D;--ready:#4CC97E;--brew:#C08A5E;--steam:#8EC1EC}
*{box-sizing:border-box}
body{margin:0;padding:16px;background:#d9d9d9;color:#222;font:14px/1.4 system-ui,sans-serif}
@media (prefers-color-scheme:dark){body{background:#202020;color:#eee}}
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
border:6px solid #111;height:720px;display:flex;flex-direction:column}
.dialrow{background:var(--bg_i);display:flex;align-items:center;justify-content:center;
position:relative;flex:0 0 250px}
.dial{width:200px;height:200px;border-radius:50%;
background:conic-gradient(var(--accent_i) 0 var(--pct),var(--surface) var(--pct) 100%);
display:flex;align-items:center;justify-content:center}
.dial>div{width:176px;height:176px;border-radius:50%;background:var(--bg_i);
display:flex;flex-direction:column;align-items:center;justify-content:center}
.temp{font-size:34px;font-weight:600}.sp{font-size:12px;color:var(--ts)}
.cfg{flex:1;background:var(--bg);padding:8px 14px;display:flex;flex-direction:column;gap:6px}
.cfg .t{font-size:15px;border-bottom:1px solid var(--tp);padding-bottom:2px}
.blk{background:var(--surface);border-radius:6px;color:var(--ts);font-size:11px;
display:flex;align-items:center;justify-content:center;opacity:.7}
.btns{display:flex;gap:10px;justify-content:flex-end}
.btns span{background:var(--accent);color:var(--tp);border-radius:4px;padding:6px 14px;
font-size:12px}
.mode{color:var(--accent_i);font-weight:700;letter-spacing:.1em;font-size:13px}
.state{color:var(--tp);font-size:13px}
.slot{display:inline-flex;flex-direction:column;align-items:center;gap:1px;width:52px}
.slot i{display:inline-flex;width:34px;height:34px;align-items:center;justify-content:center;
color:var(--off)}
.slot i svg{width:30px;height:30px}
.slot b{font-weight:400;font-size:10px;color:var(--ts);white-space:nowrap}
.slot.on i{color:var(--c)}
.slot.on i svg{filter:drop-shadow(0 0 3px var(--c)) drop-shadow(0 0 6px var(--c))}
.slot.on.pulse i svg{animation:p 2s ease-in-out infinite}
.slot.on b{color:var(--tp);font-weight:600}
@keyframes p{50%{filter:drop-shadow(0 0 1px var(--c))}}
@media (prefers-reduced-motion:reduce){.slot.on.pulse i svg{animation:none}}
/* L1 */
.strip{display:flex;align-items:center;justify-content:space-between;padding:6px 12px;
background:var(--surface);border-top:1px solid var(--off);border-bottom:1px solid var(--off)}
/* L2 */
.corner{position:absolute}.tl{top:10px;left:10px}.tr{top:10px;right:10px}
.bl{bottom:10px;left:10px}.br{bottom:10px;right:10px}
.l2mode{position:absolute;top:8px;left:0;right:0;text-align:center}
/* L3 */
.bar{display:flex;align-items:center;gap:10px;padding:10px 14px;background:var(--surface)}
.chip{border:1.5px solid var(--accent_i);border-radius:12px;padding:1px 10px}
.side{display:flex;flex-direction:column;gap:2px;margin-left:16px}
</style></head><body>
<h1>RemoteControlScreen status layouts (TODO #8)</h1>
<div>Pick a machine state and palette; all three mocks update. Icons: Option A; 0x04–0x06 Option B.</div>
<div class="ctl" id="st"><span class="lbl">State</span></div>
<div class="ctl" id="md"><span class="lbl">Mode</span></div>
<div class="ctl" id="pl"><span class="lbl">Palette</span></div>
<div class="mocks">
 <div class="mock"><h2>L1 — Status strip</h2>
  <p>Strip between dial and configuration. Least change; dial untouched.</p>
  <div class="phone" data-l="1"></div></div>
 <div class="mock"><h2>L2 — Dial corners</h2>
  <p>Slots in dial-row corners, state text inside dial. No extra height.</p>
  <div class="phone" data-l="2"></div></div>
 <div class="mock"><h2>L3 — Header bar + side column (SELECTED)</h2>
  <p>Top bar: mode chip + state. Slots stacked right of dial.</p>
  <div class="phone" data-l="3"></div></div>
</div>
<script>
const ICONS=__ICONS__, STATES=__STATES__, IDLE=__IDLE__;
const MODES=["CLASSIC","PROFILE","STEP FUNCTION"];
const ROLE={heat:"--heat",ready:"--ready",brew:"--brew",steam:"--steam",off:"--off"};
const SLOTS=[["boiler","Boiler"],["brew","Brew"],["steam","Steam"],["step","Step"]];
let cur=0x00, mode=0;
function slot(s){const st=STATES[cur],on=st[2]===s[0],
 key=on?st[3]:IDLE[s[0]],lit=on&&st[4]!=="off";
 return `<span class="slot${lit?" on":""}${lit&&st[5]?" pulse":""}"
 style="--c:var(${ROLE[st[4]]})"><i>${ICONS[key]}</i><b>${on?st[1]:s[1]}</b></span>`}
function dial(inner){return `<div class="dial" style="--pct:${cur===0?"70%":"100%"}"><div>
 <span class="temp">${cur===0?"68":"93"}°C</span><span class="sp">set 93°C</span>${inner||""}
 </div></div>`}
const CFG=`<div class="cfg"><div class="t">Machine Configuration</div>
 <div class="blk" style="height:60px">Brew / Steam temperature presets</div>
 <div class="blk" style="height:190px">Brew profile table (Pre-Infusion · Infusion · Declining)</div>
 <div class="btns" style="margin:14px 0"><span>Controller</span><span style="margin-left:auto">Read</span><span>Write</span></div></div>`;
function render(){
 const st=STATES[cur],m=`<span class="mode">${MODES[mode]}</span>`,
  sl=SLOTS.map(slot);
 const L={
 1:`<div class="dialrow">${dial()}</div>
   <div class="strip">${m}<span>${sl.join("")}</span></div>${CFG}`,
 2:`<div class="dialrow" style="flex-basis:270px"><div class="l2mode">${m}</div>
   ${dial(`<span class="state" style="margin-top:4px">${st[1]}</span>`)}
   <span class="corner tl">${sl[0]}</span><span class="corner tr">${sl[1]}</span>
   <span class="corner bl">${sl[2]}</span><span class="corner br">${sl[3]}</span></div>${CFG}`,
 3:`<div class="bar"><span class="mode chip">${MODES[mode]}</span>
   <span class="state">${st[1]}</span></div>
   <div class="dialrow">${dial()}<div class="side">${sl.join("")}</div></div>${CFG}`};
 document.querySelectorAll(".phone").forEach(p=>p.innerHTML=L[p.dataset.l]);
 document.querySelectorAll("#st button").forEach(b=>b.classList.toggle("sel",+b.dataset.v===cur));
 document.querySelectorAll("#md button").forEach(b=>b.classList.toggle("sel",+b.dataset.v===mode));
}
function btns(id,items,cb){const c=document.getElementById(id);items.forEach(([v,t])=>{
 const b=document.createElement("button");b.dataset.v=v;b.textContent=t;
 b.onclick=()=>{cb(v,b);render()};c.appendChild(b)})}
btns("st",Object.keys(STATES).map(k=>[+k,"0x"+(+k).toString(16).padStart(2,"0")+" "+STATES[k][0]]),v=>cur=v);
btns("md",MODES.map((t,i)=>[i,i+" "+t]),v=>mode=v);
btns("pl",[["p1","#1/#3 current"],["p7","#7 Gaggia light"],["p8","#8 Gaggia dark"]],(v,b)=>{
 document.documentElement.dataset.pal=v;
 document.querySelectorAll("#pl button").forEach(x=>x.classList.toggle("sel",x===b))});
document.querySelector("#pl button").classList.add("sel");
render();
</script>
</body></html>
"""

out = (HTML.replace("__ICONS__", json.dumps(ICONS))
       .replace("__STATES__", json.dumps({str(k): v for k, v in STATES.items()}))
       .replace("__IDLE__", json.dumps(IDLE)))
with open(os.path.join(UX, "layout_preview.html"), "w", newline="\n", encoding="utf-8") as f:
    f.write(out)
print("ok")
