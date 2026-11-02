# Shared chrome: header, sheet, glass nav, OrangeDial (measured)

Source: `meals.png`, `lift.png`, `cardio.png`, `weeks.png`, `settings-*.png`. Every frame is 393 x 855, so 1 px = 1 du.
Boxes are OUTER boxes (stroke included): `x y w h`. Right edge = x + w - 1.
Tags: MEASURED = read from pixels, BRIEF = value from REMAINING_TABS_BRIEF.md that the pixels agree with, EST = judged.
Text size rule: font size = cap height / 0.73 (Inter Tight and JetBrains Mono caps are about 0.73 em). Always `(du * scale).dp.toSp()`.
Corrections from brief section 0A are applied here and marked FIX.

## Colours (all go to Theme.kt)

| Role | Value | Tag |
|---|---|---|
| orange | `#FD3F00` | MEASURED |
| black | `#000000` | MEASURED |
| sheet top highlight (1 du line at y 404) | `#1C1C1C` | MEASURED |
| chip on orange fill / stroke | `#E83A00` / `#C93300` | MEASURED |
| well fill / stroke | `#0A0A0A` / `#1F1F1F` (stroke ~2 du, fades inward `#151515`) | MEASURED |
| text primary / muted | `#F2F2F2` / `#8A8A8A` | MEASURED |
| panelGrey | `#5F6971` | MEASURED |
| chart bar on grey (FIX) | `#171A1D` (3.1:1 on `#5F6971`; the mock `#2B2F33` is 2.4:1) | computed |

## Header row (all six frames)

| Element | Box (x y w h) | Notes |
|---|---|---|
| Left chip `SUN` | 16 50 54 32 | radius ~6, mono 700, text cap 9 -> font 12, tracking .1em; on orange `#E83A00` + stroke `#C93300`, black text; on black `chipDark` |
| Right chip `NOV.1` | 313 50 64 32 | right edge 376 (16 from screen edge). Width grows with text, min 54 |
| Title | ink centre x ~189, cap top y 61, cap height 11 | mono 700, font 15, tracking .26em. Ink centre sits left of 196.5 because of trailing tracking |

Chip and title y: chips y 50..81, title cap 61..71.

## OrangeHero top block (Meals, Lift, Cardio)

| Element | Value | Tag |
|---|---|---|
| Label (`KCAL LEFT`, `REST LEFT`, `CARDIO KCAL`) | x 17, cap top y 100, cap height 17 -> Inter Tight 600 font ~23 | MEASURED / EST |
| Value (`1,240`, `2:34`, `187`) | x 18, digit top y 133, digit height 61 -> Inter Tight 700 font ~84, tracking -.045em | MEASURED / EST |
| `SHOW MORE` button | 254 118 123 42, stroke 3 du black, radius ~6, mono 700 12 tracking .22em | MEASURED |
| Sheet | black from y 405, 1 du highlight `#1C1C1C` at y 404, top corners radius ~26, content left/right edge 16 and 376 | MEASURED |

## Glass nav (identical on all six frames)

| Element | Value | Tag |
|---|---|---|
| Container | 12 781 369 60 (bottom edge 840, 14 above screen bottom), fully rounded (r 30) | MEASURED |
| Fill / top highlight | fill ~`#1B1B1B` over black, 1 du top highlight (~`#7F7F7F` at 38%), soft fade of the highlight to orange tint near active tab | MEASURED |
| Tab items (TDY MLS LFT CRD WKS SET) | label mono 700 10, tracking .12em, inactive `#BDBDBD`; item height 46 | BRIEF |
| Active tab | orange fill, black text, outer box about 48 x 46 (fill 42 x 40 measured at y 789..828) | MEASURED / EST |
| Item centres (x) | TDY 43, MLS 93, LFT 144, `+` 196.5, CRD 249, WKS 299, SET 349 (all at y 811) | EST |
| `+` button | circle d 48 at centre (196.5, 811), black 55% fill, 1.5 stroke orange 75%, plus icon 20 du stroke 2.6 | BRIEF |
| Touch targets | each item at least 44 x 44 | BRIEF |

## OrangeDial (Meals, Lift, Cardio): geometry check

MEASURED against `meals.png`, `lift.png`, `cardio.png` (identical across the three).

| Part | Measured | Brief value | Use |
|---|---|---|---|
| Outer disc | left edge x 148, top y 164 | centre (300, 316), r 152 | brief value confirmed |
| Inner disc fill | 238 254 124 124 (r 62 fill + 3 stroke = 64 outer) | r 64 | confirmed |
| Orange pill in disc | 261 338 78 22, black mono text | 78 x 22 | confirmed |
| Top grey pill | 272 178 56 24, `#5F6971`, text `#F2F2F2` | 56 x 24 at (272,178) | confirmed |
| Highlighted letter circle | box 159 302 19 19 (Lift, number 3) -> centre (168.5, 311.5) | (168.1, 311.4) r 10 | confirmed |
| Letter circle positions | (183.5, 254), (168.1, 311.4), (179.4, 369.7) | same | use brief |
| Knob | outer box 27 229 78 78 -> centre (65.5, 267.5), outer r ~39 | centre (66, 268) r 37 + stroke | confirmed |
| FAB | box 22..78, top y 358 (partly under the sheet edge at 404) | centre (50, 386) r 28 | confirmed |
| L / M / H active circle (M) | box 79 344 19 19 -> centre (88.5, 353.5) | (88.3, 357.9) | **measured differs by 4.4 du in y: use the measured (88.5, 353.5)** |

Rules (brief 0A, FIX):
- All three letters on a dial are circles r ~9.5 to 10. The current one is orange with black text, the others black with orange text. Meals highlights P, Lift highlights the current set, Cardio highlights R.
- L / M / H and the knob are decoration only: not focusable, no gestures, no 44 du target.
- FAB keeps a visible rim: 1.5 du `#1F1F1F` stroke is invisible where the FAB overlaps the black sheet (y > 404), so add a 2 du orange-tinted or `#2A2A2A` rim around the whole circle. FIX
- Remaining dial numbers (tick ring, fan, rings, inner text) come from REMAINING_TABS_BRIEF.md section 3; they were not re-measured.
