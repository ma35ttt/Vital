# WEEKS (BlackPage, nav WKS active): measured from `weeks.png`

Frame 393 x 855, 1 du = 1 px. Outer boxes `x y w h`. Header and nav: `chrome.md` (BlackPage: chips `chipDark`, title `#F2F2F2`, no orange block).
Tags: MEASURED, BRIEF, EST. Font = cap / 0.73.
Check: (97 + 88 + 91) / 3 = 92.

## Hero card

| Element | Box / value | Tag |
|---|---|---|
| Card | 12 98 369 326 (bottom 423), orange, radius ~18 (brief; pixel est ~12-18) | MEASURED / BRIEF |
| Tag `ADHERENCE · 4 WEEKS` | x 26, y 121..128 (cap 8 -> mono 700 ~11), black | MEASURED |
| L / M / H switch | outer box ~272 113 95 31, dark-orange inset capsule; letters at y 124..131 (x 287, ~318, 346); active M = black pill r ~6 behind an orange `M` | MEASURED / EST |
| Giant number `92` | x 30, digit top y 150, digit height 69 -> JetBrains Mono 300 ~94 (brief 92), tracking -.06em; ink x 30..145 | MEASURED |
| `%` superscript | x ~128..147, y ~152..178, mono 300 ~36 | EST |
| P / C / F labels | x 28 / 141 / 254, y 248..255 (cap 8 -> mono 10, 70% black), texts `PROTEIN` `CARBS` `FAT` | MEASURED |
| P / C / F values | x 28 / 141 / 253, top y ~270, height ~14 -> Inter Tight 20; `97%` `88%` `91%` | MEASURED |
| Chart panel | 24 290 345 122 (bottom 411), grey `#5F6971`, inset shadow top/left, radius ~6 | MEASURED |

**FIX (0A.5):** the value row ends at y ~285 and the panel starts at y 290 (5 du gap). Move the label row to y 238..245 and the value row up so its bottom is at y <= 281 (>= 8 du above the panel top 290); giant number may shrink 2 du or the card may grow 4 du. Do not move the panel.

### Chart (12 bars)

| Bar | x | height | Colour |
|---|---|---|---|
| 1 | 38 | 58 | `#171A1D` (FIX; mock `#2B2F33`) |
| 2 | 66 | 72 | same |
| 3 | 94 | 50 | same |
| 4 | 122 | 80 | same |
| 5 | 150 | 66 | same |
| 6 | 178 | 88 | same |
| 7 | 206 | 70 | same |
| 8 | 234 | 84 | same |
| 9 | 262 | 76 | same |
| 10 | 290 | 82 | same |
| 11 | 318 | 80 | **FIX: same grey** (mock is pure `#000`, reads as a stray highlight) |
| 12 (W12, current) | 345 | 98 outer | orange `#FD3F00` fill with 2 du black stroke: outer 345 303 17 98, fill 347 305 14 94 |

All bars: width 16 (bar 12 is 17 outer), radius ~3, bottom edge y 399 (pixel 400), pitch 28 (bar 12 starts 27 after bar 11). Orange bar is the only bar taller than its slot (top y 303, others top >= 312).
Bar contrast: `#2B2F33` on `#5F6971` is 2.4:1; `#171A1D` is 3.1:1 (passes 3:1). **FIX (0A.8)**

Tooltip pill `W12 · 96%`: black, outer box 252 296 81 22 (r 11), text `#F2F2F2` mono 700 ~11 (centred in pill).
**FIX (0A.1):** the mock pill sits over bars 9 to 10 (x 252..333), but W12 is the orange bar at the far right. Centring the 81 wide pill on bar 12 (centre x 353.5) would run past the panel edge (369), so: pill box 260 294 81 22 (right edge 340, bottom 315, clear of the bar 10 top at y 317 and bar 11 top at 320), plus a 1 du black leader line from the pill's right edge (340, 305) to bar 12's left edge (345, 305). EST positions; keep the pill inside the panel and never over a grey bar.

## TRENDS

| Element | Box / value | Tag |
|---|---|---|
| Section label `TRENDS` | x 16, y 458..468 (cap 11 -> mono 700 ~15 incl tracking .24em; text `#F1F1F1`) | MEASURED |
| `SHOW MORE` (dark) | 254 442 123 42, stroke `#1F1F1F`, fill `#0A0A0A`, text mono 700 12 at x 272..357, y 459..467 | MEASURED |
| Capsule row 1 BODY WEIGHT | 12 496 369 68 (bottom 563), radius 34 | MEASURED |
| Capsule row 2 WEEKLY VOLUME | 12 572 369 68 | MEASURED |
| Capsule row 3 BENCH PR | 12 648 369 68 | MEASURED |

Row pitch 76, height 68, gap 8, width 369 (left/right edge 12, not 16). Last row bottom y 715; nav top 781.

### Capsule internals (top T)

| Part | Value | Tag |
|---|---|---|
| Icon well | d 40 at x 24, y T + 14; stroke icon ~15 x 15 orange at x 34..48 (y T + 26..40) | MEASURED / BRIEF |
| Label (`BODY WEIGHT`) | x 74, cap top T + 16, cap 7 -> mono 10 `#868686` | MEASURED |
| Value | x 74, digits top T + 34, digit height ~17 -> Inter Tight 700 ~23, `#F2F2F2`; secondary text (`−0.6 · 4W`, `PEAK 21.2`, `+5 KG`) mono ~10 `#8A8A8A` baseline-aligned after the value | MEASURED |
| Sparkline panel | outer ~272 T+12 96 44 (grey region measured 279..368 x T+17..T+55 area incl. inset); radius ~6 | MEASURED / EST |
| Sparkline marks | polyline `#000`/dark, end dot r 3 on rows 1 and 3; row 2 is 6 bars | EST |

Row values (mock): `68.4 kg` / `18.4 t` + `PEAK 21.2` / `100 × 5` + `+5 KG`.

## Not drawn in the mock
Weeks in L (7 bars) and H (12 weeks view), empty history (no bars, `—`), loading.
