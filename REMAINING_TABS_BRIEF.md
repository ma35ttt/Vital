# Brief: build the five remaining tabs (Meals, Lift, Cardio, Weeks, Settings) in the Vital style

Read `CLAUDE.md`, `SPEC.md` and `NOTES.md` first. Everything in them still applies: du units, `Theme.kt` as the only place for hex
literals, `Modifier.insetShadow()` for every inset surface, run `assembleDebug` after every step.

This brief replaces the earlier one. The old per-tab accent colours (lime, blue, teal, amber) are gone. **The app has two colours: orange
`#FD3F00` and jet black `#000000`**, plus the neutral grey data panel `#5F6971` from the Summary and Activity references. Nothing else.

These five screens have no pixel reference. The target is a design mockup, so this is a build-from-design job, not a pixel-diff job.
Do not invent a diff loop and do not claim a match. After each screen write `NOT TESTED: visual comparison` and stop. I review the
screenshot myself.

## 0A. Corrections and decisions (2026-11-01). These override anything below.

**Bugs in the mockup PNGs: build the corrected version, not the PNG.**
1. Weeks chart: the `W12 · 96%` tooltip points at the orange W12 bar (the far-right bar), and bar 11 is the same grey as bars 1 to 10 (not black).
2. Meals dial: P, C, F use the same letter circles as Lift and Cardio, with P highlighted (orange circle, black text).
3. Lift: the plate-calculator header and result follow the WEIGHT field (80 kg gives `25 + 5` per side). Mock numbers must agree across the screen.
4. Settings: the CALORIES widget tile and any calories icon use a flame, not a clock.
5. Weeks hero: leave at least 8 du between the P / C / F values and the grey panel.
6. Settings rows with two lines are 52 to 56 du tall; all small text is at least 10 du.
7. The FAB keeps a visible rim where it overlaps the sheet edge.
8. Grey chart bars on the grey panel must reach 3:1 contrast (lighten the bars or darken the panel).

**Decisions (defaults; the user may overrule).**
- L / M / H letters and the knob on the Meals, Lift and Cardio dial art are decorative, non-focusable and non-draggable. This replaces "real controls" in section 3 and removes the 44 du touch-target conflict. The real L / M / H control is on Summary and in the Weeks hero.
- Hero `SHOW MORE` opens the Summary screen. `SHOW MORE` next to section labels is not specified: ask before building it.
- Weeks default period is M (4 weeks). L = 7 days, M = 4 weeks, H = 12 weeks.
- Header (chips and title) is fixed on every tab; content scrolls beneath it with a black backing.
- Widgets are removed (CLAUDE.md, 2026-10-31). Omit the Settings WIDGETS section (section 4.5, item 8) until the user re-enables widgets. Do not add Glance or any dependency.
- Progress and Coach Chat have no mockups and are not part of this brief.
- Text sizes: `(du * scale).dp.toSp()` so they ignore system font scale. Never `.sp` with a du value.
- Build from `design/tabs/<screen>.md` numbers. If the file for a screen is missing, stop and ask.
- Open questions are tracked in NOTES.md.

---

## 0. Setup

1. I will add mockup exports to `design/tabs/`: `meals.png`, `lift.png`, `cardio.png`, `weeks.png`, `settings-top.png`, `settings-scrolled.png`.
   Each is a 393 x 855 frame, so 1 px in the mockup = 1 du. If a file is missing, stop and ask for it.
2. Build from `design/tabs/<screen>.md` (measured numbers), not from the PNGs: you cannot reliably view images. Where this brief and a `.md` disagree, the `.md` wins on look and this brief wins on behaviour. Section 0A corrections beat the PNGs and the `.md` tables if a table still carries a mockup bug.
3. Reuse what exists: `LocalDesign`/`du`, `VitalColors`, `VitalType`, `insetShadow`, `HeroLineGraph`, `Sparkline`, the Today and Summary hero code.
   Add new tokens to `Theme.kt` only, never inline.
4. Do not touch Today, Summary, auth, build files or the version catalog. If something needs a new dependency, stop and ask.

## 1. Tokens (add to `Theme.kt`)

| Token | Value | Use |
|---|---|---|
| `orange` | `#FD3F00` | hero fill, active states, accents |
| `black` | `#000000` | screen background, sheets, text on orange |
| `chipOnOrange` | `#E83A00`, stroke `#C93300` | header chips on an orange screen |
| `chipDark` | `#0C0C0C`, stroke `#1E1E1E` | header chips on a black screen |
| `well` | `#0A0A0A`, 1.5 du stroke `#1F1F1F` | rows, fields, tiles on black |
| `panelGrey` | `#5F6971` | data panels (use `insetShadow`, top/left `#424143`, soft light bottom/right) |
| `textPrimary` | `#F2F2F2` | text on black and on grey panels |
| `textMuted` | `#8A8A8A` | small labels |
| `tickGrey` | `#3D3D3D` | dial tick ring |
| `iconWell` | `#121212`, stroke `#232323` | round icon wells in capsule rows |

Fonts: Inter Tight 500/600/700 for numerals and names; JetBrains Mono 300 to 700 for labels, tags and the huge Weeks number.
If Inter Tight is not bundled yet, add it to `res/font` (it is OFL); this is not a dependency change.
Text sizes use `du.toSp()` and ignore system font scale, like the rest of the art.

## 2. Shared chrome (build once, `ui/components/ScreenScaffold.kt`)

`ScreenScaffold(variant, title, selectedTab, content)` where `variant` is `OrangeHero` (Meals, Lift, Cardio) or `BlackPage` (Weeks, Settings).

| Element | Value (du) |
|---|---|
| Background | `OrangeHero`: orange top, black sheet from y 404 with 26 radius top corners and a 1 du top highlight `#1C1C1C`. `BlackPage`: black. |
| Header row | top 50, left/right 16, height 32. Do not draw a fake status bar. |
| Header chips (`SUN`, `NOV.1`) | min width 54, height 32, radius 6, mono 700 12, tracking .1em. On orange: `chipOnOrange` with black text. On black: `chipDark` with `#F2F2F2` text. Inner top shadow. |
| Title | mono 700 15, tracking .26em, centred. Black on orange, `#F2F2F2` on black. |
| Hero label + value (OrangeHero) | label Inter Tight 600 22 at (16, 98); value Inter Tight 700 80, tracking -.045em, 6 below; both black |
| SHOW MORE button | right 16, top 118, height 42, 3 du stroke, radius 6, mono 700 12, tracking .22em. Black stroke on orange, `#1F1F1F` stroke on black (BlackPage, fill `#0A0A0A`). |
| Sheet body | left/right 16, top 416 to bottom 84, column, gap 8 |
| Black page body | left/right 12, from top 98 |

### Bottom nav: liquid glass with a centre +

6 tabs plus one centre `+` action: `TDY MLS LFT [+] CRD WKS SET`.

| Part | Value |
|---|---|
| Container | left/right 12, bottom 14, height 60, padding 6, fully rounded (30), z above content |
| Fill | vertical gradient white 16% to white 5% |
| Border | 1 du, white 16% |
| Highlights | inner top edge white 38% (1 du), inner bottom edge white 7%, soft inner glow white 5%, drop shadow black 65%, blur 24, y 10 |
| Tab item | height 46, fully rounded, mono 700 10, tracking .12em, inactive `#BDBDBD` |
| Active tab | orange fill, black text, inner top highlight white 40%, orange glow (alpha .45, blur 14) |
| `+` button | 48 circle, 3 du margin each side, fill black 55%, 1.5 du stroke orange 75%, orange plus icon 20 du stroke 2.6, faint orange glow |

Real backdrop blur is not available as a plain Compose modifier. **Do not add a blur library.** Build the glass with the translucent fill,
border and highlights above, which already reads as glass over the orange and black content. If you think a real blur is worth it, write
that in `NOTES.md` as a question and wait. Minimum touch target for every item is 44 du even where the drawn shape is smaller.

`+` behaviour: opens a small bottom sheet "QUICK ADD" with four rows: LOG MEAL, LOG SET, LOG CARDIO, LOG BODY WEIGHT. Each routes to the
matching tab and focuses its primary input. Only the sheet and routing are in scope here, the inputs already exist or arrive with their tab.
This is my proposal; if `SPEC.md` says something different about `+`, follow SPEC and say so in `NOTES.md`.

### Shared components

`VitalChip`, `HeroTag`, `SectionLabel` (mono 700 13, tracking .24em), `WellRow`, `CapsuleRow` (68 tall, full radius, `well`, round icon
well 40, two-line text, optional 96 x 44 grey sparkline panel on the right), `VitalToggle` (46 x 26, off = `#1A1A1A` with `#555` knob, on =
orange with black knob, real `Switch` semantics drawn custom), `GreyPanel`, `OrangeDial` (section 3).

## 3. The dial (Meals, Lift, Cardio hero art)

One composable `OrangeDial`, drawn in a Canvas over a 393 x 470 area at the top of the screen. All coordinates in du.

- Outer disc: centre (300, 316), radius 152, black, 1.5 stroke `#1A1A1A`. It is deliberately clipped by the right edge of the screen.
- Tick ring: radius 135, stroke 14, `tickGrey`, dash pattern on 1.3 / off 5.71.
- Thin ring: radius 100, 1.5 stroke `#141414`.
- Accent fan: 13 lines starting at radius 78 from the centre, angles 140 to 212 degrees in steps of 6, length growing 5 to 50 (end radius 83 to 128),
  stroke width 1.2 to 3.6, alpha .35 to 1, round caps, orange.
- Three letter circles (radius 10) at (183.5, 254), (168.1, 311.4), (179.4, 369.7), mono 700 11. The highlighted one is orange with black text,
  the others black with orange text.
- Inner disc: radius 64, fill `#0A0A0A`, 3 stroke `#222`, plus a radius 56 ring stroke 6 `#2B2B2B`, dash 0.9 / 4.6.
- Inner text, centred at x 300: small label mono 9 `#8A8A8A` tracking 1.6 at y 293; value Inter Tight 700 34 at y 329 (unit 14 to 16, `#8A8A8A`);
  orange pill 78 x 22 radius 11 at y 338 with black mono 700 9.5 text.
- Top pill: grey `panelGrey` 56 x 24 radius 8 at (272, 178), `#F2F2F2` mono 500 11.
- Knob: centre (66, 268), radius 37, orange, 3.2 black stroke dashed 2.6 / 2 (scalloped edge), inner ring radius 31 black 28%, black dot radius 7 at
  (60, 264), black pointer line from (80, 279) to (89, 287), width 5, round caps.
- L / M / H letters on a small arc under the knob at (67.1, 343), (88.3, 357.9), (99.2, 381.3). Active one sits on a black circle (radius 10) with orange text,
  inactive ones are black text on orange. M is active on Meals, Lift and Cardio. They are decorative only: non-focusable, no click handler, no 44 du touch target (see 0A).
  On Cardio the dial shows R / B / W; the L / M / H letters stay as art under the knob.
- FAB at (50, 386), radius 28, black, 1.5 stroke `#1F1F1F`, orange glyph (plus on Meals and Lift, camera on Cardio).

This is the same visual language as the Summary reference. If the Summary screen already contains the dial, knob or fan code, reuse it and
parametrise instead of copying.

## 4. Screens

All numbers below are mock values from the mockup. Every real value comes from `UiState`, fed by Room. Nothing hard-coded in composables.
Empty tables must render a real empty state, not fake data.

### 4.1 MEALS (`OrangeHero`, nav MLS active)
- Hero: label `KCAL LEFT`, value remaining kcal (target minus eaten), `SHOW MORE` outlined. Dial: letters P, C, F (P highlighted), inner `EATEN / 1,260`,
  pill `OF 2,500`, top pill = current meal slot (`LUNCH`).
- Sheet, top to bottom:
  1. Four method tiles (54 high, radius 12, `well`, orange stroke icon + mono 700 9 label): SEARCH (selected = orange fill, black icon), TEXT, WEB, RECIPE.
  2. Smart-text field (48 high, well) with an orange `ADD` button inside. Under it, when the parser hits an ambiguous RAW/COOKED food,
     an outlined orange pill `RICE 150G · RAW OR COOKED?`. Tapping it opens the modal from SPEC section 4. Never guess.
  3. Four meal-slot capsule rows filling the rest: time-letter circle, `SLOT · hh:mm` label, item summary on one ellipsised line, kcal right.
     An empty slot is a dashed capsule with `+ LOG DINNER` in orange.
- Behaviour (SPEC 4.3): local autocomplete, text parser, Open Food Facts search with a visible offline/empty state, recipe batch splitter.
  Macros are snapshotted on `MealLogEntry`. Logging a meal updates remaining kcal here, on Today and on the Dial.

### 4.2 LIFT (`OrangeHero`, nav LFT active)
- Hero: label `REST LEFT`, value `m:ss` countdown. Dial: letters 2, 3, 4 (current set highlighted), inner `BENCH PRESS / 80 kg`, pill `SET 3 · ×8`, top pill `OF 3:00`.
- Sheet: exercise title row (name Inter Tight 600 17, right `LAST 80 × 8`); WEIGHT and REPS steppers (54 high wells) plus `LOG SET` (92 wide, orange);
  three rest presets 2:00 / 3:00 / 4:00 (selected = orange fill); grey plate-calc panel (flex): barbell drawn in Canvas (bar 8 high, collar, plates as rounded rects,
  25 kg black and 15 kg orange, height scales with weight), result and header both follow the WEIGHT field (mock: 80 kg gives `25 + 5` big, `KG PER SIDE · EXACT`, header `PLATES · TARGET 80 KG · BAR 20 KG`; 100 kg would give `25 + 15`).
  If not exact, show the nearest loadable weight and the word `NEAREST`. Last row: `SESSION · 3 SETS` and volume in kg.
- Behaviour (SPEC 4.4, 5, 7): logging a set pre-fills the next from history and starts the rest timer from the exercise `defaultRestSec`. Wire the UI to the
  existing foreground service and chronometer notification, do not re-implement them. The bar weight is user-changeable. The L/M/H letters on the dial
  are not used on this screen beyond decoration; make them inert and mark them non-focusable.

### 4.3 CARDIO (`OrangeHero`, nav CRD active)
- Hero: label `CARDIO KCAL`, value kcal. Dial: letters R, B, W (R = run highlighted), inner `SPEED / 6.0 km/h`, pill `INCL 2.0%`, top pill = duration. FAB is a camera that opens the scan flow.
- Sheet: four type tiles RUN / BIKE / WALK / OTHER (selected = orange fill); `SCAN TREADMILL · ON-DEVICE OCR` row (46 high well); 2 x 2 editable fields
  DURATION / DISTANCE / SPEED / INCLINE (44 high wells, mono 500 10 muted label over Inter Tight 14 value); footer `ACSM EST · VALID ≤ 6 KM/H` in orange with a `SAVE` button;
  `RECENT` label and up to three capsule rows (icon well, `TYPE · dd.MM`, distance and kcal, grey sparkline panel).
- Behaviour (SPEC 4.5): ML Kit text recognition on the scan. Unreadable calories use the ACSM walking equation and show the `ACSM EST` tag; when speed is above 6 the
  note becomes a visible warning. Before a scan the fields are empty and the hero shows zero, not fake numbers.
  Check value: 6 km/h, 2% incline, 68 kg, 32:10 gives 187 kcal.

### 4.4 WEEKS (`BlackPage`, nav WKS active)
- Orange hero card (left/right 12, top 98, height 326, radius 18): tag `ADHERENCE · 4 WEEKS`, L / M / H switch top right (a small orange inset capsule, active = black fill with orange letter),
  giant adherence number (JetBrains Mono 300, 92, tracking -.06em) with a small `%` superscript, P / C / F percentages row (label mono 10 at 70% opacity, value Inter Tight 20),
  grey bar-chart panel (122 high, 12 bars, last bar orange with black stroke, black tooltip pill `W12 · 96%`).
- Period follows SPEC: **L = 7 days, M = 4 weeks, H = 12 weeks**. The Step 1 placeholder had it reversed; fix it and note it in `NOTES.md`.
- `TRENDS` section label with a dark `SHOW MORE` button, then three capsule rows: BODY WEIGHT (value, delta over the period, sparkline), WEEKLY VOLUME (t, peak, bars),
  BENCH PR (weight x reps, change, sparkline). Orange stroke icons.
- All series come from Room (`MealLogEntry`, `WorkoutSet`, `BodyMetrics`) and are passed to the charts as `List<Float>`. Only `@Preview` has sample data.

### 4.5 SETTINGS (`BlackPage`, nav SET active), one scrolling column
The nav stays fixed; the column scrolls beneath it with enough bottom padding that nothing hides behind the nav at the end of the scroll.
1. Orange hero `DAILY TARGETS`: kcal target Inter Tight 700 76 plus `KCAL`, P / C / F grams, grey 36 high macro-share bar (`P 29% C 40% F 31%`). Tapping a value edits it.
2. `PROFILE`: NAME row with edit icon; `TRUE BLACK` row with toggle (on by default, `PURE #000 FOR AMOLED`). There is **no theme colour picker**: the app only has orange and black.
   If TRUE BLACK is off, the screen background is `#141414` instead of `#000`.
3. Two wells side by side: BODY WEIGHT (kg) and REST PRESET (m:ss).
4. `AI COACH`: segmented GEMINI / OPENROUTER (selected = orange fill) and a masked key row with `SHOW`. The key goes to encrypted storage only.
5. `SECURITY`: PIN LOCK toggle, BIOMETRIC UNLOCK toggle, CHANGE PIN row.
6. `NOTIFICATIONS`: ALLOW NOTIFICATIONS (shows permission state, links to the system page), MEAL REMINDERS (times), WORKOUT (days and time), WATER (interval). Each row has a toggle.
7. `DATA`: `EXPORT JSON` (orange fill) and `IMPORT JSON` (orange outline), with the note that AI keys and PIN are never exported or overwritten by an import.
8. `WIDGETS`: ON HOLD, do not build (widgets removed, see 0A). Mockup shows three tiles CALORIES / WATER / WORKOUT; skip them and the Glance dependency.
Values persist to `Settings`; targets feed Meals, Today and Weeks.

## 5. Rules

1. Colours only from `Theme.kt`; sizes in du; text from `UiState` or string resources.
2. Icons are stroke icons (1.8 du, round caps) converted to `ImageVector`. No Material icons. Remove the large placeholder camera and plus icons that Step 1 left on Meals and Lift.
3. Screens survive rotation and tab switches with state kept (SPEC Step 3 check) and never crash on empty Room tables.
4. Anything below the fold scrolls and never sits under the nav at the end.
5. No `// TODO`, no stubs. If something cannot be finished, write it in `NOTES.md`.
6. Existing tests and `./gradlew assembleDebug` must pass before every "done".

## 6. Order of work (stop after each and wait for "go")

1. Tokens, shared chrome (header, chips, glass nav with `+`, QUICK ADD sheet, `OrangeDial`) and the five placeholders replaced by empty-state scaffolds. Screenshot all five.
2. Settings.
3. Meals.
4. Lift.
5. Cardio.
6. Weeks.

Each step: build, install on the OnePlus 11R, check logcat, `adb exec-out screencap -p > tmp/<screen>.png`, write `NOT TESTED: visual comparison`,
and list anything you could not reproduce from the mockup.

## 7. Done means

- All six tabs open with the shared header, the glass nav and live Room data where SPEC says so.
- Orange and black only. No other accent colour anywhere.
- Each screen matches its measured table (plus the 0A corrections) in layout, hierarchy and spacing on the device. I decide that, not you.
- No Step 1 placeholder look remains (no giant icons, no clipped cards, no overlapping text).
- `NOTES.md` lists every deviation from the mockups and every unverified item.

## Paste-ready prompt

> Read CLAUDE.md, then REMAINING_TABS_BRIEF.md, then SPEC.md sections 3 and 4, then NOTES.md. Do not read Notes/old. You cannot reliably view images: build from `design/tabs/<screen>.md`; if a file is missing, stop and tell me. Stay in manual mode. Do step 1 only: add the tokens, build the shared chrome
> (header, chips, the glass nav with the centre `+` and its QUICK ADD sheet, `OrangeDial`), swap the five placeholder tabs onto it as empty states, build and install,
> take one screenshot per tab into `tmp/`, write "NOT TESTED: visual comparison", and stop and wait for "go".
