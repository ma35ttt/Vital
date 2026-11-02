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
