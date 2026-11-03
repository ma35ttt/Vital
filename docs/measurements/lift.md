# LIFT (OrangeHero, nav LFT active): measured from `lift.png`

Frame 393 x 855, 1 du = 1 px. Outer boxes `x y w h`. Shared parts: `chrome.md`. Tags: MEASURED, BRIEF, EST. Font = cap / 0.73.

## Hero and dial content

| Element | Value |
|---|---|
| Label / value | `REST LEFT` / `2:34` (m:ss countdown) |
| Dial letters | 2, 3, 4 as circles. Current set (3) highlighted orange (box 159 302 19 19) |
| Inner disc | `BENCH PRESS` (mono 9 muted), `80` + `kg`, orange pill `SET 3 · ×8` |
| Top pill | `OF 3:00` |
| FAB | plus glyph, see chrome.md for the rim fix |

## Sheet

| Element | Box | Radius | Notes |
|---|---|---|---|
| Exercise title row | text x 17, cap top y 425, height 13 (cap 13 incl. descender) -> Inter Tight 600 ~17; `LAST 80 × 8` right-aligned to x 374, y 428..434, mono ~10 `#828282` | | |
| WEIGHT well | 16 454 127 54 | ~10 | label `WEIGHT KG` x 30, cap top 462, cap 7 -> mono 10 `#878787`; value `80` x 30, y 481..498 (digit height 18 -> Inter Tight 600 ~24, `#F2F2F2`) |
| REPS well | 150 454 127 54 | ~10 | label `REPS` x 164; value `8` x 164 |
| `LOG SET` button | 285 454 92 54 | ~10 | orange, label `LOG SET` mono 700 ~12 black, centred (label not measured separately) |
| Rest preset 2:00 | 16 516 115 36 | ~6 | well, label mono 700 ~12 `#E1E1E1`, y 530..538 |
| Rest preset 3:00 (selected) | 139 516 115 36 | ~6 | orange fill, black label |
| Rest preset 4:00 | 262 516 115 36 | ~6 | well |
| Plate panel | 16 560 361 159 (bottom 718) | ~8 (est) | flat `#5F6971` with inset shadow (top/left darker); header text x 26, y 570..577 |
| Session row | 16 727 361 44 (bottom 770) | ~10 | well; `SESSION · 3 SETS` x 32, y 746..752 mono ~10 `#7F7F7F`; volume `1,880` + `KG` right edge x 358, digits 18 tall at y 742..759 -> Inter Tight 700 ~20 |

Gaps: title row to wells 8; wells to presets 8; presets to panel 8; panel to session row 8.

### Plate panel internals (mock shows 100 kg numbers; build the corrected 80 kg version)

| Part | Value | Tag |
|---|---|---|
| Header | `PLATES · TARGET {weight} KG · BAR 20 KG`, mono 700 ~10, black-ish text on grey at x 26, y 570..577 | MEASURED |
| Bar line | x 16 to 376 (bleeds to both panel edges), y 652..659, h 8 (black) | MEASURED |
| Collar | box ~34 639 10 33, black (left of the plates) | EST |
| 25 kg plate | black rounded rect, x 50..66, y ~611..700 (h ~90) | EST |
| 15 kg plate | orange rounded rect, **x 68..80, y 623..687 (13 x 65)** | MEASURED |
| Result number | right edge x 361, digit height ~28 -> Inter Tight 700 ~38, `#F2F2F2` | EST |
| Result caption `KG PER SIDE · EXACT` | right edge x 361, cap ~7 -> mono 700 10 | EST |

**FIX (brief 0A.3):** the mock header says 100 kg and `25 + 15`, but the weight field says 80. The panel must follow the WEIGHT field:
- weight 80 kg, bar 20 kg: 30 kg per side = `25 + 5`. Draw one 25 kg plate (black, full height) and one 5 kg plate (orange, height scaled down).
- weight 100 kg would be `25 + 15` (the mock drawing).
- Plate height scales with plate weight; keep the 15 kg plate drawn at 65 du tall, so 25 kg = 90, 5 kg about 40 (EST scale: height = 27 + 2.5 x kg for kg 5..25; fine tune on device).
- **FIX:** leave at least 6 du between the bar line top (y 652) and the result caption, and keep the result number clear of the bar line (mock: caption ends ~y 673, bar line at 652..659 sits left of the text; on a narrow right column keep text x >= 200).

Not exact target: show nearest loadable weight and the word `NEAREST` in place of `EXACT`.

## Mock numbers (must agree after the fix)
Weight 80, reps 8, `LAST 80 × 8`, dial `80 kg`, `SET 3 · ×8`, session `1,880 KG` (3 x 80 x 8 = 1,920 does not equal 1,880: the mock volume is just a sample, derive it from sets).

## Not drawn in the mock
Empty Lift state (hero `0:00`, dial `—`, session `0 SETS`, `0 KG`), non-exact plate result, rest finished state.
