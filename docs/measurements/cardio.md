# CARDIO (OrangeHero, nav CRD active): measured from `cardio.png`

Frame 393 x 855, 1 du = 1 px. Outer boxes `x y w h`. Shared parts: `chrome.md`. Tags: MEASURED, BRIEF, EST. Font = cap / 0.73.
Check: 6.0 km/h x 32:10 (0.5361 h) = 3.22 km. ACSM at 6 km/h, 2%, 68 kg, 32:10 = 187 kcal (brief check value).

## Hero and dial content

| Element | Value |
|---|---|
| Label / value | `CARDIO KCAL` (x 17, cap 17) / `187` (digit height 61) |
| Dial letters | R, B, W as circles, R (run) highlighted. L / M / H stay as decorative art under the knob (not a second control) |
| Inner disc | `SPEED` (mono 9 muted), `6.0` + `km/h`, orange pill `INCL 2.0%` |
| Top pill | `32:10` (duration) |
| FAB | **camera** glyph (opens scan flow); rim fix in chrome.md. Mock FAB sits at (50, 386) like the other tabs |

## Sheet (y 405 to 776)

| Element | Box | Radius | Notes |
|---|---|---|---|
| Type tile RUN (selected) | 16 416 85 54 | ~12 | orange fill, black icon, `RUN` |
| Type tiles BIKE / WALK / OTHER | 108 416 85 54 / 200 416 85 54 / 293 416 84 54 | ~12 | well; icon orange; label mono 700 10 `#D5D5D5` at cap top y 453 |
| Scan row | 16 478 361 46 | ~10 | well; camera icon in a 30 x 30 icon well at x 25 (icon 17 x 14 at x 33..49, y 494..507); `SCAN TREADMILL` Inter Tight 700 ~14 `#F1F1F1` at x 70..193, cap 11; `ON-DEVICE OCR` mono ~10 `#858585` right edge x 358 |
| DURATION field | 16 532 177 44 | ~10 | well; label x 30, cap top 538, cap 7 -> mono 500 10 `#858585`; value `32:10` x 30, y 557..567 (cap 11 -> Inter Tight / mono ~15) |
| DISTANCE field | 200 532 177 44 | ~10 | label x 215; value `3.22 KM` |
| SPEED field | 16 584 177 44 | ~10 | value `6.0 KM/H` (digit height 14 incl. units) |
| INCLINE field | 200 584 177 44 | ~10 | value `2.0 %` |
| Footer note | x 17, y 651..659 (cap 9), orange mono 700 | | `ACSM EST · VALID ≤ 6 KM/H`. **FIX: mock is ~9 du; use 11 du** |
| `SAVE` button | 297 637 80 36 | ~8 | orange fill, black `SAVE` mono 700 ~12 |
| `RECENT` label | x 17, y 686..692 (cap 7), mono 700 ~10, `#7A7A7A` | | section label; measured smaller than the 13 du `SectionLabel` on other screens; build as 10 to 11 muted, per the mock |
| Recent capsule | 16 702 361 56 | 28 (full) | well |

Gaps: tiles to scan row 8; scan row to fields 8; field rows 8 apart (pitch 52); fields to footer row ~12.

### Recent capsule internals (top T = 702)

| Part | Value | Tag |
|---|---|---|
| Icon well | d ~34, icon orange (run) at x 41..51, y 722..736 | MEASURED / EST |
| Label `RUNNING · 30.10` | x 78, cap top 714, cap 7 -> mono 10 `#838383` | MEASURED |
| Value `5.2 km` + `342 KCAL` | x 78..191, digits 14 tall at y 732..745 -> Inter Tight 700 ~19 (`5.2 km`), `342 KCAL` mono ~10 muted baseline-aligned | MEASURED |
| Sparkline panel | grey flat region 279 715 86 35; outer about 272 708 96 44 | MEASURED / EST |

## Empty state (brief: zero, not fake)
Hero `0`, dial `SPEED —`, fields empty with muted placeholders, no RECENT rows.

## Behaviour hooks
Speed above 6 km/h: the footer note becomes a visible warning (keep orange, add an icon or the word `OUT OF RANGE`). Not drawn in the mock.
