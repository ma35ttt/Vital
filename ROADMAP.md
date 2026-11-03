# Vital: roadmap and exit checks

Order of work. Each milestone has exit checks; do not start the next one until the exit checks pass and the user says "go".
Every step ends with: build green, app launches (no `AndroidRuntime:E`), screenshot where relevant, user commits and tags.
New ideas go to `TWEAKS.md` (parking lot). They are reviewed once the daily-use milestone (M5) is reached, never mid-step.

Realistic time (3 to 4 hours a day): M0 to M5 about a week; M6 and M7 another one to two weeks.

## M0. Stable ground (day 1)
User, before any code:
1. Put `Summary.png` and `Activity.png` in `design/ref/` (the audit found `Notes/docs/` missing). Export the mockup boards to `design/tabs/` (`meals.png`, `lift.png`, `cardio.png`, `weeks.png`, `settings-top.png`, `settings-scrolled.png`, `quickhold.png`, `snap-treadmill.png`, `snap-meal.png`).
2. Replace CLAUDE.md, NOTES.md, SPEC.md, ROADMAP.md, FIXES_AND_GESTURES_BRIEF.md with the new versions. Commit: `docs: roadmap and rules`, tag `m0-docs`.
3. Move the three big archives out of the repo (`fud-ai-android-v7.2.tar.gz`, `openGym-1.4.3.tar.gz`, `vital-ui.zip`) to `~/Vital-archive/`. Do not delete them.

Batch 1, housekeeping (one "go"): `tmp/cleanup.sh` GROUP 1 only; add `.kotlin/`, `window.xml`, `tools/`, `*.textClipping` to `.gitignore`. Verify: `git status --short`, build, launch.
Batch 2, fonts only (one "go"): rename `jetbrainsmono_light.ttf` to `jetbrains_mono_300.ttf` (we need weight 300 for the Weeks number), then delete the other duplicate `jetbrainsmono_*` files; remove the five `spacegrotesk_*.ttf` and the `SpaceGrotesk*` lines in Fonts.kt / Type.kt; use Inter Tight (`inter_tight_400/500/600/700.ttf` already exist) and JetBrains Mono (`jetbrains_mono_300/400/500/700`). Keep DebugShowcaseScreen.kt and Previews.kt: M2 uses them. Do NOT touch Color.kt or strings.xml now (see M7).
Batch 3, build hygiene (user removes the deny lines for this batch):
- 3a. Catalog: move every literal dependency version in `app/build.gradle.kts` into `libs.versions.toml` and use catalog references. Add the missing `hilt-lifecycle-viewmodel` entry only if the artifact is verified on Google Maven (UNVERIFIED so far; if it is not needed, remove it). Remove `ksp(libs.androidx.security.crypto)`. Remove the `resolutionStrategy { eachPlugin }` block in settings.gradle.kts. Delete the stale commented block in gradle.properties and `android.enableArtifactTransforms`. Keep the Gradle wrapper at 9.6.0 and fix the docs, not the wrapper.
- 3b. Safety and speed: Room schema export (`ksp { arg("room.schemaLocation", "$projectDir/schemas") }`), `ui-tooling` and `ui-test-manifest` as `debugImplementation`, a `fast` build type (`initWith(debug)`, `isDebuggable = false`, debug signing, minify off) for speed tests, JUnit5/JUnit4 test setup only if tests need it.
- 3c. Manifest: remove `package=`, add `USE_BIOMETRIC` (drop `USE_FINGERPRINT`, never needed), boot receiver `exported="false"`, review `allowBackup` and the backup rules so EncryptedSharedPreferences are excluded.
- 3d. Biometric: `1.2.0-alpha04` is an alpha. The auth code does not exist yet; verify whether a stable version has what we need (`biometric-ktx` may have no stable release). Decide when the auth gate is built (M6), not now.
- Not in M0: any other version bump, a newer Compose BOM, baseline profile, CameraX. (The audit says the newest BOM needs a newer Kotlin than the built-in 2.2.10: verify before trying it in M6.)
Exit: build green, `compileDebugKotlin` ran, install, launch, 0 `AndroidRuntime:E`, tag `m0`.

## M1. Measure speed (no code)
Run the commands in FIXES_AND_GESTURES_BRIEF.md section 6 on both the debug build and the `fast` build. Send the numbers. This tells us whether the lag is the dial/shadows or just the debug build.

## M2. Design system, once
- F2 scaffold: top offset, hero label/value wiring, text-clip pattern, TRUE BLACK default ON and wired to Settings (remove the `isTrueBlackEnabled() = false` stub), system-date chips.
- F3 nav and QUICK ADD bottom sheet.
- F4 `OrangeDial` with cached drawing (static layers rendered once), letters, inner disc, FAB. Shown on `DebugShowcaseScreen` (add a debug-only route).
Exit: showcase screen renders scaffold, nav and dial; user screenshot measured numerically; scrolling the showcase feels smooth on the phone.

## M3. Logic with unit tests (no UI)
Smart-text parser (incl. RAW/COOKED ambiguity), plate calculator (80 kg = 25 + 5; 100 kg = 25 + 15; nearest loadable), ACSM (6 km/h, 2%, 68 kg, 32:10 = 187 kcal), recipe splitter, adherence % (L 7 days, M 4 weeks, H 12 weeks), kcal totals and macro share, rest timer state, weekly volume, estimated 1RM, PR detection.
Exit: `./gradlew testDebugUnitTest` green, each function has a test with the numbers above.

## M4. Tabs, end to end (UI + ViewModel + Room), one at a time
Order: Settings, Meals, Lift, Cardio (manual entry), Weeks, then Today leftovers (F tooltip artefact, F macro column, bottom padding).
Add date pickers for back-logging (log or edit a past day) in Meals and Lift: get the mockup from Claude chat first.
Each tab is done when: matches its `design/tabs/<tab>.md` numbers (user measures), empty state works, survives rotation and tab switch, data persists after app restart, no crash on empty tables, `tmp/<tab>.png` reviewed by Claude Code.
Remove `// TEMP sample data` blocks as each tab gets real data.

## M5. Daily-use checkpoint
Use the app for 2 to 3 days. Log real meals, sets, cardio, weight. Put everything you notice in `TWEAKS.md`. Review the list, batch it, design anything that changes a screen in Claude chat first. Tag `daily-use`.

## M6. Gestures, speed, background features
Speed fixes (FIXES brief section 6, items 1 to 4), hold menu on `+`, swipe between tabs (pager; show the NavHost plan and wait for "go"), notifications and reminders (permission state, exact-alarm check), rest timer foreground service, auth gate (PIN + biometric; none exists today), JSON export/import. Optional: newer Compose BOM speed test on its own.

## M7. Extras and final cleanup
Snap flow (treadmill OCR, food photo), AI coach, photo analysis, quality-of-life items (FIXES brief section 7), v1.1 candidates in SPEC section 11. Final cleanup: remove unused colour tokens and strings (only after every screen uses the new tokens; never one Today or Summary uses), delete the debug route if no longer needed, update NOTES.md.

## Rules for every step
- One step per Claude Code session. Start fresh, give the step text, stop at the end.
- Claude Code reviews its own screenshot against the design PNG before it stops, and never writes "matches".
- User sends device screenshots to Claude chat at milestones for numeric measurement, not after every tweak.
- Tag after every step. If a step goes wrong, go back to the last tag instead of rewriting.
