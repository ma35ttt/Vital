import { el, rx, ry, rl } from "./geometry.js";
import { ICONS } from "./icons.js";

// Measured on a 3x crop of the reference whose origin is reference px (100, 1260).
const c = (x, y) => [rx(100 + x / 3), ry(1260 + y / 3)];
const cl = (v) => rl(v / 3);

/** Bottom-left activity button: teardrop (one square corner) + L / M / H intensity bracket. */
export function drawFab(svg, { intensity = "M", icon = "walk", onIntensity } = {}) {
  const g = el("g", { id: "fab" }, svg);
  const [x0, y0] = c(65, 215);
  const size = cl(375);

  // soft shadow
  const defs = el("defs", {}, g);
  const f = el("filter", { id: "fabShadow", x: "-40%", y: "-40%", width: "190%", height: "190%" }, defs);
  el("feGaussianBlur", { stdDeviation: 7 }, f);
  const sh = el("path", { d: teardrop(x0 + 3, y0 + 10, size), fill: "rgba(120,20,0,.55)", filter: "url(#fabShadow)" }, g);
  sh.setAttribute("opacity", ".75");

  el("path", { d: teardrop(x0, y0, size), fill: "var(--ink)" }, g);

  // corner bracket
  const [bx, by] = c(350, 241), [bx2] = c(411, 241), [, by2] = c(410, 302);
  el("path", { d: `M${bx},${by}H${bx2 + 1.15}M${bx2},${by - 1.15}V${by2}`, stroke: "var(--orange)", "stroke-width": 2.3, fill: "none", "stroke-linecap": "butt" }, g);

  // glyph
  const [ix, iy] = c(260, 400);
  el("path", { d: ICONS[icon], fill: "var(--orange)", transform: `translate(${ix},${iy}) scale(${40 / 24 * 1.13}) translate(-12,-12)` }, g);

  // L / M / H leaders
  const lead = el("g", { fill: "none", stroke: "var(--ink)", "stroke-width": 2.1, "stroke-linecap": "butt" }, g);
  const P = (pts) => pts.map(([x, y], i) => (i ? "L" : "M") + c(x, y).join(",")).join("");
  el("path", { d: `M${c(250, 128)}L${c(250, 170)}Q${c(250, 180)} ${c(272, 181)}C${c(320, 185)} ${c(358, 198)} ${c(388, 217)}` }, lead);
  el("path", { d: P([[441, 214], [459, 196]]) }, lead);
  el("path", { d: `M${c(437, 232)}C${c(458, 262)} ${c(468, 330)} ${c(480, 399)}L${c(526, 400)}` }, lead);

  const labels = { L: c(249, 70), M: c(516, 142), H: c(587, 405) };
  Object.entries(labels).forEach(([k, [x, y]]) => {
    const t = el("text", { x, y: y + 6.9, "text-anchor": "middle", "font-family": "var(--font-sans)", "font-weight": 500, "font-size": 13.2, fill: "var(--ink)", "data-intensity": k, style: "cursor:pointer" }, g);
    t.textContent = k;
    t.addEventListener("click", () => onIntensity?.(k));
  });
  return {};
}

/** 74px blob with a square top-right corner. */
function teardrop(x, y, s) {
  const r = s / 2;
  return `M${x + r},${y}H${x + s}V${y + r}A${r},${r} 0 0 1 ${x + r},${y + s}A${r},${r} 0 0 1 ${x},${y + r}A${r},${r} 0 0 1 ${x + r},${y}Z`;
}
