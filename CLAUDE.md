# Vital

Local-first Android app (2 users, OnePlus 11R, Android 14+, minSdk 28, compile/target SDK 36). Requirements: SPEC.md. Order of work and exit checks: ROADMAP.md.
Versions, decisions, open questions and history: NOTES.md. Keep this file short: rules and current state only. History goes to NOTES.md.

## DESIGN SOURCE OF TRUTH
- Summary and Activity: pixel references `design/ref/Summary.png` and `design/ref/Activity.png` (tracked in git). Their measured numbers win over any number in this file. If either file is missing, stop and tell the user.
- All other tabs (Meals, Lift, Cardio, Weeks, Settings), the shared chrome and the new gestures: `design/tabs/<screen>.md` (measured numbers) plus `REMAINING_TABS_BRIEF.md` and `FIXES_AND_GESTURES_BRIEF.md` (repo root). If `design/tabs/<screen>.md` is missing, stop and ask before building that screen.
- One accent, orange `#FD3F00`, plus black `#000000` and the grey data panel `#5F6971`. No per-tab accents.
- Where files disagree: `design/tabs/*.md` wins on look, the briefs win on behaviour, SPEC.md wins on data and logic.
- `Notes/old/` and every earlier brief are obsolete. Do not read them.

## Images
- You can view images with the Read tool. After each build, look at `tmp/<screen>.png` and the matching `design/tabs/<screen>.png` and list every visible difference (missing, clipped, overlapping, wrong text, wrong position). Build from the measured numbers; the pictures are for catching obvious mistakes.
- Never write that a screen "matches" a reference. Write `NOT TESTED: visual comparison` and stop. The user measures the device screenshot with Claude chat and sends numeric differences.
- The user takes device screenshots by hand (adb or the phone) and sends them. If you cannot capture one, write `NOT TESTED: screenshot (taken by user)` and carry on.

## Current state (2026-11-03). Details and history in NOTES.md
- Builds, installs and launches on the OnePlus 11R. Today and Summary are done to the references except the open Today items in NOTES.md.
- Meals, Lift, Cardio, Weeks, Settings exist on a first chrome pass with many defects (list: FIXES_AND_GESTURES_BRIEF.md section 1).
- Not built yet: auth gate (no code found by the 2026-11-03 audit), hold menu on `+`, swipe between tabs, snap (photo) flow, speed fixes, tab data logic.
- Widgets are REMOVED (2026-10-31). Do not add Glance.

## Stack
Kotlin 2.2.10 (built-in with AGP 9.5.0), Jetpack Compose (Material3 + custom Canvas), single activity, MVVM, Hilt, Room with KSP, WorkManager, OkHttp + kotlinx.serialization, ML Kit text recognition (bundled model). Debug build today; a not-debuggable `fast` build type for speed tests is planned (ROADMAP M0).
Package layout: com.vital: data (entity, dao, repository, seed), domain, ui (theme, components, screens), ai, sync, service, auth. Design files: `design/ref/` and `design/tabs/`.

## Workflow rules
- Work one step at a time (ROADMAP.md). After each step: build, install, launch check, then STOP and wait for the user to say "go". Report every check in one line; write `NOT TESTED: <reason>` for anything you did not run, never "passed".
- Run `./gradlew assembleDebug` after every step, then `./gradlew --stop`. Never run builds in a loop or in the background. If any command fails with `fork failed`, stop and tell the user.
- Manual (approve-each-edit) mode. Do exactly the step you were given. Do not commit unless the step says so. After a step the user runs: `git add -A app NOTES.md && git commit -m "<step> done" && git tag <tag>`.
- A step is not done until the app launches without a crash: `adb logcat -c`, launch, `adb logcat -d -s AndroidRuntime:E`. If it crashes, show the `Caused by:` lines before fixing.
- Temporary sample data used only to see a component on the device is allowed in one block marked `// TEMP sample data, remove in M4`. Components take data as parameters; no sample data inside components.
- Do not start the next step, and do not edit files a step does not name. If you want to change another file, stop and ask.

## Build files (loosened 2026-11-03)
- AGP, Kotlin, KSP and Hilt versions are FROZEN. Never change them. If a fix seems to need one, stop and give options with verified sources.
- Other build-file edits (catalog hygiene, dependency fixes, build types, manifest, `gradle.properties`) are allowed ONLY inside an approved batch from ROADMAP M0, with the user's "go" for that batch. One batch = one group of related changes; never mix unrelated version changes.
- `.claude/settings.json` denies Edit/Write on the build files. The user removes those deny lines for the duration of an approved batch and restores them after.
- After every build-file change: confirm `compileDebugKotlin` actually ran and classes are in the APK. `assembleDebug` passing alone is not proof.
- Check after every step that `git diff --stat -- '*.gradle.kts' gradle.properties gradle/ app/src/main/AndroidManifest.xml` shows only what the approved batch allows (empty outside a batch).

## Build rules (do not change without a verified reason)
- AGP 9.5.0, Gradle wrapper 9.6.0 (AGP minimum; do not bump), Kotlin 2.2.10 (built-in), KSP 2.2.10-2.0.2, Hilt 2.60.1.
- `android.builtInKotlin=true` in gradle.properties. No `org.jetbrains.kotlin.android` plugin. Compose compiler comes from `org.jetbrains.kotlin.plugin.compose` 2.2.10; never use `composeOptions`.
- KSP version must match the Kotlin version prefix. Room 2.8.5 works with KSP 2.2.10-2.0.2.
- Use the compilerOptions DSL with `.set()` / `.addAll()`; never `kotlinOptions`.
- Hilt Gradle plugin id is `com.google.dagger.hilt.android`. `hilt-navigation-compose` and `hilt-work` use their own androidx.hilt versions, not the Dagger version.
- `gradle/libs.versions.toml` is meant to be the single source of versions. Today most dependencies are literals in `app/build.gradle.kts`; ROADMAP M0 batch 3 fixes that. Until then do not add a new dependency without asking.
- No AppCompat. Keep themes.xml on `Theme.MaterialComponents.DayNight.NoActionBar`.
- For security-crypto with Hilt: create the MasterKey inline in the provider (avoids KSP "NonExistentClass").

## Code rules
- All colours live in `ui/theme` (Color.kt / Theme.kt). No hex literals elsewhere (exception: generated `DialTicks.kt`). Do not remove a colour token until the ROADMAP M7 cleanup, and never one that Today or Summary uses: new-design tokens have zero references until the tabs use them.
- Positions and sizes in du (393 x 855 frame, `ui/theme/DesignUnits.kt`, 1 du = screenWidthDp / 393). On the OnePlus 11R 1 du = 3.155 px and the screen is 878.5 du tall: anchor the header and sheet to the top, the nav to the bottom.
- Text sizes `(du * scale).dp.toSp()` (ignores system font scale). Never `.sp` with a du value.
- Room is the source of truth; the UI never waits on the network. Syncable entities carry id (UUID), updatedAt, deleted (tombstone). Export the Room schema (batch 3) before changing entities; no destructive migrations.
- Day/date tags (chips) on every tab come from the system date.
- JSON export must exclude aiApiKeyEncrypted and pinHash; import must never overwrite them. Open Food Facts: send a User-Agent, use `*_100g` fields, skip products with missing macros.
- Verify any notification API (chronometer, foreground service types) against the installed android.jar. `SCHEDULE_EXACT_ALARM` is not granted by default on Android 14+: check `canScheduleExactAlarms()` before scheduling.
- AI API keys: encrypted via Android Keystore / EncryptedSharedPreferences. Biometric: BiometricPrompt with PIN fallback.
- Icons: stroke ImageVectors (1.8 du, round caps). Existing Material icons are replaced as screens are rebuilt (REMAINING_TABS_BRIEF.md rule 2).
- No `// TODO`, no stubs. If something cannot be finished, write it in NOTES.md.

## Diagnosis rules
- Read the full error before diagnosing: `./gradlew <task> 2>&1 | grep -A25 "What went wrong"`, show the lowest `Caused by:`. Never act on a collapsed "+N lines" output.
- Compile errors: `./gradlew compileDebugKotlin 2>&1 | grep "^e: " | head -10`. Fix only the FIRST error, rebuild, repeat. Max 2 attempts on the same error, then stop and show the full "What went wrong" block.
- Do not guess at flags, plugins or version numbers from memory. Fetch the real doc or release note, quote the line you rely on, propose ONE change and wait for "go".
- Never undo a fix you just made to test a theory.

## Version verification rules
- Google artifacts (AGP, AndroidX, Material): `https://dl.google.com/dl/android/maven2/<group path>/<artifact>/maven-metadata.xml`. Everything else: Maven Central `maven-metadata.xml`. Mark each in NOTES.md as "verified: <that URL>" or "UNVERIFIED".
- A maven.google.com/web/index.html link does not count (curl cannot read it).
- Stable releases only. Never an alpha, beta, rc or dev version unless explicitly asked.
- List the newest entries unfiltered (`sort -V | tail -20`); do not filter by the current version string.

## Compose Canvas facts (Compose UI 1.7.4 / BOM 2024.10.00)
- Unresolved references in Kotlin source are import or API mistakes, not dependency problems. Never change plugin versions, BOM or dependencies to fix them.
- DrawScope members (drawRect, drawRoundRect, drawCircle, drawLine, drawPath, drawArc, drawOval, drawPoints) are NOT imported. Inside `Canvas { }` or `fun DrawScope.x()` call them directly. In a helper, make it an extension `fun DrawScope.drawFoo(...)` and call it without passing `this`.
- `provides` is an infix member of ProvidableCompositionLocal: `LocalFoo provides value`. No import, never the function-call form.
- Packages: `androidx.compose.ui.geometry` (Offset, Size, Rect, RoundRect, CornerRadius); `androidx.compose.ui.graphics` (Color, Brush, Path, Paint, StrokeCap, StrokeJoin, PathEffect, BlendMode); `androidx.compose.ui.graphics.drawscope` (DrawScope, Stroke, Fill, drawIntoCanvas, rotate, translate, withTransform); `androidx.compose.ui.text` (rememberTextMeasurer, TextMeasurer, TextStyle, drawText).
- `verticalScroll` and `rememberScrollState` live in `androidx.compose.foundation` (not `foundation.gestures`).
- `Cannot access constructor ... TextStyle: it is internal` is a side effect of an argument type mismatch (usually `fontSize` given a Float instead of a TextUnit). Fix the argument; do not rewrite the component.
- Dial data: `app/src/main/java/com/vital/ui/components/DialTicks.kt` is GENERATED from the measured prototype (regeneration master: `Notes/DialTicks.kt`). Never hand edit it.
- Never read `Notes/docs/frame-*.png` or `motion-notes.md` until the animation step.
