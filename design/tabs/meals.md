# MEALS (OrangeHero, nav MLS active): measured from `meals.png`

Frame 393 x 855, 1 du = 1 px. Outer boxes `x y w h`. Shared header, hero block, sheet, nav and dial: see `chrome.md`.
Tags: MEASURED, BRIEF, EST. Font size = cap height / 0.73. Mock numbers: 420 + 560 + 280 = 1,260 eaten; 2,500 - 1,260 = 1,240 left (checked).

## Hero and dial content

| Element | Value |
|---|---|
| Label | `KCAL LEFT` |
| Value | `1,240` (target minus eaten) |
| Dial letters | P, C, F. **FIX: circles r ~10 like Lift and Cardio, P highlighted (orange circle, black text).** The mock draws plain orange letters with no circles |
| Inner disc | small label `EATEN` (mono 9, `#8A8A8A`), value `1,260`, orange pill `OF 2,500` |
| Top pill | current slot, `LUNCH` |
| FAB | plus glyph |

## Sheet (y 405 to 776, left 16, right 376, gap 8)

| Element | Box | Radius | Fill / stroke | Text |
|---|---|---|---|---|
| Method tile SEARCH (selected) | 16 416 85 54 | ~12 | orange `#FD3F00`, black icon | `SEARCH` mono 700, cap 7 -> font 9.5 -> use 10, black |
| Method tile TEXT | 108 416 85 54 | ~12 | well `#0A0A0A`, stroke `#1F1F1F` | `TEXT`, orange icon, label `#D5D5D5`/orange per brief |
| Method tile WEB | 200 416 85 54 | ~12 | well | `WEB` |
| Method tile RECIPE | 293 416 84 54 | ~12 | well | `RECIPE` |
| Tile icon | ~15 x 15 at tile centre x, y 427..441 | | stroke 1.8, orange | |
| Tile label | cap top y 453, cap 7 | | | centred under icon |
| Smart-text field | 16 478 361 44 (**brief said 48; measured 44**) | ~10 | well | text x 32, cap ~10 (`chicken breast cooked 200g, rice 150g`), Inter Tight ~14, `#D3D3D3` |
| `ADD` button inside field | 317 485 51 30 (right inset 8, top inset 7) | ~8 | orange | `ADD` mono 700 ~11, black |
| Ambiguity pill | 16 530 214 24 | 12 (full) | transparent, orange stroke ~1.5 | `RICE 150G · RAW OR COOKED?` mono 700 ~10, orange, text x 33..213 |
| Slot row BREAKFAST | 16 562 361 42 | 21 (full) | well | see below |
| Slot row LUNCH | 16 612 361 42 | 21 | well | |
| Slot row DINNER (empty) | 16 662 361 42 | 21 | **dashed** stroke `#1F1F1F`-ish, no fill | `DINNER` label muted, `+ LOG DINNER` orange |
| Slot row SNACK | 16 712 361 42 | 21 | well | |

Row pitch 50, height 42, gap 8. Last row bottom y 753; the nav starts at 781 (28 du clear).

### Slot row internals (row top = T)

| Part | Value | Tag |
|---|---|---|
| Time-letter circle | d 30 at x 23, y T + 6 (Dinner measured 23 668 31 30); fill `#121212`, stroke `#232323`; letter orange mono 700 ~11 (x 36..40, 8 tall) | MEASURED |
| Label `BREAKFAST · 08:10` | x 64, cap top T + 8, cap 7 -> font 10, mono, `#828282`, tracking ~.1em | MEASURED |
| Item summary | x 64, cap top T + 25, cap/height ~10 -> Inter Tight 500 ~13, `#D8D8D8`, one line, ellipsised before the kcal block (the Lunch text is cut at x ~280) | MEASURED |
| kcal value `420` | right edge x ~331 for value, digits 14 tall at T + 14 -> Inter Tight 700 ~19; `#EDEDED` | MEASURED |
| unit `KCAL` | mono ~10, `#8A8A8A`, right edge x ~358, baseline-aligned with value | MEASURED / EST |

Value + unit block spans x 294..358.

## Behaviour hooks (not visual)
Pill only appears when the parser hits an ambiguous RAW/COOKED food; tapping opens the RAW OR COOKED modal. Empty slot = dashed row with `+ LOG <SLOT>`. Row heights stay 42 when there is no data.

## Not drawn in the mock (to design or default)
QUICK ADD sheet, RAW OR COOKED modal, empty Meals state (all four slots dashed, hero `2,500` or target, dial `EATEN 0`), keyboard-open state.
