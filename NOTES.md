# Vital - Build Notes

## Version Catalog (libs.versions.toml) - Sources and Compatibility

*To be populated during Step 1 with verified versions from Google Maven, Maven Central, GitHub releases*

### Core Android/Kotlin
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| AGP (Android Gradle Plugin) | TBD | Google Maven |  |
| Kotlin | TBD | Kotlin GitHub Releases |  |
| KSP (Kotlin Symbol Processing) | TBD | KSP GitHub Releases |  |
| Compile SDK | 36 | SDK 36 installed; matches CLAUDE.md |  |
| Target SDK | 36 | SDK 36 installed; matches CLAUDE.md |  |
| Min SDK | 28 | Android 9 |  |

### AndroidX Libraries
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| Core KTX | TBD | Google Maven |  |
| Lifecycle | TBD | Google Maven |  |
| Room | TBD | Google Maven |  |
| Hilt | TBD | Dagger GitHub Releases |  |
| Hilt Worker | TBD | Google Maven |  |
| Hilt Navigation Compose | TBD | Google Maven |  |
| Compose BOM | TBD | Google Maven |  |
| Material3 | TBD | Google Maven |  |
| Activity Compose | TBD | Google Maven |  |
| ConstraintLayout Compose | TBD | Google Maven |  |
| WorkManager | TBD | Google Maven |  |

### Network & Serialization
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| OkHttp | TBD | Maven Central |  |
| Kotlinx Serialization | TBD | Maven Central |  |
| Kotlinx Coroutines | TBD | Maven Central |  |
| Kotlinx Datetime | TBD | Maven Central |  |

### ML Kit
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| ML Kit Text Recognition | TBD | Maven Central |  |

### Security
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| Argon2 (PIN hashing) | TBD | Maven Central |  |

### Testing
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| JUnit | TBD | Maven Central |  |
| JUnit Jupiter | TBD | Maven Central |  |
| Truth | TBD | Maven Central |  |
| Espresso | TBD | Google Maven |  |
| AndroidX Test | TBD | Google Maven |  |
| Robolectric | TBD | Maven Central |  |
| Turbine | TBD | Maven Central |  |

## Compatibility Notes
*To be filled during Step 1*

## Changes to Spec

2026-11-01 UI redesign (all tabs except Today and Summary). SPEC.md and CLAUDE.md were updated to match:
- One accent: orange `#FD3F00` plus black and `#5F6971`. Per-tab accents (lime, blue, teal, amber, light grey) removed. Old Safety Orange `#FF4D00` replaced by the measured `#FD3F00`.
- Theme colour picker removed from Settings. `User.themeColor` stays in the Room schema unused (avoids a schema change).
- TRUE BLACK is ON by default (background `#000000`; `#141414` when off).
- Bottom nav is a glass pill with a centre `+` and a QUICK ADD sheet. No blur library; glass drawn with translucent fills and highlights.
- Fonts: JetBrains Mono + Inter Tight (Space Grotesk dropped), after Step 2 only. Text sizes in du via `.dp.toSp()` (ignores font scale).
- Dial sizes are in du, not fractions of W/H (Step 2 decision, now also in SPEC).
- Weeks period switch: L = 7 days, M = 4 weeks, H = 12 weeks (the Step 1 placeholder had it reversed).
- Widgets stay REMOVED (2026-10-31). SPEC sections marked removed.
- compileSdk / targetSdk set to 36 in SPEC and NOTES to match CLAUDE.md (what is installed and built).
- Tabs brief cleanup (2026-11-01): build from `design/tabs/*.md`, not PNGs; dial L / M / H letters are decoration on Meals, Lift and Cardio; Lift calculator follows the WEIGHT field (80 kg = 25 + 5); Settings WIDGETS section on hold; stale widget lines removed from SPEC step 5/6 and NOTES.
- Mockup bugs to build corrected (brief 0A): Weeks W12 pill and bar 11 colour, Meals dial letter circles, Settings flame icon (n/a while widgets are on hold), Weeks P/C/F spacing, two-line row height 52 to 56 du, text at least 10 du, FAB rim, chart bar contrast 3:1.
- Two pixel references only: `Notes/docs/Summary.png`, `Notes/docs/Activity.png` (`reference.png` / `frame-*.png` are no longer references).

## Unverified Items (to be verified)
- Food kcal values vs 4×protein + 4×carbs + 9×fat (check each seed food)
- Recipe ingredient quantities and total cooked weights
- Exercise library defaultRestSec values
- OCR parsing accuracy on real treadmill displays
- AI photo analysis accuracy with free tier models
- Coach chat response quality with free tier models
- Knob drag smoothness at 60 fps on mid-range device
- Notification exact timing on Android 14+
- Inter Tight font files bundled in `res/font` (OFL); not yet added
- Glass nav look without real blur (device review)
- Header must sit at y 121.8 du from the physical screen top on the OnePlus 11R (edge-to-edge); first device measurement was 13.1 du too high
- Mock numbers in the tab designs are consistent (Meals 420+560+280 = 1,260 eaten; Cardio 6 km/h, 2%, 68 kg, 32:10 = 187 kcal)

## Open design questions (need the user's answer; defaults in brackets)

1. Widgets: the new Settings design has a WIDGETS section, but widgets were removed on 2026-10-31 and Glance needs a build-file change. [Default: widgets stay removed, WIDGETS section omitted.]
2. L/M/H letters and knob on the Meals, Lift, Cardio dial art. [Default: decorative, non-focusable, non-draggable. Real L/M/H control lives on Summary and in the Weeks hero.]
3. `SHOW MORE` next to section labels (WORKOUTS, TODAY, TRENDS, VOLUME). [No default: ask when building that tab. Hero `SHOW MORE` opens Summary.]
4. Weeks default period. [Default: M = 4 weeks, as in the mockup.]
5. Settings header while scrolling. [Default: header fixed, content scrolls beneath it with a black backing, as in the mockup.]
6. Progress and Coach Chat screens have no mockup. [Default: do not build until designs exist.]


## Manual Device Checklist (for OnePlus 11R)
- [ ] Knob drag smoothness on Summary Dial
- [ ] Rest timer chronometer notification countdown
- [ ] Daily reminder notifications fire at exact times
- [ ] Biometric auth / PIN fallback works
- [ ] Camera and OCR on real treadmill display
- [ ] AI photo analysis returns usable food data
- [ ] Coach chat streams responses token-by-token
- [ ] JSON export/import round-trip (excludes secrets)

## Build Investigation Status
*To be updated during each step*

### STEP 1 (Chrome) — Colour token aliases added (2026-11-02)

- `sheetHighlight` → aliases `sheetTopHighlight` (#1C1C1C)
- `backgroundTrueBlackOff` → aliases `falseBlackBackground` (#141414)

Both added as aliases per REMAINING_TABS_BRIEF.md section 1 / chrome.md "Colours" table. Original tokens kept for compatibility.

### Corrected 2C measurements table (2026-11-01)

The previous table here mixed numbers from the Summary SVG (vital-ui) into an "Activity.png" table and ended with a pasted prompt. It was replaced by the table below, which was measured from pixels of `Notes/docs/Activity.png`.

## 2C measurements (Activity.png, du, frame 393x855, scale 1.675 px/du)
MEASURED = read from pixels; EST = judged from the picture. All y values are from the top of the physical screen (edge-to-edge); the device status bar overlays the top.

| Element | x | y | w | h | Notes |
|---|---|---|---|---|---|
| Hero card | 13.1 | 121.8 | 365.4 | 326.6 | fill #FD3F00 (MEASURED); radius ~11 (MEASURED) |
| "MOVE" label | 29.3 | 143.3 | 57.9 | 15.5 | black text, mono, cap height 15.5 -> font ~21 (EST) |
| "Now" | 340.3 | 139.7 | 28.7 | 11.3 | colour #D33100 (MEASURED) |
| Big time "09:24" | 26.3 | 174.3 | 254.9 | 60.9 | digit height 60.9 -> font ~83 (EST), thin mono |
| Superscript "24" | 285.4 | 174.9 | 33.4 | 21.5 | digit height 21.5 -> font ~29 (EST) |
| Stat labels row | 28.1 | 262.7 | - | 11.9 | dim orange #D33100; columns at x ~28 / ~153 / ~284 (EST) |
| Stat values row | 29.9 | 283.0 | - | 16.1 | black, bold; unit (CAL/KM/BMP) smaller (EST) |
| Grey panel | 19.7 | 321.2 | 353.4 | 121.2 | ~6 du inset from card; FLAT #5F6971; radius ~6 (EST) |
| Panel inner shadow | | | | | top edge darkest ~#424143, fades to flat over ~7 du; left edge same |
| Guide line 1 | | 339.1 | | 1.8 | at 15% of panel height; darker than panel (red 74 vs 95) |
| Guide line 2 | | 381.2 | | 1.8 | at 50% of panel height |
| Bottom label row | | ~427 | | | at ~87% of panel height; "6m ... 13m" with dashes, tiny (~5-6 du, EST) |
| End dot | centre 322 | 385.4 | 6 | 6 | at 85.6% panel width, 53% panel height; polyline STOPS here |
| Tooltip pill | ~55 wide | ~19 tall | | | centred on guide line 1 and on end dot x (EST); thin outline; text 111BMP |
| Leader line | 322 | pill bottom -> dot | 1px | | thin vertical |
| Capsule rows | 13.1 | 529.0 / 626.3 / 723.6 | 366.6 | 90.7 | pitch 97.3; radius = half height (~45) |
| Capsule fill | | | | | #0D0D0D; darker outer ring ~6.5 du (#010101-#060606); light rim line at bottom edge |
| Icon circle | ~21 | | ~74 dia | | EST |
| Thumbnail | 262.7 | 537.3 | 108.7 | 73 | flat #5F6971 (matches 0.28W x 0.185W) |
| Thumb bottom ruler | | | | | tick numbers (960 / 980 / 990), active one dark, others faded; NOT "MIN AVG MAX" |

### Summary screen numbers (separate screen; unverified copy)

These were copied by Claude Code from `Notes/vital-ui/src/*.js`. The files win. Do not use them for the Activity/Today hero.

| Element | Value | Source |
|---|---|---|
| Dial radius | 147.5 du | dial.js |
| Inner dial | 115.5 du | dial.js |
| Knob radius | 34 du | knob.js |
| FAB radius | 40 du | fab.js |
| Header text, SHOW MORE, 09:24 pill | see main.js | main.js |

### M2 F3 — glass nav and QUICK ADD bottom sheet (2026-11-03)

Built from FIXES_AND_GESTURES_BRIEF section 2 and chrome.md "Glass nav".

- `GlassBottomNav.kt` rewritten: container 12..381 du, height 60, bottom 14, radius 30; fill white 16 % to 5 % vertical gradient, 1 du white 38 % top highlight, 1 du white 7 % bottom highlight, 1 du white 16 % border, black 65 % drop shadow (blur 24, y 10). Slots: three equal-weight tabs, fixed 54 du centre slot, three more. Tab item 48 x 46, radius 23, mono 700 10 tracking .12em, inactive `#BDBDBD`, active orange with black text, white 40 % inner top highlight and orange glow (alpha .45, blur 14). `+` drawn 48 du (black 55 % fill, 1.5 du orange 75 % stroke, orange plus 20 du stroke 2.6 round caps, faint orange glow) inside a 56 du tap target.
- `QuickAddSheet.kt` (new): real bottom sheet, scrim black 50 % fading 120 ms, panel slides up 220 ms, bottom edge on the nav's top edge (74 du above the screen bottom), black fill, 26 du top corners, 1 du `#1C1C1C` top highlight, `QUICK ADD` label, four 52 du well rows (8 du gap) with stroke glyphs. Back dismisses. Tapping a nav tab while open closes the sheet.
- `VitalNavigation.kt`: one `navigateToTab(index)` used by the nav, the sheet and every in-screen tab callback, so the highlight always matches the page (it did not before: in-screen navigation left the old tab highlighted).
- Device measurements (OnePlus 11R, 3.155 px/du): nav top 804.6 du, bottom 864.6, height 60; slot centres 43.3 / 94.1 / 144.7 / 196.7 / 248.6 / 299.0 / 349.9 (target 43 / 93 / 144 / 196.5 / 249 / 299 / 349); TDY pill 48.2 du; `+` circle 48.2 du. Sheet top highlight 503 du, row pitch 60 du, panel bottom 803 du, nav top 804.8 du.
- Deferred: "each row focuses the tab's primary input" (REMAINING_TABS_BRIEF section 2). Lift WEIGHT and Settings BODY WEIGHT are display-only wells today and Meals' field has no focus plumbing; wire focus when those tabs get real inputs in M4.
- NOT TESTED: visual comparison (no QUICK ADD mockup exists; the nav has no separate mockup either).

### M4 Settings 2a — data layer (2026-11-04, commit 92743cf, tag m4-settings-2a)

Split by the user so a DI crash shows up before any DI work sits on top of it.

- `SettingsViewModel.kt` (Hilt): `settings` and `user` StateFlows from `SettingsRepository` / `UserRepository`, plus `setTrueBlack`, `setAiProvider`, `setKcalTarget`, `setMacroTargets`, `setBodyWeightKg`, `setRestPresetSec`, `setNotificationsEnabled`, `setMealRemindersEnabled`, `setWorkoutRemindersEnabled`, `setWaterRemindersEnabled`, `setDisplayName`. No export/import, no API-key setter, no reminder-time setter, no biometric setter yet.
- `SettingsScreen` takes `viewModel: SettingsViewModel = hiltViewModel()`. The look did not change in 2a.
- Seeding: `Settings` and `User` rows seeded. The Settings parts of `MainViewModel.loadSampleData()` were removed; the rest of that block (Weeks/Today/Meals/Progress sample data) is still there and still flagged `// TEMP sample data`.
- `DatabaseModule` gained five unscoped `@Provides` DAO functions (`userDao`, `foodDao`, `recipeDao`, `exerciseDao`, `settingsDao`). Only the DAOs some repository reaches are bound: any other repository fails to resolve the moment something asks for it, and gets a matching `@Provides` then.

### M4 Settings 2b — screen rebuilt to settings.md (2026-11-04)

`SettingsScreen.kt` rebuilt to `design/tabs/settings.md` and the section 1 defects. Measurements below: mocks are 393x855 at 1 du = 1 px, the device is 1 du = 3.1552 px.

- **Section 1 item 1 corrected.** The hero is already at 98 du in the mock (measured 98.0 in `settings-top.png`), so it was never misplaced. The real defect was a **doubled horizontal inset**: the scaffold already pads 12 du and the screen padded 12 again. `ScreenScaffold` now owns the inset and Settings does not reapply `modifier`/`innerPadding` (KDoc says so on the signature).
- **KCAL size corrected**: settings.md said "mono 700 ~17 (EST)". Measured from the mock: 20.5 du. Built at 20.5.
- Hero number: measured cap height 55 du in the mock, consistent with the md's 76 du Inter Tight 700 tracking −0.055. Kept at 76.
- Rows per FIX 0A.6: one-line 47 du, profile 57, two-line 54 with an 8 du gap (pitch 62), label top padding 11, sub-label bottom padding 11. Gaps: hero→label 20.7, label→row 11.3, row→row 7, profile→label 17.7.
- DATA note text decoded from the mock: `AI KEYS AND PIN ARE NEVER EXPORTED OR OVERWRITTEN BY AN\nIMPORT.` at 10.5 du, line height 15 (the md's FIX warned it must not fall below 10–11 du).
- **PIN LOCK / BIOMETRIC UNLOCK draw real state but are not tappable** (user's decision). BIOMETRIC reads `User.biometricEnabled == true`, PIN LOCK reads `User.pinHash != null`; both are `VitalToggle(..., enabled = false)` until the M6 auth gate. No schema change. Drawn to the mock's toggle spec.
- ABOUT dropped, DEBUG SHOWCASE kept as the last row (user's decision). WIDGETS is not built (settings.md line 53; widgets removed 2026-10-31), so the mock's last section has no counterpart.
- **Not implemented yet** (visible as missing behaviour, not missing pixels): EXPORT JSON / IMPORT JSON (buttons drawn, no handlers), the API-key SHOW row (drawn, non-interactive), hero tap-to-edit (brief section 4.5 item 1), and the ALLOW NOTIFICATIONS tap-through to the system page (brief section 4.5 item 6).
- **Two-line rows are 54 du / pitch 62 by design, the mock has 46 / pitch 53.** Everything from ALLOW NOTIFICATIONS down therefore sits ~27–38 du lower than the mock. This is the brief's FIX 0A.6, not drift.
- The mock's final well band (689..744) is the WIDGETS tile row, which is not built; the device's equivalent band (709..752) is the DEBUG SHOWCASE row, so those two are not comparable.
- The two orange blocks in the nav band (172.7..220.6 and 325.8..373.7 du, y 810.7..858.6) are `GlassBottomNav`'s centre `+` button and the selected SET tab's orange indicator. By design; not a Settings artefact. (An earlier note suspected an OS overlay — that was wrong.)

Differences measured against the mocks, device vs mock, same classifier on both sides:

| Element | Mock | Device | Delta |
|---|---|---|---|
| Hero box | 98.0..326.0 | 97.9..326.1 | exact |
| Macro bar | 278..314 | 278..314 | exact |
| Hero number ink | 148.0..203.0 | 155.0..210.1 | **+7.0 du low** |
| KCAL ink | 190.0..203.0 | 192.1..207.0 | +2.1 low, ~1.9 taller |
| P/C/F fat value ink | 244.0..258.0 | 246.3..260.5 | +2.3 low |
| NAME row | 371.0..427.0 | 376.8..430.4 | +5.8 top |
| TRUE BLACK row | 435..491 | 440.9..491.2 | +5.9 top, +0.2 bottom |
| BODY WEIGHT / REST row | 499..555 | 501.7..555.3 | +2.7 top, +0.3 bottom |
| AI COACH segmented | 600..641 | 606..646.5 | +6.0 |
| Key row | 652..697 | 658..702.6 | +6.0 |
| Security row pitch (scrolled) | 53.0 | 52.9 | matches |
| Two-line row pitch (scrolled) | 53.0 | 61.8 | FIX 0A.6, deliberate |
| Nav top | 781 (855 du canvas) | 804.7 (878.5 du screen) | bottom-anchored, expected |

- The one unexplained number is the **hero number sitting 7.0 du low**: the mock's digits start at y 148, ours at 155, with the same cap height. The `Text` offset is y 39.1 inside the hero, i.e. 17.6 du from box top to cap top, where the mock implies ~10.7. Everything else inside the hero is only +2.1..+2.3 du low, so this is the number's own placement, not the hero's. Candidate fix: move that offset up ~7 du (39.1 → ~32). Not applied — waiting for the user's go.
- The profile and AI COACH sections sit ~3–6 du lower than the mock (rows 1–3 measured by well-band tops, which carry the same `insetShadow` bias on both sides; row bottoms, which are reliable, are within 0.3 du for TRUE BLACK and BODY WEIGHT). Cause not yet isolated.
- Data-driven text differences, all expected from the seed: BODY WEIGHT `75` vs the mock's `68`; meal reminders `08:00 · 13:00 · 19:00 · 22:00` (seeded JSON) vs the mock's `08:30 · 13:00 · 19:30`; WORKOUT `EVERY DAY · 18:00` vs `MON WED FRI · 18:00`; key row `NO KEY SET` vs the mock's masked key; PIN LOCK and BIOMETRIC OFF vs the mock's ON; WATER ON vs the mock's OFF. Name `Mrityunjay` and provider GEMINI match.
- Shared components outstanding (not changed in this step): `VitalToggle` sizes in raw dp, so 46 dp renders 43.4 du against the mock's 46 du; `SectionLabel` carries 16 dp horizontal padding; the fonts pass across the other tabs is still pending.
- NOT TESTED: visual comparison.

### M4 Settings 2b follow-up — hero offsets and the profile/AI COACH drift (2026-11-04)

Fixes the round left open at the end of the 2b entry: the hero number, the KCAL ink, the hero P/C/F values, and the cause of PROFILE / AI COACH sitting 3–6 du low.

- **Hero number** `y` 39.1 → 32.1 du. Ink moved from 155.0..210.1 to 147.7..203.8 against the mock's 148.0..203.0: **+7.0 du → −0.3 / +0.8**.
- **KCAL ink** size 20.5 → 18 du, `y` 88.3 → 87 du. Band is now 190.2..203.2, **h = 13.0, identical to the mock** (was 192.1..207.0, h 14.9: 2.1 low and 1.9 taller). This reverses the 2b call that set it to the measured 20.5; 20.5 was measuring the laid-out box, not the cap, so the glyphs overshot.
- **Hero P/C/F values** `y` 143.4 → 141.1 du: **+2.3 → −0.3**.
- **The profile/AI COACH drift was not a gap above the section.** Three causes, found by probing the row borders at x = 190 du (`26 <= max(c) <= 70`, which reads the 1.5 dp `WellRow` stroke and is immune to the `insetShadow` bias that corrupts well-band tops):
  1. **A `Text` with `lineHeight = 13` lays out ~16.7 du.** JetBrains Mono's ascent + descent is ~1.32 em, so the font's own line box wins over the smaller `lineHeight`. Every section label therefore added ~3.7 du and pushed all rows below it down. Fixed by pinning `SettingsSectionLabel` to a 13 du box (`.height(...)`) with a measured 2.5 du glyph lift. Inter Tight is ~1.211 em, so the same effect applies to Inter text.
  2. **`Alignment.CenterStart` on a top-padded Column halves its own padding** — offset = (rowHeight − contentHeight)/2 + padding/2, so the specified top padding is only half-effective and a small target offset is unreachable. `NameRow` and `SettingsTwoLineRow` now use `.align(Alignment.TopStart)`.
  3. **Row pitch was 7 du where settings.md FIX 0A.6 says 8.** Fixing (1) then overshot AI COACH to −2.9 and unmasked this. `GAP_BETWEEN_ROWS` 7 → 8, and a new `GAP_BETWEEN_CELLS = 7f` keeps the two horizontal pairs (BODY WEIGHT / REST PRESET, EXPORT / IMPORT) at the md's 7 du so they are not silently widened.
  - Nested text boxes in `SettingsTwoLineRow` (12 / 10.5 du) and `SettingsValueRow` (10 du) are pinned to their nominal heights for the same reason as (1).
- Row boxes after, device vs mock (du): NAME 371.1..427.9 (372..426), TRUE BLACK 436.1..489.7 (436..490), segmented 598.1..641.8 (599..642), key row 650.0..697.9 (651..698) — all within ~1 du. Scrolled pitches: security 53.0 (md 53), two-line 61.8 (FIX 0A.6 62).
- Remaining residuals are the app-wide small-text ink bias, all ≤ 3 du and all downward: NAME label +2.8, TRUE BLACK label +3.0, its sub +2.6, BODY/REST label +1.6, BODY value +0.9, AI COACH −1.0, PROFILE 0.0. Not chased further in this step.
- Two bands are **not comparable** and were left alone: "NAME value" (mock glyph height 5 vs device 12.7 — the mock's is masked) and the key row top (−4.6, `NO KEY SET` vs the mock's masked dots).
- Scrolled state read visually only: hero lower half, PROFILE, AI COACH, SECURITY, NOTIFICATIONS, DATA, the AI-keys note, DEBUG and DEBUG SHOWCASE all present; no missing, clipped or overlapping element.
- NOT TESTED: visual comparison.
- NOT TESTED: numeric comparison of the scrolled state against `settings-scrolled.png`.

### M4 Settings 2c + shared components (2026-11-04)

- **Key row SHOW/HIDE.** The row now reads `NO KEY SET` until a key exists, otherwise masked dots. SHOW swaps to the stored value and HIDE swaps back; the toggle resets whenever `aiApiKeyEncrypted` changes.
- **Hero tap-to-edit.** Tapping the DAILY TARGETS hero opens a kcal + protein/carbs/fat dialog. SAVE writes all four through one `SettingsViewModel.setDailyTargets(...)` Room upsert, so the hero and the day's remaining numbers move together.
- **ALLOW NOTIFICATIONS tap-through.** The row opens `Settings.ACTION_APP_NOTIFICATION_SETTINGS` with `EXTRA_APP_PACKAGE`; the toggle still writes `notificationsEnabled`.
- **Shared components converted to du.** `VitalToggle` is 46 x 26 du with a 20 du knob and 1.5 du stroke; `SectionLabel` uses 16 du horizontal padding. Today and Summary do not reference either component. Lift is the only other caller of `SectionLabel`, so its labels now scale with the design unit like the rest of the tab.
- Build, unit tests and `assembleDebug` green after the step.

### M4 Meals — Room-backed rebuild (2026-11-04)

`MealsScreen.kt` rebuilt to `docs/measurements/meals.md` and wired to Room through a new `MealsViewModel`; `MainViewModel` sample meals are no longer used.

- **Layout.** Sheet content starts at 416 du (scaffold 8 du + 3 du list padding). Method tiles 54 du, smart-text field 44 du, ambiguity pill 24 du, slot rows 42 du, all 8 du apart. Dashed empty-slot rows use a real `PathEffect` round-rect (the first pass drew the corner arcs 90 degrees out). Empty rows stack the muted slot label over the orange `+ LOG <SLOT>` line like the mock.
- **Live data.** Hero `KCAL LEFT` = target − sum of today's `MealLogEntry.kcal`; dial `EATEN` uses the same sum, so the hero, dial and slot rows agree even if a seed's kcal is not exactly 4/4/9. `OF 2,500` and the top slot pill read `Settings.kcalTarget` and the current time slot.
- **Logging.** Smart text parses the seeded Food rows (RAW/COOKED grouped, aliases normalised from names), opens the RAW OR COOKED modal on ambiguity, and snapshots the chosen variant's macros. Search rows open a grams + slot dialog. Tapping a filled slot opens its detail list with soft delete; tapping an empty slot selects TEXT and targets that slot for ADD.
- **Seed fix.** `SeedLoader.loadIfNeeded()` was never called, so the Food/Recipe/Exercise tables stayed empty. `VitalApplication` now calls it after `seedUserAndSettings()`. `DatabaseModule` gained the `MealLogDao` provider the new ViewModel needs.
- **Device check (e281c7, 1240 x 2772, density 476).** Logged `rice 150g` → RAW/COOKED dialog → COOKED: LUNCH row `White Rice 150g`, hero `2,312`, dial `EATEN 188`; restarted the app and the row/hero persisted; soft delete returned the hero to `2,500` / `EATEN 0`; no `AndroidRuntime:E`.
- **Not built yet.** WEB and RECIPE tiles select but have no flow; there is no date picker (today only); QUICK ADD and the keyboard-open state are still design-only. Search results render inline when a query matches, which the mock does not draw.

### M4 Cardio — Room-backed manual entry (2026-11-05)

`CardioScreen.kt` rebuilt to `docs/measurements/cardio.md` and wired to Room through a new `CardioViewModel`; the old `MainViewModel` cardio samples are gone.

- **Layout.** Sheet content starts at 416 du (scaffold 8 du + 11 du list padding). Type tiles 54 du, scan row 46 du, field rows 44 du at a 52 du pitch, footer/SAVE row 36 du, RECENT label and 56 du capsules, all 8 du apart. The list has 96 du bottom padding so the last capsule finishes above the glass nav.
- **Live data.** Hero `CARDIO KCAL` is today's `CardioSession.kcal` sum; RECENT shows the last three sessions across days as `TYPE · dd.MM`, distance (or duration when distance is blank) and kcal. Tapping a capsule opens a detail dialog with soft delete.
- **Estimate.** Manual entry has no kcal field, so calories always come from `Acsm.kcal` using the stored `Settings.bodyWeightKg`. A speed above 6 km/h is clamped to 6 for the estimate and the footer switches to `OUT OF RANGE · ACSM ≤ 6 KM/H`. When DISTANCE is left blank it is filled from speed × duration.
- **Bug fixed during device check.** The settings `StateFlow` was never collected, so `save()` read its 75 kg fallback while the profile stored 80 kg. `save()` now reads `Settings` with `first()`; `6.0 km/h · 2.0% · 32:10` at 80 kg gives 220 kcal.
- **Device check (e281c7, 1240 x 2772, density 476).** Saved a run (`3.22 km`, 220 kcal) and a bike session (`4 km`, 205 kcal); hero summed to 425; the type switch moved the dial highlight to B; 8.0 km/h showed OUT OF RANGE; tab switch kept the draft; force-stop + relaunch kept the sessions; soft delete returned the hero to 0; no `AndroidRuntime:E`.
- **Not built yet.** `SCAN TREADMILL` and the camera FAB are visual only; the ML Kit OCR snap flow is M7 per ROADMAP, not M4. There is no date picker (today only).
- **Not tested.** Visual comparison against `design/tabs/cardio.png` (user measures).

### M4 Summary B + 5-tab shell (2026-11-05)

The approved Summary option B (24-hour day ring) replaces the old dial Summary, and the tab bar becomes five items with a More sheet. Editorial-dark tokens are introduced alongside, but the remaining tabs still render their old designs.

- **Navigation.** `VitalNavigation.kt` now hosts `Meals · Lift · Today · Cardio · More`, with Today as the start destination and the centre item. Tab selection uses `popUpTo("today")` + `saveState`/`restoreState`; More toggles an overlay instead of a route, so its tab stays orange while open. Summary, Weeks, Settings, AI coach, Progress and Debug are detail routes reached from More (and from the old screens' callbacks). `VitalBottomNav.kt` draws the bar: 1 dp top hairline, 64 dp row, custom stroke glyphs (fork/knife, dumbbell, house, heartbeat, three dots), orange active / `navInactive` rest, `navigationBarsPadding()`.
- **More sheet.** `MoreSheet.kt` is a custom `AnimatedVisibility` overlay above the nav (not `ModalBottomSheet`): scrim + slide-up, drag handle, Done, QUICK ADD (Log meal / set / cardio / body weight) and GO TO (Summary / Weeks / Settings / AI coach / Debug showcase in debug builds). Quick-add jumps to the relevant tab (body weight opens Settings); go-to closes the sheet and pushes the route.
- **Summary B.** `SummaryScreen.kt` draws a 240 dp 24-hour ring: hour ticks (major every 6 h), a hairline circle, an outer orange lift arc per session, an inner translucent orange cardio arc, one ink dot per meal slot (first food logged sets the time), and a thin orange now marker. Centre is eaten kcal, the EATEN eyebrow, and `left`/`over`; below are the meal/lift/cardio legend, a Sessions list (`Lift`/`Cardio` leading column, title, duration or distance), the P/C/F totals grid, a verdict line and Share day. Share opens the system chooser with a plain-text day summary.
- **Real data.** New `SummaryViewModel` aggregates today's Room rows (`MealLogDao`, `WorkoutDao` + per-session sets, `CardioDao`, `SettingsDao`, `ExerciseDao`) rather than `MainViewModel` samples. Lift duration falls back to the last set time when `endedAt` is null; lift title is the most-used exercise in the session, sentence-cased. Cardio start time is inferred as `updatedAt − durationSec` because `CardioSession` stores no start time.
- **Editorial primitives.** `Editorial.kt` adds the page header (`‹ Back` + date eyebrow), eyebrow, hairline, section header, ruled row, macro grid and the 50 dp orange button; `Color.kt` adds `navInactive` and `sheet`.
- **Deviation.** Summary's back label is `‹ Back`, not the mock's `‹ More`, because Summary is also opened from Today's SHOW MORE and is not a More-only destination. The ring's cardio arc is 45% orange so it reads as the quieter series; the mock uses one orange.
- **Device check (e281c7, 1240 x 2772, density 476).** Nav renders with Today orange and the four others muted; More opens above the nav with quick-add and go-to rows; Summary showed the logged bench-press set (1 min), the 30 min / 3 km run and the chicken-breast meal (156 kcal, P 31 / C 0 / F 4, 2,344 left) with the lift and cardio arcs and the meal dot on the ring; Share day opened the chooser; no `AndroidRuntime:E`.
- **Not built yet.** There is no day picker (today only); the ring does not yet show a body-weight marker; quick-add rows jump to a tab rather than opening that tab's add flow directly.
- **Not tested.** Visual comparison against `tmp/concepts/summary-options.html` (user measures); the old `SummaryDialScreen.kt` and `GlassBottomNav.kt` are now unused but still compiled.

### Lift — editorial restyle (2026-11-05)

`LiftScreen.kt` rewritten to the approved all-tabs mock; the ViewModel and Room writes are unchanged.

- **Layout.** One ruled column: header (`Lift` + `THU 05.11`), rest hero (orange `REST` eyebrow, 72 sp timer, `set N logged · target M:SS`, 104 dp ring), exercise row (name + last set), weight/reps grid (hairline top/bottom, 1 dp centre rule), orange `Log set` button, rest presets as underlined mono tabs, plate calculator (`per side · exact/nearest` + `25 + 5 + 1.25`), and the session list (`Set N`, `weight × reps`, exercise name, `HH:mm`).
- **Rest ring.** The ring is the stored preset's countdown: `remaining / restPresetSec`, drawn clockwise from 12 o'clock with a 5 dp orange stroke over a `ruleStrong` track. It is empty when the timer is not running.
- **Presets.** The selected tab is `Settings.restPresetSec`. The ViewModel clamps its list base to 60 s, so a stored value below 60 shows `0:30 · 1:00 · 2:00` with `0:30` selected — the list and selection can therefore disagree about the base, which is why the mock's middle-tab selection is not forced.
- **Dialogs.** The exercise picker and session detail are restyled to the same system (sheet fill, hairline rows, orange filled Close) and the picker now scrolls to the selected exercise on open.
- **Deviation.** The mock's weight/reps cells show a small `kg` suffix; the value is now an inline `BasicTextField`, so the unit moved into the cell label (`Weight · kg`) to keep the number and unit tight while typing. Session rows also carry the exercise name, which the mock omits, so multi-exercise sessions stay readable.
- **Device check (e281c7, 1240 x 2772, density 476).** Logged `82.5 × 10` from the inline fields: set 3 appeared with the right time, volume moved to 2,105 kg, the plate result moved to `25 + 5 + 1.25`, the ring refilled, and force-stop + relaunch kept all three sets. The picker opened scrolled to `Bench Press` with `Selected`; no `AndroidRuntime:E`.
- **Not built yet.** No per-set edit (delete only, from the session dialog), no warm-up/RPE controls, no plate inventory settings.
- **Not tested.** Visual comparison against `tmp/concepts/all-tabs.html` (user measures).

### Today — editorial restyle (2026-11-05)

`TodayScreen.kt` rewritten to the approved all-tabs mock and backed by a new `TodayViewModel`; the `MainViewModel` Today samples are no longer used.

- **Layout.** Header (`Today` + `THU 05.11`), kcal-left hero (orange eyebrow, 72 sp value, `of 2,500 · 6% eaten`, 104 dp ring), three-cell macro grid, `This week` line chart with day labels and weekly kcal total, and `Workouts` rows (`Lift`/`Cardio`, title, duration or distance).
- **Live data.** `TodayViewModel` combines today's meals, workout sessions + their sets, cardio and settings with the trailing seven days of meals. Lift duration uses `endedAt`, else the last set time, so an open session still has a length; lift title is the most-used exercise. The week total and the chart use meal kcal only.
- **Ring.** The 104 dp ring is eaten / target, drawn clockwise from 12 o'clock (5 dp orange over a `ruleStrong` track).
- **Chart.** The trailing week is a muted polyline; only the final segment into today is orange, with an orange dot at today. All-zero weeks draw a flat line.
- **Navigation.** Tapping a Lift row opens the Lift tab and a Cardio row opens Cardio. The old `SHOW MORE` button is gone because Summary now lives in the More sheet; Today's unused navigation callbacks stay in the signature so `VitalNavigation` did not need to change.
- **Device check (e281c7, 1240 x 2772, density 476).** Today showed `2,344` left, `6%` eaten, P 31 / C 0 / F 4, a week line ending at today, and the two real sessions (`Bench press 16 min`, `Run 3 km`); tapping the Lift row opened Lift with the three logged sets intact; no `AndroidRuntime:E`.
- **Not built yet.** The week chart has no scrub/tooltip and no week/month switch; the header has no day picker (today only).
- **Not tested.** Visual comparison against `tmp/concepts/all-tabs.html` (user measures).

### Cardio — editorial restyle (2026-11-05)

`CardioScreen.kt` rewritten to the approved all-tabs mock; `CardioViewModel` is unchanged apart from a new `todayCount` flow for the hero meta.

- **Layout.** Header (`Cardio` + `THU 05.11`), number-only hero (`CARDIO KCAL`, 72 sp value, plain hairline, `today` / `N sessions`), underlined type tabs, a 2 x 2 ruled entry grid (Duration, Distance · km, Speed · km/h, Incline · %), orange `Log session`, `Recent` rows (type, kcal, `dd.MM`) and the `Estimate · ACSM` note between hairlines. No gauge, target or scan row.
- **Type tabs.** The mock draws four (Run / Bike / Walk / Other). The app keeps all eight `CardioType` values in a horizontally scrolling tab row so Rowing, Swimming, Elliptical and Stairs stay reachable; the first six are visible at 416 dp.
- **Recent.** Rows show the three latest sessions across days. The trailing count is `3 shown`, not the mock's `2 today`, because the list is not day-scoped; the hero's `N sessions` is today-only.
- **Estimate.** The note switches to orange `Above 6 km/h · the estimate is clamped.` when the typed speed exceeds the ACSM limit; otherwise it reads `Valid up to 6 km/h · above that the estimate is clamped.`
- **Dialogs.** The session detail is restyled to the editorial sheet (hairline detail rows, orange outline Delete, orange filled Close).
- **Device check (e281c7, 1240 x 2772, density 476).** Logged `30:00 · 5 km · 6 km/h · 1%`: the hero moved from 162 to 346 kcal, the meta to `2 sessions`, the form cleared, and `Run 184 kcal 05.11` appeared above the earlier run and walk; the detail dialog showed duration, distance, speed, incline and the ACSM kcal; no `AndroidRuntime:E`.
- **Not built yet.** `SCAN TREADMILL` is gone from the main screen (OCR is M7); no date picker (today only); no edit, only soft delete.
- **Not tested.** Visual comparison against `tmp/concepts/all-tabs.html` (user measures).

### Meals — editorial restyle (2026-11-05)

`MealsScreen.kt` rewritten to the approved all-tabs mock; the parser, search and Room writes in `MealsViewModel` are unchanged.

- **Layout.** Header (`Meals` + `THU 05.11`), eaten hero (orange `EATEN`, 72 sp value, orange progress hairline, `of 2,500` / `N left`), underlined method tabs (Search / Text / Web / Recipe), the smart-text row with an orange `Add`, an orange ambiguity hint, SEARCH result rows (name, kcal / 100 g), `Meals` slot rows (Breakfast / Lunch / Dinner / Snacks; filled ones show the foods and kcal, empty ones an orange `+ Log <slot>`), and a `Today` macro block (value, target percent).
- **Progress.** The hairline under the hero fills orange with eaten / target.
- **Dialogs.** Raw/Cooked, food grams + slot, and slot detail are restyled to the editorial sheet (hairline rows, orange outline Cancel, orange filled Save/Close, orange Delete).
- **Deviation.** The mock's floating `+` FAB is replaced by the inline `Add` on the smart-text row, so there is no separately-opened add sheet. Empty slot rows open the Text method pre-targeted at that slot rather than the mock's add sheet.
- **Device check (e281c7, 1240 x 2772, density 476).** Typed `chicken breast 200g` in Text and confirmed Cooked: Breakfast gained a 200 g chicken breast and eaten moved 156 → 313. Search `rice` listed raw/cooked White Rice; the food dialog logged 100 g into Lunch (126 kcal). The Breakfast detail listed both entries and Delete removed one, dropping eaten 438 → 282 and protein 65 → 34 g. No `AndroidRuntime:E`.
- **Still sample-only.** WEB and RECIPE tabs are labels only, as before.
- **Not built yet.** No date picker (today only), no per-entry edit (delete only), the query is not cleared after a smart-text add.
- **Not tested.** Visual comparison against `tmp/concepts/all-tabs.html` (user measures).

### Weeks — editorial restyle (2026-11-05)

`WeeksScreen.kt` rewritten to the approved all-tabs mock; the sample data in `MainViewModel` and the `WeeksPeriod` enum are unchanged.

- **Layout.** Header (`Weeks` + `THU 05.11`, `‹ Back`), an adherence hero (orange `ADHERENCE`, 72 sp percent, orange rule at the adherence level, `12 weeks` / `77 of 84 days`), the 12W / 4W / 7D underlined period tabs, a three-cell protein / carbs / fat percentage grid, `Adherence by week` bars with this-week orange and tick labels, and `Trends` rows (body weight, volume, bench PR) with sparklines.
- **Periods.** The period now drives the day count (`84`, `28`, `7`), the number of bars and the tick labels (`W-11 … THIS`, `W-3 … THIS`, `D-6 … TODAY`). Macro cells and the hero keep using the trailing sample values.
- **Bars.** Heights are min-max stretched into a 45–100% band so small adherence differences stay visible; the first value is the shortest and the current week is the tallest and orange.
- **Trend values.** The normalized sample series are mapped to plausible units: body weight `80 − 1.5·v` kg, volume `10·v` t, PR `80 + 20·v` kg, so the mock's `79.4 kg` reproduces.
- **Navigation.** `WeeksScreen` gained an optional `onBack` (wired to `popBackStack` in `VitalNavigation`); it defaults to a no-op so the existing call sites still compile.
- **Device check (e281c7, 1240 x 2772, density 476).** Rendered `92%`, `77 of 84 days`, the 12 bars with `THIS` orange, and the three trends; switching to 7D moved the meta to `6 of 7 days`, redrew seven bars and the `D-6 … TODAY` labels; Back returned to Today; no `AndroidRuntime:E`.
- **Still sample-only.** Weeks does not read Room; the numbers do not change with real logs.
- **Not built yet.** No tooltip on the bars, no tap-through from a trend to its detail.
- **Not tested.** Visual comparison against `tmp/concepts/all-tabs.html` (user measures).
