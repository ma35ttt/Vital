// Coordinates in this project are measured on the reference screenshot (660 x 1436 px screen,
// origin = top-left of the phone screen) and converted to css px of the 393 x 855 frame.

export const K = 660 / 393;                 // reference px per css px
export const FRAME = { w: 393, h: 855 };
export const SCREEN_ORIGIN = { x: 99, y: 80 };   // where the screen starts inside reference-full.png

/** reference-screenshot x/y  ->  css px inside the frame */
export const rx = (x) => +((x - SCREEN_ORIGIN.x) / K).toFixed(2);
export const ry = (y) => +((y - SCREEN_ORIGIN.y) / K).toFixed(2);
/** a length measured in reference px -> css px */
export const rl = (v) => +(v / K).toFixed(2);

const NS = "http://www.w3.org/2000/svg";
export function el(tag, attrs = {}, parent) {
  const n = document.createElementNS(NS, tag);
  for (const [k, v] of Object.entries(attrs)) n.setAttribute(k, v);
  if (parent) parent.appendChild(n);
  return n;
}

export const rad = (d) => (d * Math.PI) / 180;
export const polar = (cx, cy, r, deg) => [cx + r * Math.cos(rad(deg)), cy + r * Math.sin(rad(deg))];
