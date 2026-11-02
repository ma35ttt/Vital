# SETTINGS (BlackPage, nav SET active): measured from `settings-top.png` and `settings-scrolled.png`

Two states of one scrolling column. Frame 393 x 855, 1 du = 1 px. Outer boxes `x y w h`. Header and nav: `chrome.md` (BlackPage).
The header (chips + title, y 50..82) is fixed; content scrolls beneath it with a black backing. The nav is fixed (bottom 14, height 60), so give the column bottom padding of at least 100 du.
Tags: MEASURED, BRIEF, EST. Font = cap / 0.73. Check: 29% + 40% + 31% = 100%; 180 x 4 + 250 x 4 + 85 x 9 = 2,485 kcal (target 2,500; mock only).

Content left/right edge is 12 and 380 (369 wide), not 16.

## Top state (`settings-top.png`, scroll offset 0)

| Element | Box / value | Tag |
|---|---|---|
| Daily-targets hero | 12 98 369 228 (bottom 325), orange, radius ~18 | MEASURED |
| `DAILY TARGETS` | x 27, y 121..128 (cap 8 -> mono 700 ~11), black | MEASURED |
| `2,500` | x 26, digits start y ~147, Inter Tight 700 ~76 (brief), black | BRIEF / EST |
| `KCAL` | right of the number at x ~230, mono 700 ~17 | EST |
| P / C / F labels | x 27 / 141 / 254, y 222..229 (cap 8 -> mono 10), black 70% | MEASURED |
| P / C / F values | x 27 / 141 / 253, y 244..263 (cap/digit 14 + unit -> Inter Tight 700 ~20): `180 g` `250 g` `85 g` | MEASURED |
| Macro-share bar | outer 24 278 345 36 (grey `#5F6971`, inset shadow, radius ~6); text mono 700 ~11: `P 29%` at x 36, `C 40%` centred, `F 31%` right-aligned to x 357 | MEASURED / BRIEF |
| Section label `PROFILE` | x 17, y 348..357 (cap 10 -> mono 700 ~13, tracking .24em, `#ECECEC`) | MEASURED |
| NAME row | 12 371 369 57 (bottom 427), radius ~10, well; label `NAME` x 28, y 382..388, mono ~10 `#7C7C7C`; value `Mrityunjay` x 28, y 400..419 (cap 14 -> Inter Tight 600 ~19); edit icon 14 x 14 at x 348..361, y 393..406, `#8A8A8A` | MEASURED |
| TRUE BLACK row | 12 435 369 57 (bottom 491), well; label x 28, y 449..455; sub `PURE #000 FOR AMOLED` x 28, y 469..476, mono ~11 `#D9D9D9`; toggle box 319 450 46 26 (orange fill measured 320 451 45 25) | MEASURED |
| BODY WEIGHT well | 12 499 181 57 (bottom 555), radius ~10; label x 26, y 510..516; value `68` x 26, y 529..543 (digits 15 -> Inter Tight 700 ~20) + `KG` mono ~10 muted | MEASURED |
| REST PRESET well | 200 499 181 57; label x 215; value `3:00` + `MIN` | MEASURED |
| Section label `AI COACH` | x 16, y 575..584 | MEASURED |
| Segmented control | 12 599 369 44 (bottom 642), radius ~10, well; selected `GEMINI` orange 18 605 176 32 (6 inset); `OPENROUTER` label `#7F7F7F` mono 700 ~11 centred at x ~285; selected label black | MEASURED |
| Key row | 12 651 369 48 (bottom 698), well; masked key text x 30..202, y 670..680 mono ~13 `#E2E2E2`; `SHOW` orange mono 700 ~10 right edge x 363, y 671..678 | MEASURED |

Gaps in the top state: hero to PROFILE label 22; label to first row ~14; rows 8 apart (371 -> 435 is pitch 64 for 57 high rows, so gap 7); key row bottom 698 to nav top 781 leaves 83 du of empty space (more sections follow when scrolling).

## Scrolled state (`settings-scrolled.png`)

The first visible label is `SECURITY` at y 104..113. The scroll offset is not measurable from the two frames (no shared element), so build the column in this order: DAILY TARGETS, PROFILE, AI COACH, SECURITY, NOTIFICATIONS, DATA (WIDGETS is in the mock but on hold).

| Element | Box / value | Tag |
|---|---|---|
| Section label `SECURITY` | x 17, y 104..113 | MEASURED |
| PIN LOCK row | 12 126 369 47 (bottom 172), well, radius ~10; label `PIN LOCK` x 28, y 145..153 (cap 9 -> mono 700 ~12, `#EBEBEB`); toggle on at 319 136 46 26 (fill 320 137 45 25) | MEASURED |
| BIOMETRIC UNLOCK row | 12 179 369 47 | MEASURED |
| CHANGE PIN row | 12 232 369 47 (bottom 278); chevron 6 x 11 at x 354..359, y 250..260 `#8A8A8A` | MEASURED |
| Section label `NOTIFICATIONS` | x 17, y 297..306 | MEASURED |
| ALLOW NOTIFICATIONS | 12 319 369 46 (bottom 364); label x 28, y 329..337; sub `PERMISSION GRANTED` x 28, y 350..356 (cap 7 -> mono ~10, `#868686`); toggle at fill 320 329 45 26 | MEASURED |
| MEAL REMINDERS | 12 372 369 46; sub `08:30 · 13:00 · 19:30` | MEASURED |
| WORKOUT | 12 425 369 46; sub `MON WED FRI · 18:00` | MEASURED |
| WATER | 12 478 369 46; sub `EVERY 2 H`; toggle OFF: fill `#1A1A1A`, knob `#555`, box ~321 489 43 24 | MEASURED |
| Section label `DATA` | x 17, y 543..552 | MEASURED |
| `EXPORT JSON` | 12 564 181 47 (orange fill measured 12 565 180 45), label mono 700 ~12 black, ink x 53..145 | MEASURED |
| `IMPORT JSON` | 200 564 181 47, transparent fill, orange stroke ~2, orange label (ink x 243..337, y 583..591) | MEASURED |
| Note (2 lines) | x 17, y 622..628 and 637..643, cap 7 -> mono ~9, `#818181`. **FIX: mock is ~9 du; use 10 to 11 du** | MEASURED |
| Section label `WIDGETS` | x 16, y 666..675 | MEASURED |
| Widget tiles | 12 688 118 58, 137 688 119 58, 263 688 118 58 (well, radius ~11, orange icon + mono 700 ~9 label) | MEASURED |

**WIDGETS section: do not build** (widgets removed 2026-10-31, brief 0A). If widgets return, the CALORIES icon is a flame (FIX 0A.4); the mock draws a clock.

### Two-line rows (FIX 0A.6)
Mock two-line rows are 46 high (notifications) and 47 (security uses one line), 57 high for the profile rows. The sub-label sits ~5 du from the bottom edge. Build all two-line rows (ALLOW NOTIFICATIONS, MEAL REMINDERS, WORKOUT, WATER) at **54 du** high, 8 du gap (pitch 62), label top padding 11, sub-label bottom padding 11. Profile rows keep 57. One-line rows (PIN LOCK, BIOMETRIC UNLOCK, CHANGE PIN) stay 47.
Sub-label text: at least 10 du (mock cap 7 = ~9.6 du font; round up to 10).

## Toggle (all rows)
Box 46 x 26 (fill 45 x 25 measured), fully rounded. On: orange fill, black knob d ~20 at right with 3 du inset (knob ink x 343..361 in the first row). Off: fill `#1A1A1A`, 1.5 stroke `#1F1F1F`, knob `#555555` at left. Touch target: row.

## True Black off
Not drawn in the mock. Specified: screen background `#141414` instead of `#000000`; wells stay `#0A0A0A`, which would read darker than the background, so use `#0A0A0A` unchanged and verify on device.

## Not drawn in the mock
Name edit mode, key SHOW state (unmasked), keyboard open, permission denied state, TRUE BLACK off.
