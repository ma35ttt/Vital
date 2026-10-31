# Vital: Personal Fitness Tracker (v1)

**Oct 30, 2026** · Built for 2 users · Local-first · Free LLMs only

---

## 0. Working Rules

- Work in the 6 steps in Section 9. After each step run `./gradlew assembleDebug`, fix every error, pass acceptance checks.
- Never leave `// TODO`, empty stubs or fake returns. If something truly cannot be finished, note in `NOTES.md`.
- For any API newer than Android 13, read current Android docs and check signatures in installed `android.jar` before using.
- Do not silently invent nutrition values, library versions or API fields. Record unverified items in `NOTES.md`.
- Read `docs/reference.png` before Step 2 and compare rendered screens against it.
- If requirements conflict, follow the more specific one and note the choice in `NOTES.md`.
- Ask only when blocked; otherwise decide, record the decision, and continue.
- Keep `CLAUDE.md` in repo root with stack, package layout, and "run assembleDebug after every step."

---

## 1. Target, Stack & Fixed Decisions

Build **Vital**, a local-first personal health & fitness tracker for **2 users** on Android (target OnePlus 11R, Android 14+). Output: debug APK.

| Area | Decision |
|------|----------|
| SDK | `compileSdk 35`, `targetSdk 35`, `minSdk 28` |
| Language & UI | Kotlin, Jetpack Compose, Material3 base, **custom Canvas components** |
| Architecture | Single Activity, MVVM + Clean (data, domain, ui), unidirectional data flow |
| DI | Hilt |
| Database | Room with KSP |
| Background Work | WorkManager |
| HTTP | OkHttp + kotlinx.serialization |
| AI | **Gemini 1.5 Flash (free tier)** + **OpenRouter free models** — photo analysis & coach chat only |
| OCR | ML Kit Text Recognition (bundled on-device model) for treadmill display scanning |
| Fonts | JetBrains Mono & Space Grotesk, bundled in `res/font` (downloaded at repo build time) |
| Build Type | Debug only. Minification off. Empty `proguard-rules.pro` for validity |
| Sync | **Local-only** — no Google Sheets sync |
| Auth | **Simple local PIN / biometric** (BiometricPrompt + fallback PIN) |
| Notifications | **Rest timer + daily reminders** (meal logging, workout, water) |
| Widgets | **Home screen widgets** for quick logging (calories, water, start workout) |

Deliver: compilable source, all Gradle files (version catalog), `AndroidManifest.xml`, pinned library versions in `NOTES.md`.

---

## 2. Room Data Model

Every syncable entity carries `id` (UUID string), `updatedAt` (epoch millis), `deleted` (boolean tombstone). Never hard-delete a synced row. Nutrition values stored per 100 g.

| Entity | Key Fields |
|--------|------------|
| `User` | id, name, email, themeColor, trueBlack, pinHash, biometricEnabled, createdAt |
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

Replicate the depth, knurled knobs, inner shadows, and typography of `docs/reference.png` as closely as possible. All colors in one `Theme.kt` object; **no hex literals elsewhere**.

### Palette

| Token | Value |
|-------|-------|
| Background | `#141414` (true-black setting: `#000000`) |
| Card | `#1E1E1E` |
| Near-black row | `#0B0B0B` with 1.5dp `#2A2A2A` stroke |
| Inset grey panel | `#6B7780`, grid lines `#454D53`, labels `#3D454A` |
| Primary text / muted text | `#F2F2F2` / `#8A8A8A` |
| Tag and button fill | `#2A2A2A` |

### Tab Accents (black text on accent fills)

| Tab | Label | Accent |
|-----|-------|--------|
| 1 | TDY | `#FF4D00` Safety Orange |
| 2 | MLS | `#C6FF00` Acid Lime |
| 3 | LFT | `#3D7BFF` Electric Blue |
| 4 | CRD | `#00E0C6` Teal |
| 5 | WKS | `#FFC400` Amber |
| 6 | SET | `#E6E6E6` Light Grey |

### Depth

Use a custom `DrawModifier` / `drawIntoCanvas` with native `Paint` and `BlurMaskFilter` (or `RenderEffect`, API 12+) to draw **real inner shadows** on panels, cards, tags. Flat shapes are not acceptable. Build one reusable `Modifier.insetShadow(...)` and use it everywhere.

### Summary Dial & Knurled Knob

Draw everything with Compose `Canvas`. All sizes are fractions of canvas width W and height H.

- **Dark disk** `#141414`: center (0.745W, 0.583H), radius 0.70W, faint inner ring at 0.545W, subtle outer rim highlight.
- **Tick ring**: gray ticks `#3D3D3D` from radius 0.63W to 0.665W, one every 3.5° across upper arc 150°–345°.
- **Accent tick fan**: anchored at radius 0.65W, spanning 80°–200°. Tick length grows 0.04W → 0.25W; stroke width & alpha scale up.
- **Knurled knob**: radius 0.18W, center (0.23W, 0.345H). 1.5dp black outline, serrated edge (tick every 6°), inner bevel gradient, black center dot, thick hand pointing at current progress. **Dragging rotates knob & updates values live.**

### Typography

- Clocks, numbers, tags, labels: **JetBrains Mono**, uppercase, wide letter-spacing.
- Headlines & big values: **Space Grotesk bold**, tight tracking.

---

## 4. Screens & Navigation

Six bottom tabs with labels: **TDY, MLS, LFT, CRD, WKS, SET**. Each tab uses its accent from Section 3.

1. **TODAY (Activity layout)**. Top bar: day tag left (`SUN`), tracked title center (`TODAY`), date tag right (`OCT.30`). Hero card on accent fill: top-left label, top-right "Now", huge light-weight mono remaining-kcal number with superscript unit, three-stat macro row, inset grey line graph with polyline, end dot, tooltip bubble, day labels. Below: `WORKOUTS` header with boxed `SHOW MORE` button, then capsule rows (`#0B0B0B`) each with icon badge, middle value, right-hand grey sparkline card (0.28W × 0.185W).

2. **SUMMARY DIAL**. Opened by `SHOW MORE` on any tab. Full accent background with dark top bar. Header: "KCAL LEFT" over huge grotesque value, `SHOW MORE` button with 2dp stroke. Contains interactive knob, inner round readout, category arc with P/C/F glyphs, left readout with chevron, bottom-left period switch (3 positions).

3. **MEALS**. Daily meal log + dial summary: kcal vs target, macro % split. Four ways to log:
   - Search with autocomplete over local foods
   - Smart text parser: `chicken breast cooked 200g, rice 150g` → split on commas/newlines, extract qty/unit, fuzzy-match food name. If food has RAW/COOKED variants and text doesn't specify → modal `RAW OR COOKED?` (never guess).
   - Web product search via Open Food Facts REST API. Convert to per-100g, show for confirmation, save as `Food` with source OFF. Handle offline/empty results visibly.
   - **Recipe batch splitter**: log recipe by % of batch or by cooked weight eaten, scaling macros from `totalCookedWeightG`. Preload "Chicken tomato pasta" & "Paneer gravy pasta".

4. **LIFT**. Workout session logger + dial summary of rest timer.
   - Exercise selection pre-fills weight/reps from most recent session.
   - **Plate calculator**: target weight → plates per side from 25, 20, 15, 10, 5, 2.5, 1.25 kg (diagram). Assume 20 kg bar, user-changeable. If target not exact → show nearest loadable weight.
   - **Rest timer**: auto-starts when set logged. Presets 2/3/4 min, last choice remembered in `Settings`. Uses exercise `defaultRestSec` as initial preset.

5. **CARDIO**. Session logger, **OCR scanner**, dial summary.
   - Photo scanner using ML Kit on-device text recognition on treadmill display. Extract duration, distance, speed, incline, calories. Show parsed values in editable fields for correction before save.
   - If calories unreadable → compute via ACSM walking equation, set `kcalSource = ACSM`:
     ```
     VO₂ = 0.1·v + 1.8·v·g + 3.5
     kcal = (VO₂·m/1000)·5·t
     ```
     v = speed m/min, g = grade (incline%/100), m = body weight kg from Settings, t = duration min.
   - **ACSM valid only up to 6 km/h** — mark estimates outside range.

6. **WEEKS**. 12-week summary of macros & training progression (volume, PRs, body weight trend).

7. **SETTINGS**. Targets, AI provider (Gemini/OpenRouter) + API key, true-black toggle, JSON import/export, notification toggles, reminder times, PIN/biometric setup, widget configuration.

**L / M / H Switch** (three-way selector on dial): chooses time period on dial — L = last 7 days, M = last 4 weeks, H = last 12 weeks. Today's values use live day total; switch only changes history ring & accent tick fan.

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

## 7. Notifications, Foreground Service & Widgets

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

### Home Screen Widgets (Glance)
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
- `@Preview` or debug screen renders each component; screenshot compared vs `docs/reference.png`. Note uncloseable differences.
- Knob drag rotates & updates value live at 60 fps, no visible stutter on mid-range device.

### Step 3: Today Tab, Summary Dial, Navigation
- Six-tab bottom nav, TODAY screen with live Room data, Summary Dial from `SHOW MORE`, L/M/H period switch.
- `assembleDebug` succeeds.
- All six tabs open, keep accent colors; rotation/tab switch preserves state.
- Logging test meal updates remaining-kcal on TODAY and Dial.

### Step 4: Meals & Lift
- Search autocomplete, smart text parser, Open Food Facts search, recipe batch splitter, workout logger, plate calculator, rest timer logic (foreground service + chronometer).
- `assembleDebug` succeeds.
- Parser tests: `chicken breast cooked 200g, rice 150g` → two entries with correct grams; ambiguous `rice 150g` triggers `RAW OR COOKED?` modal.
- Plate calculator: 100 kg on 20 kg bar → 25+15 per side; unreachable target → nearest loadable weight.
- Recipe splitter: 25% of batch scales macros correctly (unit test).
- Logging set pre-fills next from history, starts rest timer with exercise defaultRestSec.

### Step 5: Cardio OCR, Progress, Weeks, Coach Chat, Notifications, Widgets
- Photo scan with ML Kit, editable parsed fields, ACSM fallback, foreground service with chronometer.
- Progress screen: photo timeline, measurements, weight trend.
- Weeks screen: 12-week charts.
- Coach Chat: conversation UI, context injection, streaming responses.
- WorkManager periodic reminders (meals, workout, water).
- Glance widgets (calories, water, workout).
- `assembleDebug` succeeds.
- Unit test: ACSM calculation matches hand-computed result for known input.
- On device: rest timer shows countdown notification; completion posts alert then clears.
- Reminders fire at scheduled times.
- Widgets update on data change.
- AI photo analysis returns structured food data; coach chat streams response.

### Step 6: Polish, Export/Import, Device Testing
- JSON export (excludes AI keys, PIN hash), import (never overwrites AI keys, PIN).
- Error handling, offline support, loading states.
- Device testing on OnePlus 11R: knob smoothness, OCR accuracy, AI response times, notification behavior, widget updates, auth flow.
- `NOTES.md` complete: library versions, unverified data, design differences, requirement changes, untested items.

---

## 10. Deliverables

- Complete compilable Android project in `/Users/mrityunjaychouhan/Vital/`
- `SPEC.md` (this file), `CLAUDE.md`, `NOTES.md`
- `app/src/main/assets/seed_foods.json`, `exercises.json`
- Debug APK at `app/build/outputs/apk/debug/app-debug.apk`