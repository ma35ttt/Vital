# Vital - Build Notes

## Version Catalog (libs.versions.toml) - Sources and Compatibility

*To be populated during Step 1 with verified versions from Google Maven, Maven Central, GitHub releases*

### Core Android/Kotlin
| Component | Version | Source | Verified |
|-----------|---------|--------|----------|
| AGP (Android Gradle Plugin) | TBD | Google Maven |  |
| Kotlin | TBD | Kotlin GitHub Releases |  |
| KSP (Kotlin Symbol Processing) | TBD | KSP GitHub Releases |  |
| Compile SDK | 35 | Android 14 (API 35) |  |
| Target SDK | 35 | Android 14 (API 35) |  |
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
| Glance (Widgets) | TBD | Google Maven |  |

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
*None yet*

## Unverified Items (to be verified)
- Food kcal values vs 4×protein + 4×carbs + 9×fat (check each seed food)
- Recipe ingredient quantities and total cooked weights
- Exercise library defaultRestSec values
- OCR parsing accuracy on real treadmill displays
- AI photo analysis accuracy with free tier models
- Coach chat response quality with free tier models
- Widget update frequency and battery impact
- Knob drag smoothness at 60 fps on mid-range device
- Notification exact timing on Android 14+

## Manual Device Checklist (for OnePlus 11R)
- [ ] Knob drag smoothness on Summary Dial
- [ ] Rest timer chronometer notification countdown
- [ ] Daily reminder notifications fire at exact times
- [ ] Widget updates reflect live data
- [ ] Biometric auth / PIN fallback works
- [ ] Camera and OCR on real treadmill display
- [ ] AI photo analysis returns usable food data
- [ ] Coach chat streams responses token-by-token
- [ ] JSON export/import round-trip (excludes secrets)

## Build Investigation Status
*To be updated during each step*

### 2C Measurements Table (from Activity.png, 393×855 frame, scale 1.675 px/du)

| Element | Value | Unit | Notes |
|---------|-------|------|-------|
| **Hero Card** | | | |
| Card x | 13.1 | du | |
| Card y | 121.8 | du | |
| Card width | 365.4 | du | |
| Card height | 326.6 | du | |
| Card radius | 11 | du | (not 16) |
| Card fill | #FD3F00 | hex | |
| **"MOVE" label** | | | |
| x | 122 | du | |
| y | 312 | du | |
| Font size | ~21 | du | Monospace |
| **KCAL Remaining** | | | |
| x | 125 | du | |
| y | 402 | du | |
| Font size | ~60.5 | du | Sans, weight 600, letter-spacing -1.2 |
| **SHOW MORE** | | | |
| x | 636.5 | du (centered) | |
| y | 378 | du | |
| Font size | 14 | du | Monospace, weight 500, letter-spacing 2.2 |
| **Time pill (09:24)** | | | |
| Pill rect | 505–668, 561–607 | du | rx 8.5 |
| Pill gradient | #3a4144 → #5b656c → #5f6a72 → #5e6870 → #5a646b → #3c4346 | | Vertical linear |
| Time font size | 22 | du | Monospace, weight 500 |
| **Inset Grey Panel (inside hero card)** | | | |
| Panel inset from card edges | 6 | du | |
| Panel radius | ~6 | du | |
| Panel fill | #5F6971 | hex | FLAT, NOT gradient |
| Inner shadow top/left | ~#424143 at edge, fading over ~7 du | | |
| **Guide Lines** | | | |
| Line 1 y | 15% | panel height | |
| Line 2 y | 50% | panel height | |
| Line thickness | 1.8 | du | Darker than panel |
| **Polyline / End Dot** | | | |
| End dot x | 85.6% | panel width | Polyline STOPS here |
| End dot y | 53% | panel height | |
| End dot radius | 6 | du | |
| Polyline color | Black (RefInk #070707) | | With soft shadow |
| **Tooltip Pill** | | | |
| Size | ~55 × 19 | du | |
| Center x | End dot x | | |
| Center y | Guide line 1 y | | |
| Outline | Thin, accent color | | |
| Text | "111BMP" | | |
| **Vertical Leader** | | | |
| From | Pill bottom | | |
| To | End dot | | |
| Thin, guide line color | | | |
| **Bottom Labels** | | | |
| Y position | ~87% | panel height | |
| Labels | "6m — 7m — 8m — 9m — 10m — 11m — 12m — 13m" | | With dashes |
| Font size | ~5–6 | du | Tiny, clipped at panel edges |
| Color | #3D454A | | |
| **Capsule Rows** | | | |
| Height | 90.7 | du | |
| Radius | Half height | | |
| Fill | #0D0D0D | hex | |
| Dark outer ring | ~6.5 du, #030303 | | |
| Light rim line | Bottom edge, #1A1A1A | | |
| **Thumbnail (in capsule)** | | | |
| Size | 108.7 × 73 | du | |
| Fill | #5F6971 | hex | Flat |
| Bottom ruler | Numbers 960/980/990 | | Active dark, NOT "MIN AVG MAX" |
| **Hero Card Text (additional)** | | | |
| Time font | ~83 | du | Monospace |
| Superscript | ~29 | du | |
| **Dial / Summary (from vital-ui)** | | | |
| Dial radius | 147.5 | du | From dial.js |
| Inner dial | 115.5 | du | |
| Knob radius | 34 | du | From knob.js |
| FAB radius | 40 | du | From fab.js |

---

Then do Step 2C part 1 only: hero card + HeroLineGraph. No sparklines or capsule rows yet.

Rules:
- Use only numbers from the table. Where it says EST, keep the value but mark it "// EST" in a comment so we can tune it later.
- Panel is a FLAT #5F6971 (put it in Theme.kt) with a real inner shadow on its top and left edges (darkest ~#424143 at the edge, fading to flat over ~7 du). It is NOT a gradient. Remove InsetGreyPanelStart/End if nothing else uses them.
- Guide lines at 15% and 50% of panel height (not 1/3, 2/3), 1.8 du thick, darker than the panel.
- Polyline stops at the end dot (85.6% of panel width, 53% of panel height). End dot is 6 du. Tooltip pill ~55 x 19 du, centred on guide line 1 and on the end dot x, thin outline, text 111BMP. Thin vertical leader from pill to dot.
- Bottom label row at ~87% of panel height: "6m ... 13m" with dashes, tiny, clipped at both panel edges.
- Hero card: 13.1, 121.8, 365.4 x 326.6 du, radius 11, fill #FD3F00. Text positions and sizes from the table. Sizes in du, converted without font scale.
- HeroLineGraph takes data as a parameter; sample data only in @Preview.
- Do not edit any build file or any other component.

Build, install, check logcat, and take tmp/step2c1.png with adb exec-out screencap. Do not say it matches the reference. Write "NOT TESTED: visual comparison" and stop; I will measure the screenshot and send you the numeric differences.