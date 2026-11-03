# Vital: product spec

Local-first fitness tracker for two users on Android 14+ (minSdk 28, target 36). Data lives in Room on the phone; the network is optional (food lookup, AI, sync). Order of work: ROADMAP.md. Look: design/ref and design/tabs. Rules: CLAUDE.md.

## 1. Tabs (bottom glass nav: TDY, MLS, LFT, +, CRD, WKS, SET)
- Today: dial summary of the day, macros, water, steps (manual in v1), quick status.
- Summary: dial and graphs (pixel reference `design/ref/Summary.png`; Activity in `Activity.png`).
- Meals: slots (breakfast, lunch, dinner, snack), smart-text entry, search, recents, totals.
- Lift: sessions, exercises, sets (weight, reps), plate calculator, rest timer, PRs.
- Cardio: manual entry, treadmill snap, ACSM kcal estimate.
- Weeks: adherence L = 7 days, M = 4 weeks, H = 12 weeks; weekly volume and trends.
- Settings: units, TRUE BLACK (default ON), reminders, AI key, export/import, lock.

## 2. Data
- Room is the source of truth. Syncable entities carry id (UUID), updatedAt, deleted (tombstone).
- Room schema exported; migrations never destructive.
- JSON export/import excludes aiApiKeyEncrypted and pinHash; import never overwrites them.
- Widgets removed; no WidgetConfig entity.

## 3. Logic (unit-tested in M3)
- Smart-text parser (e.g. "chicken 150g", RAW/COOKED ambiguity asks once).
- Plate calculator: 80 kg = 25 + 5 per side; 100 kg = 25 + 15; nearest loadable weight.
- ACSM: valid at speed <= 6 km/h; 6 km/h, 2% incline, 68 kg, 32:10 = 187 kcal.
- Recipe splitter, kcal and macro totals, estimated 1RM, PR detection, weekly volume, adherence %.

## 4. Gestures
- Tap `+` (< 250 ms): QUICK ADD bottom sheet (also exposed as TalkBack custom actions).
- Hold `+` 250 ms: half disc (centre 196.5, 781 du; radius 184.5) with MEAL, LOG SET, SNAP, CARDIO, WEIGHT; slide to hover, release to confirm, release elsewhere cancels.
- Flick up >= 110 du at >= 1,400 du/s while holding: opens the camera (snap).
- Swipe left/right between the six tabs (HorizontalPager, beyondViewportPageCount = 0).
- Details: FIXES_AND_GESTURES_BRIEF.md sections 3 and 5.

## 5. Snap flow
System camera (TakePicture into a MediaStore URI; CAMERA permission requested first); downscale to <= 1600 px; ML Kit OCR on device. Treadmill if >= 2 of speed, incline, time, distance, calories are found, otherwise food. Food goes to the AI (Gemini or OpenRouter); confidence < 70 %, offline or no key falls back to manual entry. Result screens: `snap-treadmill.png`, `snap-meal.png`; always editable before logging.

## 6. AI
Optional. Keys encrypted (Android Keystore / EncryptedSharedPreferences). The app works fully without a key. Photo estimates are labelled rough.

## 7. Auth (pending, built in M6)
PIN with biometric (BiometricPrompt, PIN fallback); no re-prompt for 30 s after leaving; option to hide content in recents.

## 8. Permissions
| Permission | Why |
|---|---|
| INTERNET | food lookup, AI, sync |
| CAMERA | snap flow |
| USE_BIOMETRIC | unlock (not USE_FINGERPRINT) |
| POST_NOTIFICATIONS | reminders, rest timer |
| SCHEDULE_EXACT_ALARM | only if used; check `canScheduleExactAlarms()` first |
| FOREGROUND_SERVICE (+ type) | rest timer |
| RECEIVE_BOOT_COMPLETED | restore reminders |

## 9. Performance targets
Measured on the non-debuggable `fast` build: no sustained jank while swiping tabs and scrolling Today/Meals (janky frames under 5 %, 95th percentile frame time under 16 ms on the 11R).

## 10. Out of scope for v1
Widgets, Health Connect steps, cloud accounts, social features.

## 11. v1.1 candidates (decide after M5)
Quality-of-life list in FIXES_AND_GESTURES_BRIEF.md section 7; ideas from openGym and Fud AI after review (barcode scan, voice entry, charts export, routine templates).

## 12. Steps mapped to the roadmap
Chrome and design system: M2. Logic: M3. Tabs: M4. Daily-use review: M5. Gestures, speed, auth, notifications, export: M6. Snap, AI, extras, final cleanup: M7.
