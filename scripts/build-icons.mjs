#!/usr/bin/env node
// Ring Grid app icon: generates every native icon asset from one geometry definition.
//
//   npm i -D sharp png-to-ico
//   node build-icons.mjs [outDir] [--accent=#FF4D00] [--bg=#000000]
//
// Output (default ./icons):
//   master/   icon.svg (full-bleed), icon-circular.svg (circular-safe), icon-squircle.svg, icon-1024.png, icon-squircle-1024.png, foreground-1024.png
//   web/      favicon.svg, favicon.ico, favicon-16/32/48.png, apple-touch-icon.png, icon-192/512.png, maskable-512.png
//   ios/      AppIcon.appiconset (1024 single-size, no alpha)
//   macos/    icon.iconset + icon.icns (icns only when run on macOS), icon-1024.png
//   android/  mipmap-*/ic_launcher(.png|_round|_foreground), mipmap-anydpi-v26, values/, playstore-512.png

import sharp from 'sharp';
import { mkdir, writeFile } from 'node:fs/promises';
import { execFileSync } from 'node:child_process';
import path from 'node:path';

const args = process.argv.slice(2);
const flag = (name, fallback) => args.find((a) => a.startsWith(`--${name}=`))?.split('=')[1] ?? fallback;
const outDir = path.resolve(args.find((a) => !a.startsWith('--')) ?? 'icons');
const ACCENT = flag('accent', '#FF4D00');
const BG = flag('bg', '#000000');

// ---- geometry (1024 x 1024 design space) -----------------------------------
const CENTERS = [302, 512, 722]; // 3 x 3 grid, 210 apart
const OUTER = 106; // neighbours overlap by 2px, so the rings merge into one lattice
const HOLES = [92, 70, 46]; // left -> right: thin, medium, heavy
const SQUIRCLE_RADIUS = 225; // about 22% of 1024

const cells = CENTERS.flatMap((cy) => CENTERS.map((cx, col) => ({ cx, cy, col, hole: HOLES[col] })));
const circle = (cx, cy, r, extra = '') => `<circle cx="${cx}" cy="${cy}" r="${r}" ${extra}/>`;

// safe: artwork scale around the canvas centre so the design fits a circular
// mask. 1 = full-bleed. Android adaptive icons show only the centre 66% of the
// foreground layer (circle radius ~341 of 512 in design space), so the
// foreground uses 0.72; legacy round icons show the full inscribed circle
// (radius 512), so 0.85 keeps artwork + drop shadow inside with margin.
function iconSvg({ ground = 'square', safe = 1 } = {}) {
  const groundEl =
    ground === 'square'
      ? `<rect width="1024" height="1024" fill="${BG}"/>`
      : ground === 'squircle'
        ? `<rect width="1024" height="1024" rx="${SQUIRCLE_RADIUS}" ry="${SQUIRCLE_RADIUS}" fill="${BG}"/>`
        : '';
  const holes = cells.map((c) => circle(c.cx, c.cy, c.hole, 'fill="#000"')).join('');
  const outer = cells.map((c) => circle(c.cx, c.cy, OUTER)).join('');
  const shade = (col, opacity) =>
    `<g opacity="${opacity}">${cells.filter((c) => c.col === col).map((c) => circle(c.cx, c.cy, c.hole)).join('')}</g>`;
  const artwork = `<g filter="url(#ds)"><g mask="url(#shape)" fill="${ACCENT}">${outer}</g></g>
<g mask="url(#shape)"><g filter="url(#soft)" fill="none" stroke="#000" stroke-width="24">${shade(1, 0.12)}${shade(2, 0.28)}</g></g>`;

  return `<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
<defs>
<filter id="ds" x="-20%" y="-20%" width="140%" height="150%" color-interpolation-filters="sRGB">
<feGaussianBlur in="SourceAlpha" stdDeviation="16"/>
<feOffset dy="16" result="b"/>
<feFlood flood-color="#5a1c00" flood-opacity="0.6"/>
<feComposite in2="b" operator="in" result="s"/>
<feMerge><feMergeNode in="s"/><feMergeNode in="SourceGraphic"/></feMerge>
</filter>
<filter id="soft" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="7"/></filter>
<mask id="shape" maskUnits="userSpaceOnUse" x="0" y="0" width="1024" height="1024">
<rect width="1024" height="1024" fill="#fff"/>${holes}
</mask>
</defs>
${groundEl}
${safe === 1 ? artwork : `<g transform="translate(512 512) scale(${safe}) translate(-512 -512)">${artwork}</g>`}
</svg>
`;
}

// ---- helpers ---------------------------------------------------------------
const write = async (rel, data) => {
  const file = path.join(outDir, rel);
  await mkdir(path.dirname(file), { recursive: true });
  await writeFile(file, data);
  return file;
};
const raster = (svg) => sharp(Buffer.from(svg), { density: 72 }).resize(1024, 1024).png().toBuffer();
const scaled = (buf, size) => sharp(buf).resize(size, size, { kernel: 'lanczos3' }).png().toBuffer();
const opaque = (buf, size) =>
  sharp(buf).resize(size, size, { kernel: 'lanczos3' }).flatten({ background: BG }).removeAlpha().png().toBuffer();
const round = (buf, size) =>
  sharp(buf)
    .resize(size, size, { kernel: 'lanczos3' })
    .composite([
      { input: Buffer.from(`<svg width="${size}" height="${size}"><circle cx="${size / 2}" cy="${size / 2}" r="${size / 2}"/></svg>`), blend: 'dest-in' },
    ])
    .png()
    .toBuffer();

// ---- masters ---------------------------------------------------------------
const svgSquare = iconSvg({ ground: 'square' });
const svgSquircle = iconSvg({ ground: 'squircle' });
// Circular-safe variants (launcher uses circular icon masks)
const svgSquareSafe = iconSvg({ ground: 'square', safe: 0.85 });
const svgForeground = iconSvg({ ground: 'none', safe: 0.72 });

const square = await raster(svgSquare); // full-bleed: iOS, square legacy Android, store listings (the OS applies its own mask)
const squareSafe = await raster(svgSquareSafe); // artwork scaled to fit a circular mask
const squircle = await raster(svgSquircle); // pre-rounded: favicon, store listings
const foreground = await raster(svgForeground); // rings only, scaled into the adaptive-icon safe circle: Android foreground layer

await write('master/icon.svg', svgSquare);
await write('master/icon-circular.svg', svgSquareSafe);
await write('master/icon-squircle.svg', svgSquircle);
await write('master/foreground.svg', svgForeground);
await write('master/icon-1024.png', await opaque(square, 1024));
await write('master/icon-squircle-1024.png', squircle);
await write('master/foreground-1024.png', foreground);

// ---- web / PWA -------------------------------------------------------------
await write('web/favicon.svg', svgSquircle);
const fav = {};
for (const s of [16, 32, 48]) fav[s] = await write(`web/favicon-${s}.png`, await scaled(squircle, s));
await write('web/apple-touch-icon.png', await opaque(square, 180)); // iOS Safari needs an opaque square
await write('web/icon-192.png', await scaled(squircle, 192));
await write('web/icon-512.png', await scaled(squircle, 512));
await write('web/maskable-512.png', await opaque(squareSafe, 512)); // artwork scaled inside the 80% maskable safe zone
try {
  const { default: pngToIco } = await import('png-to-ico');
  await write('web/favicon.ico', await pngToIco([fav[16], fav[32], fav[48]]));
} catch {
  console.warn('! png-to-ico not installed, skipped favicon.ico (npm i -D png-to-ico)');
}

// ---- iOS -------------------------------------------------------------------
await write('ios/AppIcon.appiconset/AppIcon-1024.png', await opaque(square, 1024)); // no alpha, App Store rule
await write(
  'ios/AppIcon.appiconset/Contents.json',
  JSON.stringify(
    { images: [{ filename: 'AppIcon-1024.png', idiom: 'universal', platform: 'ios', size: '1024x1024' }], info: { author: 'xcode', version: 1 } },
    null,
    2,
  ),
);

// ---- macOS (Big Sur+ template: 824px squircle centred on a transparent 1024 canvas) ----
const macMaster = await sharp({ create: { width: 1024, height: 1024, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } } })
  .composite([{ input: await scaled(squircle, 824), left: 100, top: 100 }])
  .png()
  .toBuffer();
await write('macos/icon-1024.png', macMaster);
const iconset = [
  ['icon_16x16.png', 16], ['icon_16x16@2x.png', 32], ['icon_32x32.png', 32], ['icon_32x32@2x.png', 64],
  ['icon_128x128.png', 128], ['icon_128x128@2x.png', 256], ['icon_256x256.png', 256], ['icon_256x256@2x.png', 512],
  ['icon_512x512.png', 512], ['icon_512x512@2x.png', 1024],
];
for (const [name, size] of iconset) await write(`macos/icon.iconset/${name}`, await scaled(macMaster, size));
if (process.platform === 'darwin') {
  execFileSync('iconutil', ['-c', 'icns', path.join(outDir, 'macos/icon.iconset'), '-o', path.join(outDir, 'macos/icon.icns')]);
} else {
  console.warn('! not on macOS, skipped icon.icns (run again on a Mac, or use: npx png2icons / electron-icon-builder)');
}

// ---- Android ---------------------------------------------------------------
const densities = { mdpi: [48, 108], hdpi: [72, 162], xhdpi: [96, 216], xxhdpi: [144, 324], xxxhdpi: [192, 432] };
for (const [d, [launcher, fg]] of Object.entries(densities)) {
  await write(`android/mipmap-${d}/ic_launcher.png`, await opaque(square, launcher));
  await write(`android/mipmap-${d}/ic_launcher_round.png`, await round(squareSafe, launcher));
  await write(`android/mipmap-${d}/ic_launcher_foreground.png`, await scaled(foreground, fg));
}
await write(
  'android/mipmap-anydpi-v26/ic_launcher.xml',
  `<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
`,
);
await write(
  'android/values/ic_launcher_background.xml',
  `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">${BG}</color>
</resources>
`,
);
await write('android/playstore-512.png', await opaque(square, 512));

console.log(`Icons written to ${outDir}`);
