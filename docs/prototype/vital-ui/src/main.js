import { el, rx, ry, rl } from "./geometry.js";
import { drawDial, DIAL, INNER } from "./dial.js";
import { drawKnob, KNOB } from "./knob.js";
import { drawFab } from "./fab.js";

// ---------------------------------------------------------------- state (bind real data here)
export const state = {
  day: "SUN", date: "APR.9", title: "SUMMARY",
  calories: 306, steps: 11624, km: 4.98, bpm: 128, time: "09:24",
  activity: "walk", intensity: "M",
};
window.vital = state;   // handy for the console / tests: vital.steps = 12000; render()

const frame = document.getElementById("frame");
const svg = el("svg", { id: "art", viewBox: "0 0 393 855", width: 393, height: 855 }, frame);

// ---------------------------------------------------------------- art
const dialClip = el("clipPath", { id: "screenClip" }, el("defs", {}, svg));
el("rect", { x: 0, y: 0, width: 393, height: 855, rx: 55 }, dialClip);
const art = el("g", { "clip-path": "url(#screenClip)" }, svg);
el("rect", { width: 393, height: 855, fill: "var(--orange)" }, art);
drawDial(art);

// pointer notch on the dial's left edge: orange arrowhead with a dark chevron inside
el("path", { d: `M${rx(122)},${ry(879)}L${rx(147.5)},${ry(917)}L${rx(122)},${ry(955)}Z`, fill: "var(--orange)" }, art);
el("path", { d: `M${rx(121.5)},${ry(899)}L${rx(137.5)},${ry(917)}L${rx(121.5)},${ry(935.5)}`, fill: "none", stroke: "var(--ink)", "stroke-width": 1.7, "stroke-linecap": "butt", "stroke-linejoin": "miter" }, art);

const knob = drawKnob(art, { activity: state.activity, onSelect: (a) => { state.activity = a; render(); } });
drawFab(art, { intensity: state.intensity, icon: "walk", onIntensity: (i) => { state.intensity = i; render(); } });

// status bar glyphs
const sb = el("g", { id: "statusbar", fill: "var(--ink)" }, art);
for (let i = 0; i < 4; i++) {
  const h = 5 + i * 3.6;
  el("rect", { x: rx(603 + i * 9.5), y: ry(151) - h / 1.0 * 0.6, width: rl(6.2), height: h * 0.6, rx: 0.6 }, sb);
}
const wifi = el("g", { fill: "none", stroke: "var(--ink)", "stroke-width": 1.5, "stroke-linecap": "round" }, sb);
el("path", { d: `M${rx(643)},${ry(137)}A${rl(18)},${rl(18)} 0 0 1 ${rx(668)},${ry(137)}`, }, wifi);
el("path", { d: `M${rx(647)},${ry(142)}A${rl(12)},${rl(12)} 0 0 1 ${rx(664)},${ry(142)}`, }, wifi);
el("circle", { cx: rx(655.5), cy: ry(148), r: 1.5, fill: "var(--ink)", stroke: "none" }, wifi);
el("rect", { x: rx(682), y: ry(133), width: rl(33), height: rl(17), rx: 3.6, fill: "none", stroke: "var(--ink)", "stroke-opacity": .45, "stroke-width": 1 }, sb);
el("rect", { x: rx(685), y: ry(136), width: rl(27), height: rl(11), rx: 2.2 }, sb);
el("rect", { x: rx(717), y: ry(138), width: rl(3), height: rl(7), rx: 1, "fill-opacity": .5 }, sb);
// dynamic island
el("rect", { x: 135.8, y: 12, width: 121, height: 35.7, rx: 17.85, fill: "#000" }, art);
// home indicator
el("rect", { x: rx(310), y: ry(1495), width: rl(237), height: rl(8), rx: rl(4), fill: "#fff" }, art);

// ---------------------------------------------------------------- text (svg text = exact baselines)
const T = (id, text, attrs) => { const t = el("text", { id, ...attrs }, art); t.textContent = text; return t; };
const sans = { "font-family": "var(--font-sans)" }, mono = { "font-family": "var(--font-mono)" };
const clock = T("t-clock", "9:41", { ...sans, x: rx(143), y: ry(149), "font-size": 15, "font-weight": 600, fill: "var(--ink)" });
T("t-title", state.title, { ...mono, x: rx(430), y: ry(233.5), "text-anchor": "middle", "font-size": 21.5, "font-weight": 500, "letter-spacing": 2.85, fill: "var(--ink)" });
T("t-day", "", { ...mono, x: rx(171.5), y: ry(229), "text-anchor": "middle", "font-size": 16, "font-weight": 700, "letter-spacing": .5, fill: "var(--ink)" });
T("t-date", "", { ...mono, x: rx(686.5), y: ry(229), "text-anchor": "middle", "font-size": 16, "font-weight": 700, "letter-spacing": .5, fill: "var(--ink)" });
T("t-move", "MOVE", { ...sans, x: rx(122), y: ry(312), "font-size": 22, "font-weight": 500, textLength: rl(83), lengthAdjust: "spacingAndGlyphs", fill: "var(--ink)" });
T("t-cal", "", { ...sans, x: rx(125), y: ry(402), "font-size": 60.5, "font-weight": 600, "letter-spacing": -1.2, fill: "var(--ink)" });
T("t-showmore", "SHOW MORE", { ...mono, x: rx(636.5), y: ry(378), "text-anchor": "middle", "font-size": 14, "font-weight": 500, "letter-spacing": 2.2, fill: "var(--ink)" });
T("t-time", "", { ...mono, x: rx(586.5), y: ry(596.5), "text-anchor": "middle", "font-size": 22, "font-weight": 500, fill: "var(--ink)" });
T("t-km", "", { ...sans, x: rx(172), y: ry(935), "font-size": 28.5, "font-weight": 500, fill: "var(--white)" });
T("t-steps-label", "STEPS", { ...sans, x: rx(440), y: ry(863), "font-size": 16, "font-weight": 500, "letter-spacing": -1, fill: "var(--label-gray)" });
T("t-steps", "", { ...mono, x: rx(436), y: ry(947), "font-size": 49, "font-weight": 500, "letter-spacing": 1.8, fill: "var(--white)" });
T("t-bpm", "", { ...sans, x: rx(587.5), y: ry(1059), "text-anchor": "middle", "font-size": 17, "font-weight": 400, fill: "var(--ink)" });

// ---------------------------------------------------------------- pills / chips (drawn in svg, under the text)
const rbox = (x0, y0, x1, y1) => ({ x: rx(x0), y: ry(y0), width: rl(x1 - x0), height: rl(y1 - y0) });
const shapes = el("g", { id: "shapes" });
art.insertBefore(shapes, art.querySelector("#t-clock"));
el("rect", { ...rbox(122, 199, 221, 240), rx: 4, fill: "var(--orange-chip)" }, shapes);
el("rect", { ...rbox(637, 199, 736, 240), rx: 4, fill: "var(--orange-chip)" }, shapes);
el("rect", { ...rbox(536, 341, 737, 400), rx: 7.5, fill: "none", stroke: "var(--ink)", "stroke-width": 2.6 }, shapes);
const tg = el("linearGradient", { id: "timePill", x1: 0, y1: 0, x2: 0, y2: 1 }, el("defs", {}, shapes));
[[0, "#3a4144"], [0.1, "#5b656c"], [0.25, "#5f6a72"], [0.75, "#5e6870"], [0.9, "#5a646b"], [1, "#3c4346"]].forEach(([o, c]) => el("stop", { offset: o, "stop-color": c }, tg));
el("rect", { ...rbox(505, 561, 668, 607), rx: 8.5, fill: "url(#timePill)" }, shapes);
el("rect", { ...rbox(506, 1025, 669, 1073), rx: rl(24), fill: "var(--orange)" }, shapes);

// transparent hit areas for the real controls
const hit = (tag, label, b, ev) => {
  const d = document.createElement(tag);
  d.className = "hit"; d.setAttribute("aria-label", label);
  Object.assign(d.style, { left: b.x + "px", top: b.y + "px", width: b.width + "px", height: b.height + "px" });
  d.addEventListener("click", () => window.dispatchEvent(new CustomEvent(ev)));
  frame.appendChild(d);
};
hit("button", "Show more", rbox(536, 341, 737, 400), "vital:show-more");
hit("button", "Choose day", rbox(122, 199, 221, 240), "vital:pick-day");
hit("button", "Choose date", rbox(637, 199, 736, 240), "vital:pick-date");

// ---------------------------------------------------------------- render
export function render() {
  const $ = (id) => document.getElementById(id);
  $("t-day").textContent = state.day;
  $("t-date").textContent = state.date;
  $("t-cal").textContent = `${state.calories}cal`;
  $("t-time").textContent = state.time;
  $("t-km").textContent = `${state.km.toFixed(2)} km`;
  $("t-steps").textContent = state.steps.toLocaleString("de-DE");   // 11.624
  $("t-bpm").textContent = `${state.bpm}bpm`;
  knob.setActivity(state.activity);
  document.querySelectorAll("[data-intensity]").forEach((n) => n.setAttribute("opacity", n.dataset.intensity === state.intensity ? 1 : 0.78));
}
render();
document.documentElement.dataset.ready = "1";
