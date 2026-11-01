# Vital

Local-first Android app (2 users, OnePlus 11R, Android 14+ / API 35). Full requirements in SPEC.md. Build decisions and versions live in NOTES.md.

## Current Build State (as of 2026-10-31)

### What Works
- Gradle configuration resolves correctly with AGP 9.5.0, Kotlin 2.2.10 (built-in), KSP 2.2.10-2.0.2, Hilt 2.60.1
- KSP passes without "unexpected jvm signature V" error
- Room 2.8.5 compiles with KSP
- Dependencies resolve (Room, Hilt, Compose, ML Kit, OkHttp, Gson, security-crypto; Glance removed with widget feature)
- Android SDK 36 installs automatically
- Resource compilation works (mipmap icons added)
- Icon adjusted for circular launcher masks (2026-10-31, user request: "i use circular icons... too big for it"): `scripts/build-icons.mjs` `iconSvg({ safe })` now scales the artwork group around the canvas centre — Android adaptive foreground `safe: 0.72` (the centre-66% circle of the 108dp layer is radius ~341/512 design px; corner rings reached ~403 + shadow and were clipped), `ic_launcher_round` + `web/maskable-512` use `safe: 0.85` (legacy round mask shows the full inscribed circle, radius 512), square legacy / iOS / playstore stay full-bleed. Regenerated via `npm run icons` into `assets/icons/` (new `master/icon-circular.svg`), android mipmaps recopied to `app/src/main/res/` (dropped copied `values/ic_launcher_background.xml` — color already exists in `values/ic_launcher_colors.xml`, duplicate-resource error). `./gradlew installDebug` BUILD SUCCESSFUL; launcher + recents screenshots (tmp/icon-zoom3.png, tmp/app.png) show the full ring grid inside the circular mask with margin; app launches with 0 `AndroidRuntime:E`.

### Versions In Use
| Component | Version | Evidence |
|-----------|---------|----------|
| AGP | 9.5.0 | developer.android.com/build/releases/agp-9-5-0-release-notes: needs Gradle 9.6.0+, JDK 17+, SDK Build Tools 36.0.0 |
| Built-in Kotlin | 2.2.10 | AGP 9.5.0 POM: `kotlin-gradle-plugin` version 2.2.10 (Google Maven: gradle-9.5.0.pom) |
| KSP | 2.2.10-2.0.2 | GitHub releases: `2.2.10-2.0.2` tag matches Kotlin 2.2.10; Maven Central verified |
| Hilt | 2.60.1 | Verified compatible with AGP 9.5.0 |
| Gradle | 9.8.2 (system) | AGP 9.5.0 requires 9.6.0+; system Gradle 9.8.2 satisfies |
| compileSdk / targetSdk | 36 | User has SDK 36 installed |
| minSdk | 28 |  |
| AndroidX Core / Core-KTX | 1.15.0 |  |
| Room | 2.8.5 | Google Maven metadata: latest stable (verified) |
| Compose BOM | 2024.10.00 |  |
| Material3 | 1.3.1 |  |
| Material Components | 1.12.0 | For base theme |
| ML Kit Text Recognition | 16.0.0 |  |
| Gson | 2.11.0 | Added for NetworkModule |
| security-crypto | 1.1.0 | Stable version |

### Current Blockers

1. **Widget feature REMOVED (user request, 2026-10-31)** — no widget code remains. Deleted: `com.vital.widget` package (was already empty), `res/layout/widget_*.xml`, `res/drawable/widget_*.xml`, widget strings in strings.xml, Glance deps + `ExperimentalGlanceApi` opt-in from `app/build.gradle.kts` and `gradle/libs.versions.toml`. Evidence: `grep -rli glance app/src` returned nothing before removal.

2. **RESOLVED 2026-10-31: `./gradlew assembleDebug` BUILD SUCCESSFUL** — `app-debug.apk` (114 MB) at `app/build/outputs/apk/debug/app-debug.apk`. `compileDebugKotlin` reports 0 errors (was 615 at session start).
   - Root cause of the final codegen crash ("Couldn't inline method call Row"): legacy `composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }` is incompatible with built-in Kotlin 2.2.10. Fix: removed `composeOptions` block, applied `org.jetbrains.kotlin.plugin.compose` version 2.2.10 (root `build.gradle.kts` apply false + app module) — this supplies the matching Compose compiler.
   - Source fixes landed (Kotlin 2.2.10 / Compose ui 1.7.4 / M3 1.3.1): icons are extension properties needing per-icon imports (`androidx.compose.material.icons.filled.*`, `Icons.Default.X` — never fully-qualified or bare); `TextFieldDefaults.colors(...)` with focused/unfocused param names (no `textFieldColors`, no `textColor`/`containerColor`); `contentAlignment = Alignment.Center` in Box; `Modifier.weight` only inside Row/Column scope (moved to call sites via `modifier` param on CardioTypeButton/RestTimerPresetButton/PeriodButton/InfoPill); direct composable calls inside `LazyColumn {}` content must be wrapped in `item { }`; `SideEffect` block is not composable (hoisted `LocalContext.current` in Theme.kt); `VitalNavigation` uses `androidx.navigation.compose.NavHost/composable`; `GeminiClient`/`OpenRouterClient` parse fns use expression-body `= try {...} catch { null }`; `FoodAnalysisService` maps both clients' `FoodAnalysisResult` to `GeminiClient.FoodAnalysisResult`; `SeedLoader` imports `kotlinx.serialization.encodeToString`; `AuthModule` uses security-crypto 1.1.0 `EncryptedSharedPreferences.create(context, name, masterKey, ...)` with explicit `: SharedPreferences` return, unused BiometricPrompt provider deleted; `RestTimerForegroundService` implements `LifecycleOwner`; deleted unused `VitalAppContent` from MainActivity; `SummaryDial.Period` → top-level `com.vital.ui.components.Period`; Canvas code uses top-level `Stroke(w)`, `drawText(textMeasurer, ...)`, `PI.toFloat()/180f`, `Path.Direction.Clockwise`.
   - DEVICE TEST 2026-10-31 (OnePlus CPH2487 / OnePlus 11R, Android 16, `adb devices` = e281c7): `installDebug` OK; `am start -n com.vital/.ui.MainActivity` launches with **0 `AndroidRuntime:E` entries** after `adb logcat -c` — no crash. Screenshot shows TODAY tab rendering with sample data (top bar SAT/TODAY/OCT.31, orange hero card "KCAL REMAINING 850", P/C/F macro row, weekly graph, WORKOUTS capsules). Manifest fix required for launch: `android:name=".MainActivity"` → `".ui.MainActivity"` (class lives in `com.vital.ui`; the manifest `package` attr is ignored under AGP 9.5.0).
   - **Front end NOT complete vs reference**: current UI does not match `Notes/docs/frame-01..12.png` / `reference.png`. Known gaps visible in screenshot: hero-card graph is a clipped flat line at the card's bottom edge (reference has an inset grey graph panel with tooltip pill); capsule sparklines float without the reference's grey panel; Summary dial screen not yet compared on device; no bottom tab bar visible in launch screenshot. SPEC.md Section 9 steps 2-6 (canvas-vs-reference comparison, tab nav, meals/lift, cardio/progress/weeks/coach, polish) still open.
   - AuthActivity.kt issues listed in the previous revision did not appear in the final error set; compile is clean, but runtime behavior beyond launch is unverified.

3. **JDK Warning**
   - System Gradle 9.8.2 runs with Android Studio's embedded JDK 25
   - Kotlin warns: "Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_24 JVM target"
   - Build works but shows warning

---

## Stack

Kotlin, Jetpack Compose (Material3 + custom Canvas), single activity, MVVM, Hilt, Room with KSP, WorkManager, OkHttp + kotlinx.serialization, ML Kit text recognition (bundled model). No widgets (removed 2026-10-31). Debug build only.

## Package layout

com.vital: data (entity, dao, repository, seed), domain, ui (theme, components, screens), ai, sync, service, auth.

---

## Workflow rules

- Run ./gradlew assembleDebug after every step.
- Work in the 6 steps from SPEC.md section 9. After each step's build and tests, report every acceptance check in one line (write "NOT TESTED: <reason>" for anything you cannot run, never "passed"), then STOP and wait for the user to say "go".
- Never write a library or plugin version you did not see in a fetched result (use curl against Google Maven, Maven Central, GitHub releases). Mark each version in NOTES.md as "verified: <url>" or "UNVERIFIED".
- When a build fails, read the FULL error (expand hidden lines), fix only that error, and do not change unrelated versions, themes or plugins.
- Do not interrupt-loop: do not retry the same failed fix more than twice; explain and ask instead.

## Build rules (do not change without a verified reason)

- AGP 9.5.0, Gradle 9.6.0+ (system 9.8.2 works), Kotlin 2.2.10 (built-in), KSP 2.2.10-2.0.2, Hilt 2.60.1.
- Use `android.builtInKotlin=true` in gradle.properties (AGP 9.5.0 bundles Kotlin 2.2.10).
- Remove `org.jetbrains.kotlin.android` plugin from app build.gradle.kts.
- KSP version must match built-in Kotlin version prefix (2.2.10 -> 2.2.10-2.0.2).
- Room 2.8.5 works with KSP 2.2.10-2.0.2 (fixes "unexpected jvm signature V").
- gradle.properties: android.builtInKotlin=true, org.gradle.jvmargs=-Xmx4g, android.useAndroidX=true.
- Apply the kotlin-android plugin ONLY in root build.gradle.kts (not app).
- Use the compilerOptions DSL with .set() / .addAll(); never kotlinOptions, never = or += on compilerOptions properties.
- Hilt Gradle plugin id is com.google.dagger.hilt.android. hilt-navigation-compose and hilt-work use their own androidx.hilt 1.x versions, not the Dagger version.
- Do not use plain `= ` version pins in settings.gradle.kts resolutionStrategy; the version catalog (gradle/libs.versions.toml) is the single source of versions.
- No AppCompat. Use Material3 / Compose theme setup; keep themes.xml on Theme.MaterialComponents.DayNight.NoActionBar.
- For security-crypto with Hilt: inline MasterKey creation in provider to avoid KSP "NonExistentClass" error.

## Code rules

- All colors live in Theme.kt; no hex literals elsewhere.
- Room is the source of truth; the UI never waits on the network. Syncable entities carry id (UUID), updatedAt, deleted (tombstone).
- Today-screen day/date tags come from the system date.
- JSON export must exclude aiApiKeyEncrypted, pinHash; import must never overwrite aiApiKeyEncrypted, pinHash.
- Open Food Facts: send a User-Agent, use *_100g fields, skip products with missing macros.
- Verify any notification API (chronometer, foreground service types) against the installed android.jar before using it.
- AI API keys stored encrypted via Android Keystore / EncryptedSharedPreferences.
- Biometric authentication via BiometricPrompt with PIN fallback (argon2 hash).

## Diagnosis rules

- Read the full error before diagnosing. Use `./gradlew <task> 2>&1 | grep -A25 "What went wrong"` and show the lowest `Caused by:` line. Never act on a collapsed "+N lines" output.
- Do not guess at flags, plugins or version numbers from memory. Fetch the real doc or release note with curl, quote the exact line you rely on, then propose ONE change and wait for "go".
- Do not edit gradle.properties, libs.versions.toml, settings.gradle.kts or any build.gradle.kts without showing evidence and getting "go". Never make two build-file changes in one step, and never undo a fix you just made to test a theory.
- "assembleDebug passes" is not proof the code compiled. After any plugin or build-file change, confirm the compileDebugKotlin task ran and classes exist in the APK.
- A step is not complete until the app launches on the device without crashing. Run `adb logcat -c`, launch, then `adb logcat -d -s AndroidRuntime:E`. Report the result as a check; if it crashes, show the `Caused by:` lines before fixing.

## Version verification rules

- AGP and other Google artifacts: verify against Google Maven, `https://dl.google.com/dl/android/maven2/<group path>/<artifact>/maven-metadata.xml`. Everything else: Maven Central `maven-metadata.xml`. Mark each as "verified: <that URL>".
- A maven.google.com/web/index.html link does not count as verification (curl cannot read it). Re-verify any NOTES.md entry that uses one.
- Use stable releases only. Never pick an alpha, beta, rc or dev version unless explicitly asked.
- Do not filter a version search by the current version string (e.g., grep "2.2.20"). List the newest entries unfiltered with `sort -V | tail -20` so newer numbering schemes are visible.
- AGP is never changed by you. If a fix seems to need an AGP, Kotlin, KSP or Hilt version change, stop and give options with verified sources.
---

## Step 2 status (2026-11-01)

- **2A (dial): DONE** — dial face, inner dial, four tick tracks (RIM/BARS/MID/FINE) rendered from generated `DialTicks.kt`.
- **2B (knob, FAB, icons, text): DONE** — `ActivityGlyphs.kt` is now the single source for the five glyphs (Steps, Walk, Run, Bike, Moto). Deleted `DialIcons.kt`/`ActivityIcons.kt`. Knob fan (`drawKnurledKnob`) draws all five via `ActivityGlyphs.all.forEachIndexed` with transform `{ translate(pxCentreX, pxCentreY); scale(s, s, pivot=Offset.Zero); translate(-12f,-12f) }` and `VitalColors.ActivityFanIcon`. FAB (`drawFab`) draws `ActivityGlyphs.Walk.path` with same transform pattern and `VitalColors.FabIcon`. Fixed `.du()` @Composable calls to local `px()` helper in DrawScope.
- **2C (hero graph + sparklines): IN PROGRESS** — measurements received from `Activity.png` (393×855 frame, scale 1.675 px/du). Key findings vs draft:
  - Grey panel is **FLAT #5F6971** (not gradient), radius ~6, 6 du inset from card
  - Panel has **inner shadow** on top/left edges (~#424143 fading over ~7 du)
  - Two **guide lines** at 15% and 50% panel height (darker than panel)
  - Bottom labels: "6m ... 13m" with dashes, tiny ~5-6 du, at ~87% panel height
  - End dot at 85.6% panel width, 53% panel height; polyline STOPS at dot
  - Tooltip pill centred on guide line 1 & end dot x; thin outline; text "111BMP"
  - Capsule rows: 90.7 du height, half-height radius, #0D0D0D fill, darker outer ring ~6.5 du, light rim line at bottom
  - Thumbnail: 108.7×73 du, flat #5F6971; bottom ruler shows **numbers (960/980/990)**, active dark, NOT "MIN AVG MAX"
  - Hero card radius ~11 (not 16), "MOVE" label mono ~21, time font ~83, superscript ~29
- **Verified**: `assembleDebug` ✅, `installDebug` ✅ on OnePlus 11R, launch 0 `AndroidRuntime:E`, `tmp/step2c.png` captured.
- **Next**: implement measured values in `Color.kt`, `Graphs.kt`, `Components.kt`, `TodayScreen.kt`.

## Compose Canvas facts (Compose UI 1.7.4 / BOM 2024.10.00, learned on 2026-11-01)

- Unresolved references in Kotlin source are import or API mistakes, not dependency problems. Never change plugin versions, BOM, or dependencies to fix them.
- DrawScope member functions (drawRect, drawRoundRect, drawCircle, drawLine, drawPath, drawArc, drawOval, drawPoints) are NOT imported. Inside `Canvas { }` or `fun DrawScope.x()` call them directly. Never write `import ...drawscope.drawCircle` and so on. In a helper, make it an extension: `fun DrawScope.drawFoo(...)` and call `drawFoo(...)` without passing `this`.
- `provides` is an infix member of ProvidableCompositionLocal: `LocalFoo provides value`. No import, and never the function-call form `provides(a, b)`.
- Correct packages:
  - androidx.compose.ui.geometry: Offset, Size, Rect, RoundRect, CornerRadius
  - androidx.compose.ui.graphics: Color, Brush, Path, Paint, StrokeCap, StrokeJoin, PathEffect, BlendMode
  - androidx.compose.ui.graphics.drawscope: DrawScope, Stroke, Fill, drawIntoCanvas, rotate, translate, withTransform
  - androidx.compose.ui.text: rememberTextMeasurer, TextMeasurer, TextStyle, drawText
- `to` (Pair), `Stroke` as a drawStyle, and `Path.Direction.Clockwise` are all valid; if they "do not resolve", an earlier error in the file is the cause.
- When a build fails: `./gradlew compileDebugKotlin 2>&1 | grep "^e: " | head -10`. Fix only the FIRST error, rebuild, repeat. Do not rewrite a working component to avoid an import.
- Dial data: app/src/main/java/com/vital/ui/components/DialTicks.kt is GENERATED from the measured prototype. Never hand edit it. Its hex colours are measured data, the only allowed exception to the Theme.kt colour rule. Design units: `ui/theme/DesignUnits.kt` (393 x 855 frame, scale = screenWidthDp / 393).
- Do not read Notes/old/ACTIVITY_SCREEN_BRIEF.md or Notes/old/ANDROID_PORT_BRIEF.md (superseded by Notes/STEP2_PROMPT.md).
