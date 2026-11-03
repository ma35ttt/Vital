# Vital - Summary screen (reference-matched)

    npm install
    npm run serve          # http://localhost:5179  (open index.html)
    npm run diff           # screenshot -> compare with design/reference.png -> out/*.png + region table
    npm run align          # text position/size deltas vs reference
    npm run ticks          # regenerate dial tick data from the reference image

Layout: `src/tokens.css` (colours, fonts, frame) - `src/dial.js` (data-driven SVG dial) - `src/knob.js` (serrated knob, activity selector)
- `src/fab.js` (intensity button) - `src/main.js` (state, text, controls).
Interactive today: tap an activity icon (moves the needle), tap L/M/H, SHOW MORE / day / date fire `vital:*` events.
Set `window.vital.steps = 12000; ` then call `render()` from main.js exports to update numbers.
