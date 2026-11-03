#!/bin/sh
# DO NOT RUN until the user approves each group.
# Read-only audit output (2026-11-03). Run from the repo root: /Users/mrityunjaychouhan/Vital
# Group 1 = YES (safe deletions, verified by grep/shasum in tmp/AUDIT.md section 6).
# Group 2 = CHECK (needs a code edit or user confirmation first; all lines commented out).

# ================= GROUP 1: YES =================
# --- Junk tracked files (git rm) ---
git rm -q window.xml                       # uiautomator dump pasted at repo root
git rm -q "tools/Wroking build.textClipping" # macOS TextClipping; contains literal "git checkout 9ac3a0a -- ." and "./gradlew installDebug"
git rm -q -r .kotlin                        # 5x errors-*.log + .DS_Store, all tracked junk
# tracked .DS_Store files (.gitignore lists .DS_Store but they were committed before/force-added)
git rm -q app/.DS_Store app/src/.DS_Store app/src/main/.DS_Store \
          assets/.DS_Store assets/icons/.DS_Store assets/icons/android/.DS_Store \
          gradle/.DS_Store
# old root screenshots (gitignore has screen_*.png / current_ui*.png but they are tracked)
git rm -q current_ui.png current_ui2.png screen_awake.png screen_fixed_today.png \
          screen_focused.png screen_pulled.png screen_raw.png \
          screen_summary.png screen_summary2.png screen_summary3.png screen_summary4.png
# colors.xml contains only widget_background (widgets removed 2026-10-31; 0 references)
git rm -q app/src/main/res/values/colors.xml

# --- Untracked junk (rm); all paths are gitignored already ---
rm -f tmp/*.png tmp/build.log tmp/errors.txt     # old device screenshots/logs (tmp/today1.png == today2.png, identical shasum)
rm -rf tmp/phone-shots                            # old adb screenshot dump
rm -f design/tabs/docs/.DS_Store && rmdir design/tabs/docs 2>/dev/null  # empty dir, nothing but .DS_Store

# ================= GROUP 2: CHECK (do NOT run yet) =================
# --- fonts: delete only together with the Fonts.kt / Type.kt edits in AUDIT.md batch 2 ---
# git rm -q app/src/main/res/font/spacegrotesk_{regular,medium,semibold,bold,light}.ttf
#   why: SPEC.md section 1 says "Space Grotesk is dropped"; all 5 files have the SAME shasum
#        (dc03dcd18b41fb1d43c192aaa8519b0a33cde8b6) — one file copied 5 times. But Fonts.kt:18-22
#        and Type.kt:13 still reference R.font.spacegrotesk_*, so deleting the files alone breaks the build.
# git rm -q app/src/main/res/font/jetbrainsmono_{regular,light,medium,semibold,bold}.ttf
#   why: duplicate JetBrains Mono set; jetbrains_mono_400/500/700.ttf are the ones used by
#        TodayScreen.kt:97-98 and Components.kt:173-174. Keeping both = 6 extra files for the same family.
#   risk: Fonts.kt:11-15 must be switched to R.font.jetbrains_mono_* first.

# --- dead debug screen: imported in VitalNavigation.kt:30 but NO composable route (grep "debug" = only the import) ---
# git rm -q app/src/main/java/com/vital/ui/screens/DebugShowcaseScreen.kt
#   + remove the import at VitalNavigation.kt:30. May be the intentional Step 2D debug screen — confirm keep/route first.

# --- preview-only file (only AllComponentsPreview, 0 references outside Previews.kt) ---
# git rm -q app/src/main/java/com/vital/ui/components/Previews.kt

# --- 3.3 GB of source archives in gitignored Notes/ (untracked). Confirm the user does not need them ---
# rm -f Notes/fud-ai-android-v7.2.tar.gz Notes/openGym-1.4.3.tar.gz Notes/vital-ui.zip
#   vital-ui/ is already extracted on disk; Notes/DialTicks.kt (regeneration master for DialTicks.kt) is KEPT.
