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