import { TICKS } from "./tick-data.js";
import { el, polar } from "./geometry.js";

// Big dial: centre + radius were fitted to the reference (scripts/extract_ticks.py).
export const DIAL = { cx: TICKS.center[0], cy: TICKS.center[1], r: 277.56 };
// Inner "steps" dial.
export const INNER = { r: 122, ringR: 117.9 };

/** Fill ticks that were hidden behind other UI in the reference using their neighbours. */
function completeTicks(ticks) {
  const ok = (t) => t.r0 !== undefined;
  return ticks.map((t, i) => {
    if (ok(t)) return t;
    let a = i - 1, b = i + 1;
    while (a >= 0 && !ok(ticks[a])) a--;
    while (b < ticks.length && !ok(ticks[b])) b++;
    const A = ticks[a], B = ticks[b], s = A || B;
    if (!s) return null;
    const avg = (k) => (A && B ? (A[k] + B[k]) / 2 : s[k]);
    return { ...t, r0: avg("r0"), r1: avg("r1"), w: avg("w"), c: (A || B).c };
  }).filter(Boolean);
}

/**
 * Data-driven tick tracks. `values` can override tick length per track so the dial reflects
 * real activity: values.bars = [0..1] scales the long orange bars.
 */
export function drawDial(svg, { values = {} } = {}) {
  const { cx, cy } = DIAL;
  const g = el("g", { id: "dial" }, svg);

  // body: outer rim shadow -> face
  const defs = el("defs", {}, g);
  const face = el("radialGradient", { id: "dialFace", cx: cx, cy: cy, r: DIAL.r, gradientUnits: "userSpaceOnUse" }, defs);
  [[0, "#070707"], [0.86, "#080808"], [0.93, "#0b0b0b"], [0.975, "#050505"], [1, "#000"]].forEach(([o, c]) =>
    el("stop", { offset: o, "stop-color": c }, face));
  el("circle", { cx, cy, r: DIAL.r, fill: "url(#dialFace)" }, g);

  drawInnerDial(g);                 // face first, so the rim ticks below sit on top of it

  const widthScale = parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--tick-width-scale")) || 1;
  const tracks = g.appendChild(el("g", { id: "ticks" }));

  for (const [name, track] of Object.entries(TICKS.tracks)) {
    const tg = el("g", { class: `ticks-${name}` }, tracks);
    completeTicks(track.ticks).forEach((t, i) => {
      let { r0, r1 } = t;
      const v = values[name]?.[i];
      if (v !== undefined) r1 = r0 + (r1 - r0) * v;            // data-driven length
      const [x0, y0] = polar(cx, cy, r0, t.a);
      const [x1, y1] = polar(cx, cy, r1, t.a);
      el("line", {
        x1: x0.toFixed(2), y1: y0.toFixed(2), x2: x1.toFixed(2), y2: y1.toFixed(2),
        stroke: t.c, "stroke-width": Math.max(0.7, t.w * widthScale).toFixed(2), "stroke-linecap": "butt",
      }, tg);
    });
  }
  return g;
}

/** Inner steps dial: black rim ring, hairline, soft vignette. Ticks come from the `rim` track above. */
export function drawInnerDial(svg) {
  const { cx, cy } = DIAL;
  const g = el("g", { id: "inner-dial" }, svg);
  const defs = el("defs", {}, g);
  const grad = el("radialGradient", { id: "innerFace", cx, cy, r: INNER.r, gradientUnits: "userSpaceOnUse" }, defs);
  [[0, "#030303"], [0.72, "#050505"], [0.93, "#0d0d0d"], [1, "#0b0b0b"]].forEach(([o, c]) => el("stop", { offset: o, "stop-color": c }, grad));
  el("circle", { cx, cy, r: INNER.r, fill: "#000" }, g);                     // black rim band
  el("circle", { cx, cy, r: INNER.ringR + 0.5, fill: "url(#innerFace)" }, g);  // face
  el("circle", { cx, cy, r: INNER.ringR, fill: "none", stroke: "var(--ring-line)", "stroke-width": 0.8 }, g);
  return g;
}
