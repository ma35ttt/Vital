# Vital: Personal Fitness Tracker (v1)

**Oct 30, 2026** · Built for 2 users · Local-first · Free LLMs only
**Revised Nov 1, 2026**: UI redesign (one orange accent, glass nav, new tab designs). See section 0A.

---

## 0. Working Rules

- Work in the 6 steps in Section 9. After each step run `./gradlew assembleDebug`, fix every error, pass acceptance checks.
- Never leave `// TODO`, empty stubs or fake returns. If something truly cannot be finished, note in `NOTES.md`.
- For any API newer than Android 13, read current Android docs and check signatures in installed `android.jar` before using.
- Do not silently invent nutrition values, library versions or API fields. Record unverified items in `NOTES.md`.
- Read `Notes/docs/Summary.png` and `Notes/docs/Activity.png` before Step 2: they are the only pixel references. Compare rendered Summary and Today screens against them by measured numbers, not by eye. All other tabs follow the design source of truth below.
- If requirements conflict, follow the more specific one and note the choice in `NOTES.md`.
- Ask only when blocked; otherwise decide, record the decision, and continue.
- Keep `CLAUDE.md` in repo root with stack, package layout, and "run assembleDebug after every step."

## 0A. Design Source of Truth

DESIGN SOURCE OF TRUTH
- Summary and Activity: pixel references (`Notes/docs/Summary.png`, `Notes/docs/Activity.png`). Their measured numbers win over any number in this file.
- All other tabs (Meals, Lift, Cardio, Weeks, Settings) and the shared chrome: `design/tabs/<screen>.md` (measured numbers) and `REMAINING_TABS_BRIEF.md` (repo root). The PNGs in `design/tabs/` are for human review. Build from the .md numbers, because the model cannot reliably view images. If `design/tabs/<screen>.md` is missing, stop and ask before building that screen.
- One accent, orange `#FD3F00`, plus black and `#5F6971`. No per-tab accents.
- Where files disagree: `design/tabs/*.md` wins on look, `REMAINING_TABS_BRIEF.md` wins on behaviour, SPEC.md wins on data and logic.
- `Notes/old/` and every earlier brief are obsolete. Do not read them.

---

## 1. Target, Stack & Fixed Decisions

Build **Vital**, a local-first personal health & fitness tracker for **2 users** on Android (target OnePlus 11R, Android 14+). Output: debug APK.

| Area | Decision |
|------|----------|
| SDK | `compileSdk 36`, `targetSdk 36`, `minSdk 28` (36 is what is installed and in use, see CLAUDE.md) |
| Language & UI | Kotlin, Jetpack Compose, Material3 base, **custom Canvas components** |
| Architecture | Single Activity, MVVM + Clean (data, domain, ui), unidirectional data flow |
| DI | Hilt |
| Database | Room with KSP |
| Background Work | WorkManager |
| HTTP | OkHttp + kotlinx.serialization |
| AI | **Gemini 1.5 Flash (free tier)** + **OpenRouter free models** — photo analysis & coach chat only |
| OCR | ML Kit Text Recognition (bundled on-device model) for treadmill display scanning |
| Fonts | JetBrains Mono + Inter Tight, bundled in `res/font` (SIL OFL; downloaded by script or added manually). Space Grotesk is dropped. Step 2 keeps the existing fonts until the font pass. |
| Build Type | Debug only. Minification off. Empty `proguard-rules.pro` for validity |
| Sync | **Local-only** — no Google Sheets sync |
| Auth | **Simple local PIN / biometric** (BiometricPrompt + fallback PIN) |
| Notifications | **Rest timer + daily reminders** (meal logging, workout, water) |
| Widgets | **REMOVED 2026-10-31** (user request). Not built. Do not add Glance dependencies. |

Deliver: compilable source, all Gradle files (version catalog), `AndroidManifest.xml`, pinned library versions in `NOTES.md`.

---

## 2. Room Data Model

Every syncable entity carries `id` (UUID string), `updatedAt` (epoch millis), `deleted` (boolean tombstone). Never hard-delete a synced row. Nutrition values stored per 100 g.

| Entity | Key Fields |
|--------|------------|
| `User` | id, name, email, themeColor (unused: kept to avoid a schema change, no UI), trueBlack, pinHash, biometricEnabled, createdAt |
| `Food` | name, variant (RAW, COOKED, NONE), proteinPer100, carbsPer100, fatPer100, kcalPer100, source (SEED, USER, AI, OFF), barcode? |
| `Recipe` | name, totalCookedWeightG, servingsNote?, ingredientsJson |
| `MealLogEntry` | date (yyyy-MM-dd), mealSlot, foodId?, recipeId?, grams, proteinG, carbsG, fatG, kcal (macros snapshotted at log time) |
| `Exercise` | name, muscleGroup, isBarbell, equipment, instructions, defaultRestSec, isCompound |
| `WorkoutSession` | date, startedAt, endedAt?, notes?, routineId? |
| `WorkoutSet` | sessionId, exerciseId, setIndex, weightKg, reps, rpe?, isWarmup, restSecTaken? |
| `CardioSession` | date, type, durationSec, distanceKm?, speedKmh?, inclinePct?, kcal, kcalSource (READ, ACSM) |
| `BodyMetrics` | date, weightKg, bodyFatPct?, measurementsJson (waist, chest, arms, thighs, etc.) |
| `ProgressPhoto` | date, imagePath, type (front/side/back), notes? |
| `WaterEntry` | date, amountMl, time |
| `Settings` | kcalTarget, proteinTarget, carbsTarget, fatTarget, bodyWeightKg, aiProvider, aiApiKeyEncrypted, trueBlack, restPresetSec, notificationsEnabled, dailyReminderTime, waterReminderEnabled, waterReminderIntervalMin |
| `SyncMeta` | lastPushAt, lastPullAt, lastError? |
| `WidgetConfig` | userId, widgetType (calories/water/workout), displayPrefsJson |

**Macros on `MealLogEntry` are snapshotted** so later edits to a `Food` never rewrite history. DAOs expose `Flow`s for screens + one-shot queries for export.

---

## 3. Design System & Canvas Rendering

Replicate the depth, knurled knobs, inner shadows, and typography of the two pixel references (`Notes/docs/Summary.png`, `Notes/docs/Activity.png`) and the tab designs in `design/tabs/`. All colors in one `Theme.kt` object; **no hex literals elsewhere** (exception: generated `DialTicks.kt`).

### Palette

| Token | Value | Use |
|-------|-------|-----|
| orange | `#FD3F00` | hero fill, active states, accents |
| black | `#000000` | screen background (TRUE BLACK ON, the default), sheets, text on orange |
| background, true black OFF | `#141414` | screen background when `trueBlack` is off |
| chipOnOrange | `#E83A00`, stroke `#C93300` | header chips on an orange screen |
| chipDark | `#0C0C0C`, stroke `#1E1E1E` | header chips on a black screen |
| well | `#0A0A0A`, 1.5 du stroke `#1F1F1F` | rows, fields, tiles on black |
| capsule row (Today) | fill `#0D0D0D`, outer ring `#030303`, rim `#1A1A1A` | measured from Activity.png |
| panelGrey | `#5F6971`, flat | data panels, thumbnails; inner shadow top/left `#424143` over ~7 du; grid lines darker than the panel; labels `#3D454A` |
| dim text on orange | `#D33100` | "Now", stat labels (measured) |
| textPrimary / textMuted | `#F2F2F2` / `#8A8A8A` | text |
| tickGrey | `#3D3D3D` | dial tick ring |
| iconWell | `#121212`, stroke `#232323` | round icon wells in capsule rows |

Legacy tokens (Card `#1E1E1E`, tag fill `#2A2A2A`, inset grey `#6B7780`, `InsetGreyPanelStart/End`) are replaced by the table above. Remove each when nothing references it.

### Accent (one accent, no per-tab colours)

The app has one accent, orange `#FD3F00` (measured from Activity.png; the old Safety Orange `#FF4D00` is replaced). Per-tab accents (lime, blue, teal, amber, light grey) are removed. Active tab = orange fill with black text.

### Depth

Use a custom `DrawModifier` / `drawIntoCanvas` with native `Paint` and `BlurMaskFilter` (or `RenderEffect`, API 12+) to draw **real inner shadows** on panels, cards, tags. Flat shapes are not acceptable. Build one reusable `Modifier.insetShadow(...)` and use it everywhere. Use it on header chips, buttons, wells and grey panels.

### Summary Dial & Knurled Knob

Draw everything with Compose `Canvas` in **design units (du)**: the design frame is 393 x 855 du and 1 du = screenWidthDp / 393 (`ui/theme/DesignUnits.kt`). Never use fractions of W or H. All dial numbers (disc, inner dial, tick tracks, knob, FAB, text) come from the measured files, which win over any number in this document: `DialTicks.kt` (generated, never hand edit), and `Notes/vital-ui/src/dial.js`, `knob.js`, `fab.js`, `main.js`.

The same dial language is reused, parametrised, by `OrangeDial` on Meals, Lift and Cardio (`REMAINING_TABS_BRIEF.md` section 3).

- **Knurled knob**: serrated edge, inner bevel, black centre dot, thick pointer. On the Summary screen dragging rotates the knob and updates values live. On the Meals, Lift and Cardio dial art the knob is decorative (decision in NOTES.md).

### Typography

- Labels, tags, clocks and the huge Weeks number: **JetBrains Mono**, uppercase, wide letter-spacing.
- Names and numerals: **Inter Tight** 500/600/700, tight tracking. Space Grotesk is dropped.
- Text sizes are in du and must ignore system font scale: `(du * scale).dp.toSp()`. Never `.sp` with a du value.
- Step 2 keeps the existing fonts; the font switch is a separate pass after Step 2 is accepted. Inter Tight is SIL OFL: add the font files to `res/font` (not a dependency change).

### Navigation chrome and tab scaffold

Liquid-glass pill nav `TDY MLS LFT [+] CRD WKS SET` (active tab = orange fill, black text) with a centre `+` that opens a QUICK ADD sheet (LOG MEAL, LOG SET, LOG CARDIO, LOG BODY WEIGHT). Shared `ScreenScaffold` (header chips SUN / date, title, hero variants). No blur library: build the glass from translucent fills, borders and highlights. Numbers: `REMAINING_TABS_BRIEF.md` section 2 and `design/tabs/*.md`.

---

## 4. Screens & Navigation

Six tabs plus a centre `+` action in a glass bottom nav: **TDY, MLS, LFT, [+], CRD, WKS, SET** (see Section 3). One accent (orange) everywhere. Meals, Lift and Cardio use the `OrangeHero` scaffold; Weeks and Settings use `BlackPage`. Today and Summary are the pixel references.

1. **TODAY (Activity layout)**. Top bar: day tag left (`SUN`), tracked title center (`TODAY`), date tag right (`OCT.30`). Hero card on accent fill: top-left label, top-right "Now", huge light-weight mono remaining-kcal number with superscript unit, three-stat macro row, inset grey line graph with polyline, end dot, tooltip bubble, day labels. Below: `WORKOUTS` header with boxed `SHOW MORE` button, then capsule rows (`#0B0B0B`) each with icon badge, middle value, right-hand grey sparkline card (0.28W × 0.185W).

2. **SUMMARY DIAL**. Opened by the hero `SHOW MORE` button (Today, Meals, Lift, Cardio). Full orange background with dark top bar. Header: "KCAL LEFT" over huge grotesque value, `SHOW MORE` button with 2dp stroke. Contains interactive knob, inner round readout, category arc with P/C/F glyphs, left readout with chevron, bottom-left period switch (3 positions). The `SHOW MORE` next to section labels (WORKOUTS, TRENDS, TODAY) is not specified: ask the user when building that tab.

3. **MEALS**. Daily meal log + dial summary: kcal vs target, macro % split. Layout: `design/tabs/meals.md`; behaviour: tabs brief section 4.1. Four ways to log:
   - Search with autocomplete over local foods
   - Smart text parser: `chicken breast cooked 200g, rice 150g` → split on commas/newlines, extract qty/unit, fuzzy-match food name. If food has RAW/COOKED variants and text doesn't specify → modal `RAW OR COOKED?` (never guess).
   - Web product search via Open Food Facts REST API. Convert to per-100g, show for confirmation, save as `Food` with source OFF. Handle offline/empty results visibly.
   - **Recipe batch splitter**: log recipe by % of batch or by cooked weight eaten, scaling macros from `totalCookedWeightG`. Preload "Chicken tomato pasta" & "Paneer gravy pasta".

4. **LIFT**. Workout session logger + dial summary of rest timer. Layout: `design/tabs/lift.md`; behaviour: tabs brief section 4.2.
   - Exercise selection pre-fills weight/reps from most recent session.
   - **Plate calculator**: target weight → plates per side from 25, 20, 15, 10, 5, 2.5, 1.25 kg (diagram). Assume 20 kg bar, user-changeable. If target not exact → show nearest loadable weight.
   - **Rest timer**: auto-starts when set logged. Presets 2/3/4 min, last choice remembered in `Settings`. Uses exercise `defaultRestSec` as initial preset.

5. **CARDIO**. Session logger, **OCR scanner**, dial summary. Layout: `design/tabs/cardio.md`; behaviour: tabs brief section 4.3.
   - Photo scanner using ML Kit on-device text recognition on treadmill display. Extract duration, distance, speed, incline, calories. Show parsed values in editable fields for correction before save.
   - If calories unreadable → compute via ACSM walking equation, set `kcalSource = ACSM`:
     ```
     VO₂ = 0.1·v + 1.8·v·g + 3.5
     kcal = (VO₂·m/1000)·5·t
     ```
     v = speed m/min, g = grade (incline%/100), m = body weight kg from Settings, t = duration min.
   - **ACSM valid only up to 6 km/h** — mark estimates outside range.

6. **WEEKS**. Orange hero (adherence %, P/C/F, grey bar chart with 12 bars) plus TRENDS rows (body weight, weekly volume, bench PR). Period switch in the hero: L = 7 days, M = 4 weeks (default, as in the mockup), H = 12 weeks. Layout: `design/tabs/weeks.md`; behaviour: tabs brief section 4.4.

7. **SETTINGS**. Layout: `design/tabs/settings-*.md`; behaviour: tabs brief section 4.5. Daily targets hero, profile (name, TRUE BLACK default ON), body weight, rest preset, AI coach (Gemini/OpenRouter + masked key), security (PIN, biometric), notifications, data (JSON export/import). No theme colour picker. Widget configuration is on hold (widgets removed).

**L / M / H Switch** (three-way selector on the Summary dial and in the Weeks hero): chooses time period: L = last 7 days, M = last 4 weeks, H = last 12 weeks. Today's values use live day total; the switch only changes history ring & accent tick fan. On the Meals, Lift and Cardio dial art the L/M/H letters are decorative and non-focusable (decision in NOTES.md).

---

## 5. Feature Logic

### Meals & Nutrition
- Seed database from `app/src/main/assets/seed_foods.json` (28 distinct foods, 32 rows with 4 RAW/COOKED variants).
- Per-100g values from USDA FoodData Central or Indian Food Composition Tables; write reference name in `source` field per item. Every item `"verified": false`.
- After build, list all 28 items with values in `NOTES.md` for review. Check kcal ≈ 4×protein + 4×carbs + 9×fat; flag >10% deviation.

### Lift
- Plate calculator diagram drawn in Compose Canvas.
- Rest timer: foreground service with chronometer notification (countdown). Presets 2/3/4 min. Uses exercise `defaultRestSec` when available.
- Exercise library includes `defaultRestSec` (compound: 180s, isolation: 90s) and `isCompound` flag.

### Cardio & OCR
- ML Kit bundled model for text recognition.
- ACSM fallback as above.

---

## 6. AI Integration (Free Tier Only)

### Providers (user-selectable in Settings)

| Provider | Endpoint | Free Tier |
|----------|----------|-----------|
| **Gemini 1.5 Flash** | `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=API_KEY` | 1,500 req/day |
| **OpenRouter** | `https://openrouter.ai/api/v1/chat/completions` | Free models: `meta-llama/llama-3.1-8b-instruct:free`, `google/gemma-2-9b-it:free`, `microsoft/phi-3-mini-128k-instruct:free`, etc. |

### Photo Analysis (Food Recognition)
- **Input**: Compressed image (~1 MB max) + optional context text ("lunch", "homemade", etc.)
- **Prompt**: Structured system prompt for food identification + macro estimation per 100g + confidence
- **Output**: `{ foodName, confidence, proteinPer100, carbsPer100, fatPer100, kcalPer100, suggestedGrams, variant? }`
- **Fallback**: If API fails or confidence < 0.7 → manual entry with search

### Coach Chat
- **Input**: Conversation history + user context (goals, recent 7-day logs, current stats, today's remaining macros)
- **System Prompt**: Fitness/nutrition coach persona — concise, actionable, encouraging
- **Models**: Gemini 1.5 Flash or OpenRouter free models (user choice)
- **Features**: Meal suggestions, workout adjustments, progress analysis, Q&A
- **Streaming**: Token-by-token response rendering

### Security
- API keys stored encrypted (Android Keystore / EncryptedSharedPreferences)
- JSON export **excludes** AI API keys
- JSON import **never overwrites** AI API keys

---

## 7. Notifications & Foreground Service (widgets removed)

### Rest Timer Foreground Service
- Type: `shortService` (API 34+) or `specialUse` with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`
- Chronometer notification: `setUsesChronometer(true)`, `setChronometerCountDown(true)`
- High-priority alert on completion, then clear

### Daily Reminders (WorkManager periodic)
- **Meal logging**: Configurable times (breakfast/lunch/dinner/snack)
- **Workout reminder**: Configurable days + time
- **Water reminder**: Configurable interval (e.g., every 2 hours)
- All use `setExactAndAllowWhileIdle` for precision

### Runtime Permissions
- Request `POST_NOTIFICATIONS` before first alert
- Settings shows current permission state with link to system page
- BiometricPrompt for auth, fallback to PIN (stored as argon2 hash)

### Home Screen Widgets (Glance): REMOVED 2026-10-31 (user request, see CLAUDE.md). Not built. The table below is kept only as history; the Settings WIDGETS section from the tabs brief is on hold until the user re-enables widgets (NOTES.md, open questions).
| Widget | Size | Features |
|--------|------|----------|
| **Calories** | 2×2 | Remaining kcal, macro ring, quick-add food button |
| **Water** | 2×1 | Current intake vs target, +250ml/+500ml buttons |
| **Workout** | 2×2 | "Start Workout" → opens Lift tab, shows today's routine preview |

### Manifest Permissions

| Permission | Reason |
|------------|--------|
| `INTERNET` | Open Food Facts, AI APIs |
| `POST_NOTIFICATIONS` | All notifications (API 33+) |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Rest timer service |
| `CAMERA` | Food photo capture (or system picker) |
| `ACCESS_NETWORK_STATE` | Skip AI calls when offline |
| `SCHEDULE_EXACT_ALARM` | Exact reminder timing (API 31+) |
| `USE_BIOMETRIC` | Biometric authentication |
| `USE_FINGERPRINT` | Fingerprint fallback |

---

## 8. Seed Data

Create `seed_foods.json` with **exactly 28 foods** (22 core + 6 extra), each with per-100g protein, carbs, fat, kcal. Use USDA FoodData Central or Indian Food Composition Tables; write reference name in `source` field. Every item `"verified": false`. After build, list all 28 items with values in `NOTES.md`. Check kcal ≈ 4P+4C+9F; flag >10% off.

| Group | Foods |
|-------|-------|
| Core 22 | chicken breast (raw, cooked), eggs (whole), egg whites, paneer, curd (plain), milk (toned), whey protein, white rice (raw, cooked), roti (wheat), oats (raw), pasta (raw, cooked), potato (boiled), banana, apple, peanuts, almonds, olive oil, ghee, toor dal (raw, cooked), chickpeas (cooked), broccoli, spinach |
| Extra 6 | tomato, onion, tofu, soya chunks (raw), peanut butter, whole wheat bread |

**Also seed two recipes** with ingredient lists & cooked total weight: "Chicken tomato pasta" & "Paneer gravy pasta". Pick sensible home-cooking quantities, store as editable data, list in `NOTES.md`.

**Exercise library**: Combined curated list from fud-ai-android + openGym (~200 exercises) with fields: name, muscleGroup, isBarbell, equipment, instructions, defaultRestSec (compound 180s/isolation 90s), isCompound. Stored in `exercises.json`, loaded at first run.

---

## 9. Build Steps & Acceptance Checks

### Step 1: Foundation, Database, Seed, Auth
- Project setup, Gradle + version catalog, manifest, Hilt, Room entities/DAOs from §2, seed loading (foods + exercises), PIN/biometric auth.
- `./gradlew assembleDebug` succeeds.
- App launches on device/emulator without crash, shows auth screen then placeholder.
- Unit tests: seed loader (32 food rows, ~200 exercises), auth logic.

### Step 2: Canvas Graphics
- `Modifier.insetShadow()`, `KnurledKnob` (drag gesture), `SummaryDial` (tick ring, accent fan, knob, macro arc, L/M/H switch), `HeroLineGraph`, `Sparkline`, fonts, `Theme.kt`.
- `assembleDebug` succeeds.
- `@Preview` or debug screen renders each component; screenshots are measured numerically against `Notes/docs/Summary.png` and `Notes/docs/Activity.png` (not judged by eye). Note uncloseable differences.
- Knob drag rotates & updates value live at 60 fps, no visible stutter on mid-range device.

### Step 3: Today Tab, Summary Dial, Navigation
- Six-tab glass bottom nav with centre `+` (QUICK ADD sheet), TODAY screen with live Room data, Summary Dial from the hero `SHOW MORE`, L/M/H period switch.
- `assembleDebug` succeeds.
- All six tabs open on the shared scaffold with the glass nav and the single orange accent; rotation/tab switch preserves state.
- Logging test meal updates remaining-kcal on TODAY and Dial.
- Build order inside Steps 3 to 5 follows `REMAINING_TABS_BRIEF.md` section 6 (shared chrome, Settings, Meals, Lift, Cardio, Weeks). Each tab's logic checks from Steps 3 to 5 apply to that tab. The tabs brief does not touch Today or Summary.

### Step 4: Meals & Lift
- Search autocomplete, smart text parser, Open Food Facts search, recipe batch splitter, workout logger, plate calculator, rest timer logic (foreground service + chronometer).
- `assembleDebug` succeeds.
- Parser tests: `chicken breast cooked 200g, rice 150g` → two entries with correct grams; ambiguous `rice 150g` triggers `RAW OR COOKED?` modal.
- Plate calculator: 100 kg on 20 kg bar → 25+15 per side; unreachable target → nearest loadable weight.
- Recipe splitter: 25% of batch scales macros correctly (unit test).
- Logging set pre-fills next from history, starts rest timer with exercise defaultRestSec.

### Step 5: Cardio OCR, Progress, Weeks, Coach Chat, Notifications
- Photo scan with ML Kit, editable parsed fields, ACSM fallback, foreground service with chronometer.
- Progress screen: photo timeline, measurements, weight trend.
- Weeks screen: L / M / H period charts (7 days, 4 weeks, 12 weeks). Progress screen and Coach Chat have no mockup yet: build them only after the user supplies designs, in the same orange/black style.
- Coach Chat: conversation UI, context injection, streaming responses.
- WorkManager periodic reminders (meals, workout, water).
- ~~Glance widgets~~ REMOVED 2026-10-31 (see CLAUDE.md). Do not add Glance.
- `assembleDebug` succeeds.
- Unit test: ACSM calculation matches hand-computed result for known input.
- On device: rest timer shows countdown notification; completion posts alert then clears.
- Reminders fire at scheduled times.
- ~~Widgets update on data change.~~ (widgets removed)
- AI photo analysis returns structured food data; coach chat streams response.

### Step 6: Polish, Export/Import, Device Testing
- JSON export (excludes AI keys, PIN hash), import (never overwrites AI keys, PIN).
- Error handling, offline support, loading states.
- Device testing on OnePlus 11R: knob smoothness, OCR accuracy, AI response times, notification behavior, auth flow. (Widgets removed: no widget checks.)
- `NOTES.md` complete: library versions, unverified data, design differences, requirement changes, untested items.

---

## 10. Deliverables

- Complete compilable Android project in `/Users/mrityunjaychouhan/Vital/`
- `SPEC.md` (this file), `CLAUDE.md`, `NOTES.md`
- `app/src/main/assets/seed_foods.json`, `exercises.json`
- Debug APK at `app/build/outputs/apk/debug/app-debug.apk`