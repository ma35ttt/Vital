# Vital — Read-Only Audit (2026-11-03)

---

## 1. Files Read

### Markdown files (all read in full)
- CLAUDE.md (153 lines — in system context; repo copy matches)
- SPEC.md (327 lines)
- NOTES.md (164 lines)
- REMAINING_TABS_BRIEF.md (225 lines)
- Notes/STEP2_PROMPT.md (70 lines)
- Notes/vital-ui/PROTOTYPE_NOTES.md (18 lines)
- Notes/vital-ui/README.md (12 lines)
- design/tabs/chrome.md (73 lines)
- design/tabs/meals.md (53 lines)
- design/tabs/lift.md (55 lines)
- design/tabs/cardio.md (47 lines)
- design/tabs/weeks.md (70 lines)
- design/tabs/settings.md (66 lines)

### Build & config files (all read in full)
- build.gradle.kts (root)
- app/build.gradle.kts
- settings.gradle.kts
- gradle.properties
- gradle/libs.versions.toml
- gradle/wrapper/gradle-wrapper.properties
- app/src/main/AndroidManifest.xml
- app/proguard-rules.pro
- .claude/settings.json

### No .claude/settings.local.json, no FIXES_AND_GESTURES_BRIEF.md

### Inventoried (names/sizes/paths only — not read)
- All .kt/.java sources (git ls-files output above)
- app/src/main/res/font/ (19 .ttf files)
- app/src/main/assets/ (exercises.json, seed_foods.json)
- tmp/ (13 .png + build.log + errors.txt + phone-shots/)
- design/tabs/ (7 .md + 7 .png + docs/)
- Notes/ (STEP2_PROMPT, PROTOTYPE_NOTES, README, 3 large .tar.gz, vital-ui/, DialTicks.kt, .DS_Store)
- scripts/ (build-icons.mjs)
- assets/icons/ (master, android, ios, macos, web)
- tools/ (Wroking build.textClipping)

### Unreadable / Missing
- **Notes/docs/** — `ls Notes/docs` → `No such file or directory`. CLAUDE.md, SPEC.md, STEP2_PROMPT all reference `Notes/docs/Summary.png` and `Notes/docs/Activity.png` as the **only** pixel references. The directory does not exist on disk (Notes/ is gitignored so was never committed). This is a blocker for Step 2 acceptance.
- design/tabs/docs/ — exists but contains only .DS_Store; the PNGs referenced by STEP2_PROMPT (`reference.png`, `activity-reference.png`) are absent.

---

## 2. Contradictions (md vs md, or md vs build files)

| # | File A (line) | File B (line) | Contradiction |
|---|---------------|---------------|---------------|
| 1 | CLAUDE.md:14–15 (`Notes/docs/Summary.png`, `Notes/docs/Activity.png` are pixel references) | `ls Notes/docs` → **missing** | Pixel references do not exist on disk. |
| 2 | CLAUDE.md:Current Build State table "Gradle 9.8.2 (system)" | gradle-wrapper.properties:3 `distributionUrl=...gradle-9.6.0-bin.zip` | Wrapper downloads 9.6.0 while system is 9.8.2. AGP 9.5.0 requires ≥9.6.0 so 9.6.0 works, but mismatch is confusing. |
| 3 | CLAUDE.md:Build rules "version catalog is the single source of versions" | settings.gradle.kts:10 `useModule("com.google.dagger:hilt-android-gradle-plugin:2.60.1")` — hardcoded literal | Violates own rule; also duplicated in root build.gradle.kts plugins block. |
| 4 | CLAUDE.md:Build rules "catalog is the single source" | app/build.gradle.kts:60 `implementation("androidx.core:core-ktx:1.15.0")` + ~19 other literals (core-ktx, lifecycle×4, activity, constraintlayout, material 1.12.0, hilt-android, hilt-work, hilt-navigation-compose, **hilt-lifecycle-viewmodel:1.4.0 literal**, work, biometric α, okhttp, serialization, coroutines, datetime, coil, mlkit, testing) while catalog entries for most exist unused (e.g. `libs.androidx-core-ktx`, `libs.androidx-lifecycle-runtime-ktx`, etc.) | Build file hygiene ignored; catalog is decorative. |
| 5 | CLAUDE.md:Build rules "stable releases only" | app/build.gradle.kts:100–101 `biometric:1.2.0-alpha04` + `biometric-ktx:1.2.0-alpha04` | Alpha used; newest stable is 1.1.0 (Maven metadata). |
| 6 | SPEC.md:2 (Widgets REMOVED) + REMAINING_TABS_BRIEF.md 0A "Omit WIDGETS section" | colors.xml:3 `<color name="widget_background">#1E1E1E</color>` + strings.xml:135–136 `widgets`, `configure_widgets` + SettingsScreen.kt:602–604 commented WIDGETS block | Dead widget tokens remain. |
| 7 | SPEC.md:1 Fonts "Space Grotesk is dropped" + REMAINING_TABS_BRIEF.md 0A "Inter Tight + JetBrains Mono" | Fonts.kt:18–22 defines 5 `SpaceGrotesk*` FontFamilies; Type.kt:13 `SpaceGrotesk = FontFamily.Default // Placeholder`; 5 spacegrotesk_*.ttf files on disk (all **identical** shasum `dc03dcd…`) | Dropped font still wired and duplicated 5×. |
| 8 | SPEC.md:2 Table lists `WidgetConfig` entity | git ls-files / grep: **no WidgetConfig entity** exists in code | Spec stale. |
| 9 | SPEC.md:7 manifest permissions table rows `USE_BIOMETRIC`, `USE_FINGERPRINT` | AndroidManifest.xml: **neither permission present** | Auth gate missing. |
| 10 | SPEC.md:1 auth "Simple local PIN / biometric" + Step 1 acceptance "shows auth screen" | grep: **zero** `AuthActivity` / `BiometricPrompt` references anywhere; manifest has no auth activity | Auth screen absent. |
| 11 | CLAUDE.md:Step 2 "Knob drag rotates & updates value live at 60 fps" | Theme.kt:126–128 `isTrueBlackEnabled()` hardcoded `return false // for now` | TRUE BLACK setting from Settings not wired; stub function. |
| 12 | REMAINING_TABS_BRIEF.md rule 2 "Icons are stroke ImageVectors. No Material icons" | CLAUDE.md notes "icons are extension properties needing per-icon imports (`androidx.compose.material.icons.filled.*`, `Icons.Default.X`)" — current code uses Material icons | Brief vs current code disagree. |
| 13 | NOTES.md:89 "Inter Tight font files bundled in res/font (OFL); not yet added" | app/src/main/res/font/ has `inter_tight_400/500/600/700.ttf` (added 2026-10-18) | NOTES.md stale. |
| 14 | CLAUDE.md:2C measurements table (Activity.png) vs STEP2_PROMPT references `Notes/docs/reference.png` (Summary) + `Notes/docs/activity-reference.png` | Neither `reference.png` nor `activity-reference.png` exist in Notes/docs (missing dir) | Step 2 references mismatch. |
| 15 | .gitignore lists `screen_*.png`, `current_ui*.png`, `.DS_Store` | All those files are **tracked** in git (git ls-files shows them) | .gitignore doesn't untrack already-committed files. |
| 16 | gradle.properties:24–39 commented block `agp=9.4.0`, `kotlin=2.2.20`, `ksp=2.2.20-2.0.4` | libs.versions.toml has `agp=9.5.0`, `kotlin=2.2.10`, `ksp=2.2.10-2.0.2` | Stale commented versions contradict catalog. |

---

## 3. Build File Problems

| File | Lines | Problem | Evidence |
|------|-------|---------|----------|
| app/build.gradle.kts | 58–116 | ~20 literal dependency versions instead of catalog refs; catalog entries exist but unused | `implementation("androidx.core:core-ktx:1.15.0")` vs `libs.androidx-core-ktx`; same for lifecycle, activity, constraintlayout, material, hilt-android, hilt-work, hilt-nav-compose, work, biometric, okhttp, serialization, coroutines, datetime, coil, mlkit, testing. |
| app/build.gradle.kts | 93 | `implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.4.0")` literal — no catalog entry | Catalog has hiltWorker, hiltNavigationCompose but no hiltLifecycleViewmodel. |
| app/build.gradle.kts | 105 | `ksp(libs.androidx.security.crypto)` — KSP processor on a runtime library (no KSP annotation processor) | security-crypto 1.1.0 is a runtime artifact; running KSP on it is wasteful and may break. |
| settings.gradle.kts | 7–13 | `resolutionStrategy { eachPlugin { if (id == "com.google.dagger.hilt.android") useModule("…:2.60.1") } }` hardcodes version, duplicates root plugins block, violates "catalog single source" rule | Root build.gradle.kts:5 already pins `id("com.google.dagger.hilt.android") version "2.60.1" apply false`. |
| gradle.properties | 7 | `android.enableArtifactTransforms=true` — deprecated/removed in AGP 9; ignored but fragile | AGP 9 release notes removed artifact transforms flag. |
| gradle.properties | 24–39 | Stale commented version block contradicts libs.versions.toml | See Contradiction #16. |
| gradle-wrapper.properties | 3 | `distributionUrl=...gradle-9.6.0-bin.zip` while system gradle is 9.8.2 and CLAUDE.md documents 9.8.2 | Minor; 9.6.0 satisfies AGP 9.5.0 minimum. |
| gradle-wrapper.properties | 7 | `validateDistributionUrl=false` — disables checksum verification | Security/integrity minor issue. |
| Root build.gradle.kts | 2–6 | Plugins applied `apply false` — correct. However `composeOptions` block was removed (per CLAUDE.md 2026-10-31 fix) — good. | N/A — already fixed. |
| Compose BOM / Compiler | — | BOM 2024.10.00 + Kotlin 2.2.10 built-in + compose compiler plugin 2.2.10 — consistent triplet. Newest BOM 2026.09.00 exists but would require Kotlin bump (blocked by AGP). | Verified via Google Maven metadata. |
| JDK/runtime | — | System Gradle runs on Android Studio embedded JDK 25; Kotlin warns "falling back to Kotlin JVM_24" | Build works but warning noise. |

---

## 4. Rules That Cost Us (CLAUDE.md, SPEC.md, NOTES.md, briefs, .claude/settings.json)

| Rule | Protects | Hurts | Keep / Loosen / Remove | Reason |
|------|----------|-------|------------------------|--------|
| Build files frozen (deny Edit/Write on *.gradle.kts, gradle.properties, libs.versions.toml, wrapper, AndroidManifest.xml) | Prevents accidental version drift; ensures reproducibility | Blocks catalog hygiene (Batch 3), wrapper upgrade, manifest auth permissions, ksp(security-crypto) removal | **Loosen**: allow hygiene edits (catalog-ify literals, remove duplicate pin, remove wrong ksp) with evidence + "go". Keep freeze on AGP/Kotlin/KSP/Hilt versions. |
| .claude/settings.json deny list on build files | Same as above | Same as above | **Loosen** per above. |
| "Never change AGP, Kotlin, KSP, Hilt version without verified source + options" | Prevents breaking the build triplet | Good — keep as-is. | **Keep**. |
| "Max 2 attempts on a failed fix, then ask" | Prevents rabbit holes | Can stall if error is subtle but fixable in 3rd try | **Keep** — reasonable. |
| "Run ./gradlew assembleDebug after every step" | Catches regressions early | Slow (2–3 min each); blocks parallel work | **Keep** — safety critical. |
| "Manual (approve-each-edit) mode" | Human control | Slows iteration | **Keep** — user preference. |
| ".gitignore Notes/" (and Notes/*.tar.gz) | Keeps large archives out of repo | **Lost the pixel references** (Summary.png, Activity.png) that CLAUDE.md/SPEC say are the design source of truth | **Remove Notes/ from .gitignore** (or at least allow Notes/docs/*.png) so references can be committed. |
| "Do not read Notes/old" | Avoids stale context | Good — keep. | **Keep**. |
| "Write NOT TESTED: visual comparison instead of 'passed'" | Honest reporting | Forces user to measure screenshots; slows acceptance | **Keep** — correct process. |
| "One accent only (#FD3F00)" | Visual consistency | Good — keep. | **Keep**. |
| "All colors in Theme.kt; no hex elsewhere" | Centralised theming | Good — keep. | **Keep**. |

---

## 5. Planned Work → Build File / Manifest Needs

| Need | Required? | Exact Change | Risk |
|------|-----------|--------------|------|
| Not-debuggable build type for speed tests | Optional (perf only) | Add `benchmark { initWith debug; isDebuggable = false; isMinifyEnabled = false }` in app/build.gradle.kts buildTypes | Low; build-file change needs unfreeze. |
| Baseline Profile (startup smoothness, knob 60fps) | Recommended | Add `androidx.profileinstaller:profileinstaller` dep + `baselineprofile` Gradle plugin + generate profile | Medium; new dep + plugin, build-file changes. |
| CameraX (in-app viewfinder) | **Only if user approves** | Add `androidx.camera:camera-core/viewfinder` deps + camera permissions (already has CAMERA) | Medium; increases APK size. Current plan: system intent / MediaStore → **not needed**. |
| FileProvider | Not needed (MediaStore) | — | — |
| Inter Tight + JetBrains Mono in res/font | **Already present** | Just dedup (remove spacegrotesk, merge jetbrainsmono sets) | Low; code edits only. |
| Compose foundation HorizontalPager | Already available | BOM 2024.10.00 includes foundation 1.7.x which has stable `HorizontalPager` | No change needed. |
| ML Kit Barcode | Optional (not required) | Add `com.google.mlkit:barcode-scanning` if wanted later | Low. |

---

## 6. Files to Delete or Move (Step C Table)

### Group 1 — YES (safe, verified by grep/shasum in audit)

| Path | Why | Evidence | Risk | Verdict |
|------|-----|----------|------|---------|
| window.xml | uiautomator dump at repo root | `head -20 window.xml` shows hierarchy dump | None | YES |
| tools/Wroking build.textClipping | macOS TextClipping containing `git checkout 9ac3a0a -- .` + `./gradlew installDebug` (destructive if pasted) | `cat "tools/Wroking build.textClipping"` output | None | YES |
| .kotlin/ (6 tracked files: .DS_Store + 5 errors-*.log) | Build junk, recreated by Gradle | `git ls-files .kotlin` → 6 files | None | YES |
| app/.DS_Store, app/src/.DS_Store, app/src/main/.DS_Store, assets/.DS_Store, assets/icons/.DS_Store, assets/icons/android/.DS_Store, gradle/.DS_Store | Tracked .DS_Store (gitignore has .DS_Store but they were committed) | `git ls-files | grep DS_Store` → 8 files | None | YES |
| current_ui.png, current_ui2.png, screen_awake.png, screen_fixed_today.png, screen_focused.png, screen_pulled.png, screen_raw.png, screen_summary.png, screen_summary2.png, screen_summary3.png, screen_summary4.png | Old device screenshots, gitignored patterns but tracked | `git ls-files | grep -E "screen_|current_ui"` → 11 files | None | YES |
| app/src/main/res/values/colors.xml | Only contains `widget_background` (widgets removed, 0 refs in Kotlin) | `cat colors.xml` + `grep -rn "widget_background" app/src/main/java` → 0 | None | YES |
| tmp/*.png, tmp/build.log, tmp/errors.txt | Old session artifacts (tmp/today1.png == today2.png identical shasum) | `shasum tmp/today1.png tmp/today2.png` → same; tmp is gitignored | None | YES |
| tmp/phone-shots/ | Old adb screenshot dump | `ls tmp/phone-shots/Screenshots` | None | YES |
| design/tabs/docs/ (empty dir + .DS_Store) | Empty, untracked | `ls design/tabs/docs/` → only .DS_Store | None | YES |

**Totals: 27 tracked files (git rm) + ~17 untracked paths (rm).**

### Group 2 — CHECK (needs paired code edit or user confirmation)

| Path | Why | Evidence | Risk | Verdict |
|------|-----|----------|------|---------|
| app/src/main/res/font/spacegrotesk_{regular,medium,semibold,bold,light}.ttf (5 files) | SPEC says dropped; all 5 have **identical shasum** `dc03dcd…` (one file copied 5×) | `shasum …/spacegrotesk_*.ttf` | Fonts.kt:18–22 & Type.kt:13 reference them; must edit code first | CHECK |
| app/src/main/res/font/jetbrainsmono_{regular,light,medium,semibold,bold}.ttf (5 files) + jetbrainsmono_bold.ttf (6 total) | Duplicate JetBrains set; TodayScreen/Components use `jetbrains_mono_400/500/700` | `grep R.font.jetbrains` → 4 refs to `jetbrains_mono_*`; 0 to `jetbrainsmono_*` | Fonts.kt:11–15 must switch first | CHECK |
| app/src/main/java/com/vital/ui/components/Previews.kt | Only `AllComponentsPreview` (0 refs outside file); preview-only | Reference count 0 | Low | CHECK |
| app/src/main/java/com/vital/ui/screens/DebugShowcaseScreen.kt | Imported in VitalNavigation.kt:30 but **no composable route** (`grep -i debug VitalNavigation.kt` → only import) | `grep debug` → line 30 only | May be intended Step 2D debug screen — confirm | CHECK |
| Notes/fud-ai-android-v7.2.tar.gz (3.1 GB), Notes/openGym-1.4.3.tar.gz (203 MB), Notes/vital-ui.zip (4.7 MB) | Large source archives, gitignored, untracked; vital-ui/ already extracted | `ls -lh Notes/*.tar.gz Notes/vital-ui.zip` | User may want archives for re-extraction | CHECK |
| Notes/DialTicks.kt | **Keep** — regeneration master for DialTicks.kt (STEP2_PROMPT: "Copy it unchanged") | STEP2_PROMPT line 27 | — | **NO** |

**Totals: 10 CHECK items; 1 NO (keep).**

---

## 7. Proposed Change List (≤3 Batches)

### Batch 1 — Housekeeping (no build files, no code logic)
- Run `cleanup.sh` Group 1 (all YES deletions).
- Add to `.gitignore` (not frozen): `.kotlin/`, `window.xml`, `tools/`, `*.textClipping`, root `screen_*.png` `current_ui*.png` (already there but now untracked).
- **Verify**: `git status --short` clean of junk; `./gradlew assembleDebug` passes (colors.xml removal safe — 0 refs).
- **Approval**: One "go" for this batch.

### Batch 2 — Code Hygiene (no build files)
- **Color.kt**: Remove ~45 zero-ref tokens (AccentTDY…AccentWKS, ActivityAxisLine…ActivityTooltipText, BackgroundTrueBlack, backgroundTrueBlackOff, CapsuleFill, Card, chartBarOnGrey, DialDarkDisk…DialOuterRimHighlight, fabRimStroke, fabRimVisible, InsetGreyGridLines, InsetGreyLabels, InsetGreyPanel, InsetGreyPanelShadow, MacroProtein/Carbs/Fat, MutedText, navBottomHighlight, navGlow, navTopHighlight, NearBlackRow, NearBlackRowStroke, OnSurface, OnSurfaceVariant, Outline, PrimaryText, RefInkDeep, RefPillGray, RefWhite, sheetHighlight, Surface, SurfaceVariant, tickGrey, wellStrokeFade, getTabAccent, getTabAccentLabel). Keep only tokens with ≥1 ref.
- **strings.xml**: Remove ~120 unused strings (all except `app_name` used in manifest; widget strings; old UI strings not in new screens).
- **Fonts.kt**: Delete SpaceGrotesk* lines (18–22); switch JetBrainsMono* to `R.font.jetbrains_mono_*` (matching TodayScreen/Components); delete duplicate jetbrainsmono_* font files (Group 2 CHECK).
- **Type.kt**: Remove placeholder comments (lines 12–13); implement real FontFamilies.
- **Theme.kt**: Wire `isTrueBlackEnabled()` to Settings (or remove if not used yet; currently 0 refs outside Theme.kt).
- **DebugShowcaseScreen.kt**: Delete file + import in VitalNavigation.kt (or add route if Step 2D intended).
- **Previews.kt**: Delete (preview-only).
- **Verify**: `grep -rn "VitalColors\.(dead token)" app/src` → 0; `./gradlew assembleDebug` passes.
- **Approval**: One "go" for this batch.

### Batch 3 — Build Files (requires "unfreeze" from user)
- **app/build.gradle.kts**: Replace all ~20 literal versions with catalog refs (e.g. `libs.androidx-core-ktx`, `libs.androidx-lifecycle-runtime-ktx`, `libs.androidx-activity-compose`, `libs.androidx-constraintlayout-compose`, `libs.material`, `libs.hilt-android`, `libs.androidx-hilt-work`, `libs.androidx-hilt-navigation-compose`, `libs.androidx-work-runtime-ktx`, `libs.androidx-biometric` (downgrade to stable 1.1.0), `libs.okhttp`, `libs.kotlinx-serialization-json`, `libs.kotlinx-coroutines-android`, `libs.kotlinx-datetime`, `libs.coil-compose`, `libs.mlkit-text-recognition`, `libs.junit`, `libs.junit-jupiter`, `libs.truth`, `libs.robolectric`, `libs.turbine`, `libs.androidx-test-core`, `libs.androidx-test-rules`, `libs.androidx-test-ext-junit`, `libs.espresso-core`). Add missing catalog entry for `hilt-lifecycle-viewmodel` (1.4.0 stable) and use it.
- **app/build.gradle.kts:105**: Remove `ksp(libs.androidx.security.crypto)` — wrong.
- **settings.gradle.kts**: Remove entire `resolutionStrategy { eachPlugin … }` block (duplicate pin; root plugins block already pins hilt 2.60.1).
- **gradle-wrapper.properties**: Update `distributionUrl` to `gradle-9.8.2-bin.zip` (matches system & CLAUDE.md).
- **gradle.properties**: Delete stale commented block (lines 24–39); consider removing `android.enableArtifactTransforms=true`.
- **Optional version bumps** (each with verified URL, one per sub-step with "go"):
  - `core-ktx` 1.15.0 → 1.19.1 (Google Maven `release=1.19.1`)
  - `lifecycle` 2.8.7 → 2.11.0 (Google Maven stable `2.11.0`)
  - `biometric` 1.2.0-alpha04 → 1.1.0 (stable)
  - `material` 1.12.0 → 1.14.0 (Google Maven `release=1.14.0`)
  - `mlkit-text-recognition` 16.0.0 → 16.0.1
  - `activity-compose` 1.9.3 → 1.13.0 (stable)
  - `work-runtime-ktx` 2.9.0 → 2.12.0
  - `junit-jupiter` 5.11.0 → 6.1.3
  - `robolectric` 4.13 → 4.17
  - `turbine` 1.0.0 → 1.2.1
  - **AGP / Kotlin / KSP / Hilt** — **DO NOT CHANGE** (rule: frozen; AGP 9.5.0, Kotlin 2.2.10 built-in, KSP 2.2.10-2.0.2, Hilt 2.60.1).
- **Verify**: `./gradlew assembleDebug` passes; `compileDebugKotlin` runs; app installs and launches (0 AndroidRuntime:E).
- **Approval**: One "go" per sub-step (catalog hygiene first, then optional bumps individually).

---

## 8. Questions for the User

1. **Notes/docs pixel references missing** — CLAUDE.md, SPEC.md, STEP2_PROMPT all treat `Notes/docs/Summary.png` and `Notes/docs/Activity.png` as the **only** pixel references for Step 2 acceptance. The directory does not exist on disk (Notes/ is gitignored). Can you restore these two PNGs? Without them, Step 2 numeric comparison is impossible.

2. **Auth gate** — SPEC §1, §7, Step 1 acceptance all require PIN/biometric auth screen (`AuthActivity`, `BiometricPrompt`, `USE_BIOMETRIC` permission). Grep finds **zero** references in code; manifest lacks the permissions. Was the auth screen deliberately removed, or is it pending?

3. **Keep 3.3 GB Notes/*.tar.gz archives?** (`fud-ai-android-v7.2.tar.gz`, `openGym-1.4.3.tar.gz`, `vital-ui.zip`). They are untracked, gitignored. `vital-ui/` is already extracted. DialTicks.kt (regeneration master) is kept separately.

4. **Widgets stay removed?** (WIDGETS strings in strings.xml, widget_background in colors.xml, commented block in SettingsScreen). If yes, Group 1 deletion of colors.xml + strings cleanup in Batch 2 is safe. If widgets might return, keep strings but remove colors.xml.

5. **Space Grotesk deletion OK?** SPEC says dropped; 5 font files are byte-identical duplicates. Requires paired Fonts.kt/Type.kt edits (Batch 2). Confirm proceed.

---

**End of Audit.**  
Files written: `/Users/mrityunjaychouhan/Vital/tmp/AUDIT.md`, `/Users/mrityunjaychouhan/Vital/tmp/cleanup.sh`.