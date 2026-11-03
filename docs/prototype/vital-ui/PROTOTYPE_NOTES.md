# UI fidelity workflow (read before touching any visual code)

The target is `design/reference.png` (the phone screen cropped from `design/reference-full.png`).
Frame = iPhone 14 Pro screen, **393 x 855 css px**, rendered at dpr 660/393 so 1 css px = 1.679 reference px.

Rules
1. Never eyeball. Measure on the reference (subpixel edge fits, bbox of ink, colour sampling) and put the number in
   `src/tokens.css` or the component. Coordinates pasted from the reference go through `rx/ry/rl` in `src/geometry.js`.
2. Dial ticks are DATA: `python3 -I scripts/extract_ticks.py` regenerates `src/tick-data.js` from the reference. Do not hand-edit it.
3. After EVERY visual change run `node scripts/diff.mjs`. It writes `out/side-by-side.png`, `out/overlay.png`, `out/diff.png`
   and prints a per-region mismatch table (regions in `scripts/regions.json`). Look at the images, then fix the worst region.
4. Use `python3 -I scripts/align.py` for text (reports dx/dy/dw/dh in reference px) and
   `python3 -I scripts/peek.py x0 y0 x1 y1 [scale]` to zoom any region (reference | render | diff).
5. Work one component at a time; re-run the diff; do not move on while a region regresses.
6. Real values live in `state` (src/main.js) - bind data there, never hard-code text in the art.

Known limits: fonts are best-guess matches (Inter Tight + JetBrains Mono); the reference is a soft, upscaled screenshot,
so scoring blurs both images (`--soft`, default 1.0). Swap in the real fonts and re-run the diff.
