// Visual diff loop:  node scripts/diff.mjs [--region name] [--threshold 0.12]
//
//  1. serves the app, screenshots it at the exact reference size (393x855 css px @ 660/393 dpr)
//  2. compares it with design/reference.png using pixelmatch
//  3. writes out/render.png, out/diff.png, out/side-by-side.png, out/overlay.png
//  4. prints a per-region mismatch table so you know WHAT to fix next
//
// Regions live in scripts/regions.json (reference px, relative to the cropped screen).
import { chromium } from "playwright";
import pixelmatch from "pixelmatch";
import { PNG } from "pngjs";
import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { serve } from "./serve.mjs";

const args = Object.fromEntries(process.argv.slice(2).reduce((a, v, i, all) => (v.startsWith("--") ? [...a, [v.slice(2), all[i + 1]?.startsWith("--") ? true : all[i + 1] ?? true]] : a), []));
const THRESH = parseFloat(args.threshold ?? 0.12);
const W = 660, H = 1436;                       // reference screen size in px
const CSS_W = 393, CSS_H = 855;                // frame size in css px
const DPR = W / CSS_W;

mkdirSync("out", { recursive: true });
const server = await serve(process.cwd(), 5179);
const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: CSS_W, height: CSS_H }, deviceScaleFactor: DPR });
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));
page.on("console", (m) => m.type() === "error" && errors.push(m.text()));
await page.goto("http://localhost:5179/index.html?capture");
await page.waitForSelector("html[data-ready]");
await page.evaluate(() => document.fonts.ready);
await page.waitForTimeout(150);
const shot = await page.screenshot({ clip: { x: 0, y: 0, width: CSS_W, height: CSS_H } });
await browser.close(); server.close();
if (errors.length) console.log("PAGE ERRORS:\n  " + errors.join("\n  "));

// -- normalise the screenshot to the reference pixel grid (dpr rounding can be off by a pixel)
const ref = PNG.sync.read(readFileSync("design/reference.png"));
let cur = PNG.sync.read(shot);
if (cur.width !== ref.width || cur.height !== ref.height) {
  const out = new PNG({ width: ref.width, height: ref.height });
  for (let y = 0; y < ref.height; y++) for (let x = 0; x < ref.width; x++) {
    const sx = Math.min(cur.width - 1, Math.round((x * cur.width) / ref.width)), sy = Math.min(cur.height - 1, Math.round((y * cur.height) / ref.height));
    const si = (sy * cur.width + sx) * 4, di = (y * ref.width + x) * 4;
    for (let k = 0; k < 4; k++) out.data[di + k] = cur.data[si + k];
  }
  cur = out;
}

// -- the phone's rounded screen corners show the bezel in the reference; ignore them
const R = 92;   // corner radius in ref px (55 css px)
const inCorner = (x, y) => {
  const cx = x < R ? R : x >= W - R ? W - R - 1 : null, cy = y < R ? R : y >= H - R ? H - R - 1 : null;
  return cx !== null && cy !== null && Math.hypot(x - cx, y - cy) > R - 1;
};
const mask = (img) => { for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) if (inCorner(x, y)) { const i = (y * W + x) * 4; img.data[i] = img.data[i + 1] = img.data[i + 2] = 0; img.data[i + 3] = 255; } };
mask(ref); mask(cur);

// --soft N : gaussian-blur both images (sigma N px) before scoring. The reference is a soft, upscaled
// screenshot, so a crisp render would otherwise be penalised on every edge. Default 1.0.
const SOFT = parseFloat(args.soft ?? 1.0);
function blur(img, sigma) {
  if (!sigma) return img;
  const rad = Math.ceil(sigma * 3), k = []; let sum = 0;
  for (let i = -rad; i <= rad; i++) { const v = Math.exp(-(i * i) / (2 * sigma * sigma)); k.push(v); sum += v; }
  const tmp = new Float32Array(img.data.length), out = new PNG({ width: img.width, height: img.height });
  for (let y = 0; y < img.height; y++) for (let x = 0; x < img.width; x++) for (let c = 0; c < 3; c++) {
    let acc = 0; for (let i = -rad; i <= rad; i++) acc += k[i + rad] * img.data[(y * img.width + Math.min(img.width - 1, Math.max(0, x + i))) * 4 + c];
    tmp[(y * img.width + x) * 4 + c] = acc / sum;
  }
  for (let y = 0; y < img.height; y++) for (let x = 0; x < img.width; x++) { for (let c = 0; c < 3; c++) {
    let acc = 0; for (let i = -rad; i <= rad; i++) acc += k[i + rad] * tmp[(Math.min(img.height - 1, Math.max(0, y + i)) * img.width + x) * 4 + c];
    out.data[(y * img.width + x) * 4 + c] = Math.round(acc / sum); } out.data[(y * img.width + x) * 4 + 3] = 255; }
  return out;
}
const refS = blur(ref, SOFT), curS = blur(cur, SOFT);
const diff = new PNG({ width: W, height: H });
const total = pixelmatch(refS.data, curS.data, diff.data, W, H, { threshold: THRESH, includeAA: false, diffColor: [255, 0, 255], alpha: 0.35 });
writeFileSync("out/render.png", PNG.sync.write(cur));
writeFileSync("out/diff.png", PNG.sync.write(diff));

// -- side-by-side (reference | render | diff) and 50% overlay
const sbs = new PNG({ width: W * 3 + 20, height: H });
const blit = (src, ox) => { for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) { const s = (y * W + x) * 4, d = (y * sbs.width + x + ox) * 4; for (let k = 0; k < 4; k++) sbs.data[d + k] = src.data[s + k]; } };
sbs.data.fill(30); blit(ref, 0); blit(cur, W + 10); blit(diff, 2 * W + 20);
writeFileSync("out/side-by-side.png", PNG.sync.write(sbs));
const ov = new PNG({ width: W, height: H });
for (let i = 0; i < ov.data.length; i += 4) { ov.data[i] = (ref.data[i] + cur.data[i]) >> 1; ov.data[i + 1] = (ref.data[i + 1] + cur.data[i + 1]) >> 1; ov.data[i + 2] = (ref.data[i + 2] + cur.data[i + 2]) >> 1; ov.data[i + 3] = 255; }
writeFileSync("out/overlay.png", PNG.sync.write(ov));

// -- per-region report
const regions = JSON.parse(readFileSync("scripts/regions.json", "utf8"));
const rows = [];
for (const [name, [x0, y0, x1, y1]] of Object.entries(regions)) {
  if (args.region && args.region !== name) continue;
  const w = x1 - x0, h = y1 - y0;
  const a = new PNG({ width: w, height: h }), b = new PNG({ width: w, height: h }), d = new PNG({ width: w, height: h });
  PNG.bitblt(refS, a, x0, y0, w, h, 0, 0); PNG.bitblt(curS, b, x0, y0, w, h, 0, 0);
  const n = pixelmatch(a.data, b.data, d.data, w, h, { threshold: THRESH, includeAA: false });
  rows.push({ region: name, mismatch: ((100 * n) / (w * h)).toFixed(2) + " %", px: n });
  if (args.region) { writeFileSync(`out/region-${name}.png`, PNG.sync.write((() => { const s = new PNG({ width: w * 3 + 20, height: h }); s.data.fill(30); const put = (img, ox) => { for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) { const si = (y * w + x) * 4, di = (y * s.width + x + ox) * 4; for (let k = 0; k < 4; k++) s.data[di + k] = img.data[si + k]; } }; put(a, 0); put(b, w + 10); put(d, 2 * w + 20); return s; })())); }
}
console.table(rows);
console.log(`overall mismatch: ${((100 * total) / (W * H)).toFixed(2)} %  (${total} px of ${W * H})`);
writeFileSync("out/score.json", JSON.stringify({ overallPercent: +((100 * total) / (W * H)).toFixed(3), regions: rows }, null, 1));
