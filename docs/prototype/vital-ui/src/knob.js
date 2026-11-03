import { el, polar, rx, ry, rl } from "./geometry.js";
import { ICONS } from "./icons.js";

// Everything on the knob was measured on a 3x zoomed crop of the reference whose origin is at
// reference px (110, 440) -> `crop3(x, y)` maps those crop coordinates to css px in the frame.
const crop3 = (x, y) => [rx(110 + x / 3), ry(440 + y / 3)];
const crop3l = (v) => rl(v / 3);

// subpixel fits on the reference (see design notes): ring edge r=74.61, scallop mean r=69.67, 60 teeth, pin r=11.31
export const KNOB = { x: 87.85, y: 295.1, outer: 74.61, tips: 70.3, teeth: 60, toothDepth: 1.23, pin: 11.31 };

const TEAL = "#fd3f00";
// spoke angle = where the radial spoke leaves the arc; iconAngle = where the glyph centroid sits (both measured)
export const ACTIVITIES = [
  { id: "steps", angle: 16.3,  iconAngle: 17.4 },
  { id: "walk",  angle: 31.25, iconAngle: 30.7 },
  { id: "run",   angle: 46.2,  iconAngle: 45.5 },
  { id: "bike",  angle: 61.15, iconAngle: 59.2 },
  { id: "moto",  angle: 76.1,  iconAngle: 72.0 },
];
const ARC_R = 92.9;                   // spoke arc radius (css px)
const SPOKE_LEN = 17.9;
const ICON_R = 123.4;                 // glyph centroid distance from knob centre
const ICON_SCALE = 0.66;              // css px per glyph unit

function scallopPath(cx, cy, r, n, depth) {
  const pts = [];
  const steps = n * 6;
  for (let i = 0; i < steps; i++) {
    const a = (i / steps) * Math.PI * 2;
    const rr = r - depth * (0.5 + 0.5 * Math.cos(a * n));
    pts.push([cx + rr * Math.cos(a), cy + rr * Math.sin(a)]);
  }
  return "M" + pts.map((p) => p[0].toFixed(2) + "," + p[1].toFixed(2)).join("L") + "Z";
}

export function drawKnob(svg, { activity = "walk", onSelect } = {}) {
  const g = el("g", { id: "knob" }, svg);
  const { x, y, outer } = KNOB;

  // dark ring + serrated orange disc
  el("circle", { cx: x, cy: y, r: outer, fill: "var(--ink)" }, g);
  el("path", { d: scallopPath(x, y, KNOB.tips, KNOB.teeth, KNOB.toothDepth), fill: TEAL }, g);
  el("circle", { cx: x, cy: y, r: KNOB.pin, fill: "var(--ink)" }, g);   // centre pin

  // needle (rotates with the selected activity)
  const needle = el("g", { id: "needle", transform: `rotate(0 ${x} ${y})` }, g);
  const [n0x, n0y] = [x + 29.1, y + 28.4], [n1x, n1y] = [x + 41.0, y + 40.4];
  el("line", { x1: n0x, y1: n0y, x2: n1x, y2: n1y, stroke: "var(--ink)", "stroke-width": 3.4, "stroke-linecap": "round" }, needle);

  // branching selector: arc + radial spokes with rounded corners at both ends
  const sel = el("g", { id: "activity-selector", fill: "none", stroke: TEAL, "stroke-width": 2.1, "stroke-linecap": "round", "stroke-linejoin": "round" }, g);
  const first = ACTIVITIES[0].angle, last = ACTIVITIES.at(-1).angle;
  const [ax, ay] = polar(x, y, ARC_R, first), [bx, by] = polar(x, y, ARC_R, last);
  const [sx, sy] = polar(x, y, ARC_R + SPOKE_LEN, first), [ex, ey] = polar(x, y, ARC_R + SPOKE_LEN, last);
  el("path", { d: `M${sx},${sy}L${ax},${ay}A${ARC_R},${ARC_R} 0 0 1 ${bx},${by}L${ex},${ey}` , "stroke-linejoin": "round" }, sel);
  ACTIVITIES.slice(1, -1).forEach((a) => {
    const [p0x, p0y] = polar(x, y, ARC_R, a.angle), [p1x, p1y] = polar(x, y, ARC_R + SPOKE_LEN, a.angle);
    el("line", { x1: p0x, y1: p0y, x2: p1x, y2: p1y }, sel);
  });

  // activity icons at the end of each spoke
  const icons = el("g", { id: "activity-icons" }, g);
  ACTIVITIES.forEach((a) => {
    const [ix, iy] = polar(x, y, ICON_R, a.iconAngle);
    const grp = el("g", { class: "activity", "data-activity": a.id, transform: `translate(${ix},${iy})`, style: "cursor:pointer" }, icons);
    el("circle", { r: 12, fill: "transparent" }, grp);                       // generous hit area
    if (a.id === "steps") {
      // two foot prints
      el("g", { fill: TEAL }, grp).innerHTML =
        `<g transform="translate(-3.6,1.8) rotate(-4)"><rect x="-2.6" y="-4" width="5.2" height="6.4" rx="2.6"/><rect x="-2" y="3" width="4" height="2.6" rx="1.3"/></g>` +
        `<g transform="translate(3.6,-1.4) rotate(-4)"><rect x="-2.6" y="-4" width="5.2" height="6.4" rx="2.6"/><rect x="-2" y="3" width="4" height="2.6" rx="1.3"/></g>`;
    } else {
      el("path", { d: ICONS[a.id], fill: TEAL, transform: `scale(${ICON_SCALE}) translate(-12,-12)` }, grp);
    }
    grp.addEventListener("click", () => onSelect?.(a.id));
  });

  const setActivity = (id) => {
    const a = ACTIVITIES.find((v) => v.id === id) ?? ACTIVITIES[1];
    needle.setAttribute("transform", `rotate(${(a.angle - ACTIVITIES[1].angle) * 0.9} ${x} ${y})`);
    icons.querySelectorAll(".activity").forEach((n) => n.setAttribute("opacity", n.dataset.activity === id ? 1 : 0.85));
  };
  setActivity(activity);
  return { setActivity };
}
