# Brief: fix the tab UI, then add hold-menu, snap flow, swipe tabs and speed

Read `CLAUDE.md`, `ROADMAP.md`, `REMAINING_TABS_BRIEF.md` (section 0A overrides), `design/tabs/chrome.md` and this file. Do not read `Notes/old`.
You can view images (Read tool): use them to list visible differences, but build from the numbers. Never write "matches"; write `NOT TESTED: visual comparison` and stop.
The user takes device screenshots by hand (adb or phone) and sends them; if you cannot capture one, write `NOT TESTED: screenshot (taken by user)` and carry on.
Where this file's F-steps meet the roadmap: F1 = M1, F2 to F4 = M2, F5 to F10 = M4, F11 to F13 and F15 = M6, F14 and part of F15 = M7. ROADMAP.md decides the order.

New design previews (human review only, numbers are in this file): `design/tabs/quickhold.png`, `snap-treadmill.png`, `snap-meal.png`.

## 0. Hard rules (same as the chrome steps)
- Edit only the files a step names. Never touch SummaryDial.kt, SummaryDialScreen.kt, TodayScreen.kt, Graphs.kt, DialTicks.kt or auth (until M6) unless the step names them.
  Build files and the manifest only inside an approved ROADMAP M0 batch (AGP/Kotlin/KSP/Hilt versions always frozen). If a step needs another build-file or manifest change (CameraX, FileProvider, baseline profile), stop and ask.
- Sizes in du; text `(du * scale).dp.toSp()`; colours only from `ui/theme`; no `// TODO`, no stubs, no sample data inside components, no new dependencies.
- Fix only the first compile error, rebuild, repeat. Max 2 attempts per error, then show the full "What went wrong" block.
- No commit. Do not start the next step. End each step with: `git status --short`, the gradle-files diff (must be empty), the `Color(0x` grep (must be empty),
  `./gradlew assembleDebug`, `./gradlew --stop`, install, `adb logcat -d -s AndroidRuntime:E`.
- Unit conversion on the user's phone (OnePlus 11R): 1 du = 3.155 px at full resolution; the screen is 878.5 du tall, the mock frame is 855 du. Anchor the header and
  the sheet to the top, the nav to the bottom.

## 1. Defects measured from the device screenshots (894 px wide screenshots, 2.275 px per du)

Where a number is given it is measured, "mock" is the target from `design/tabs/*.md`.

### All tabs
1. Body starts too low. Meals/Cardio `SHOW MORE` top 190 du (mock 118); Weeks and Settings hero top 170 du (mock 98); Settings `SECURITY` label 189 (mock 104).
   That is +72 to +85 du, about the status-bar inset (44) plus something else. Likely the inset is applied twice or the scaffold adds a second top padding.
2. Hero label and value are wrong on the orange tabs: Meals shows `LUNCH` where `KCAL LEFT` belongs and no value; Lift shows `OF 3:00`; Cardio shows `—:—`.
   The dial top pill text is being drawn into the hero label slot, and `heroLabel` / `heroValue` are not reaching the scaffold. The grey top pill on the dial is empty.
3. The dial is the Summary dial at Summary size, not `OrangeDial`: knob outer diameter about 145 du (mock 78), disc radius about 277 du (mock 152). The tick ring, 13-line fan,
   letter circles, inner disc text and orange pill are missing; the Summary glyph fan (footprints, walker, bike) appears instead. The FAB is not drawn on any tab.
4. Text is clipped inside fixed-height boxes: Meals text field and `RICE 150G · RAW OR COOKED?` pill (text cut through the middle), Settings BODY WEIGHT / REST PRESET values, the Gemini / OpenRouter
   labels. Cause: fixed heights with default font padding and line height. Fix once in shared components: `includeFontPadding = false`, explicit `lineHeight`, `Alignment.CenterVertically`,
   and `heightIn(min = ...)` instead of `height(...)` wherever text sits inside.
5. Bottom nav: `+` centre x 184 du (mock 196.5), tab centres TDY 40 / MLS 88 / LFT 136 / CRD 232 / WKS 280 / SET 328 (mock 43 / 93 / 144 / 249 / 299 / 349). Items are packed to the left with a fixed 48 du pitch
   and 30 du of dead space on the right. Fix: row of three equal-weight tab slots, a fixed 54 du centre slot for `+`, three equal-weight tab slots (padding 6).
6. QUICK ADD opens as a panel at the TOP of the screen (title overlaps the status bar). It must be a bottom sheet.
7. TRUE BLACK is off by default (Weeks and Settings background is `#141414`; the toggle is off). Default must be ON (`#000`). With it off, the header backing must match the page colour (now black over grey = two tones).
8. Summary header chips show `SUN` / `APR.9` (hard-coded). Today shows the correct system date. Use the system date everywhere (`VitalChip` text from `LocalDate.now()`).
9. Row icons that are not in the mock were added in Settings (user, gear, key, flame, info). Remove them (the mock rows are text only). Other font issue: labels are drawn in the default sans, not JetBrains Mono / Inter Tight (fonts pass is still pending).

### Meals
- Dial: see 3. Sheet rows are the old Today capsules (heart icon, sparkline, 84 du high): build `MealSlotRow` (42 du, letter circle 30, `SLOT · hh:mm`, one-line summary, kcal right, dashed when empty). Four rows, pitch 50.
- The list scrolls up over the text field and pill. The method tiles, field and pill must stay fixed; only the slot list scrolls and it must clip at its own top edge.

### Lift
- Hero label/value (see 2). Remove the extra `PLATES` section label. The `PLATES · TARGET 80 KG · BAR 20 KG` header is a separate grey block: it belongs inside the one plate panel (x 26, y 570).
- Plate panel is missing the bar line, the collar and the big `25 + 5` result with `KG PER SIDE · EXACT`. The `SESSION · n SETS` row is missing (hidden under the nav).

### Cardio
- Type tiles have icons but no labels. The `SCAN TREADMILL` row has grown to about 700 px tall and pushed the 2 x 2 fields, `ACSM EST` note, `SAVE` and `RECENT` off screen: a flex weight sits on the wrong child. Hero value shows `—:—` in the wrong slot.

### Weeks
- Period switch shows `12W / 4W / 7D` (order and labels wrong, 12W active while the tag says 4 WEEKS). Required: `L M H` = 7 days / 4 weeks / 12 weeks, M active by default, placed top right of the hero on the tag row.
- Chart panel has collapsed to about 14 du with a black bar and a stray dot; no bars drawn. Panel is 122 du high with 12 bars (see `weeks.md`).
- `TRENDS` touches the hero (needs 22 du gap) and `SHOW MORE` sits on the next line (it sits right of the label, same row). Capsule rows are 62 du high with no gap (mock 68 and 8).
- BODY WEIGHT shows `0.39 kg` (looks like the delta, not the weight).

### Settings
- Hero: macro values and the share bar are missing (bar collapsed to a line), `2500` has no thousands separator, `KCAL` sits at the bottom right instead of next to the number. Hero height 228.
- Wells: the icon wells overlap the row border (see 9). `DISPLAY NAME` row shows the placeholder `User`: show the real name. Mock order in SECURITY: PIN LOCK (toggle), BIOMETRIC UNLOCK (toggle), CHANGE PIN (chevron).
  Now: BIOMETRIC first, PIN CODE with no toggle, CHANGE PIN with a gear on the left and a key on the right.
- `ALLOW NOTIFICATIONS`, `MEAL REMINDERS`, `WORKOUT`, `WATER` lack their second lines (`PERMISSION GRANTED`, `08:30 · 13:00 · 19:30`, `MON WED FRI · 18:00`, `EVERY 2 H`).
- `EXPORT JSON` is invisible (needs the orange fill; label black). The DATA note is drawn in a sans font, should be mono 10.

### Today (already known, still open)
- `0BMP` tooltip artefact with an orange outline; the F macro column is missing (columns are too wide); last capsule row is hidden under the nav (needs bottom padding of at least 100 du).

## 2. Glass nav numbers (replaces any earlier nav numbers)
Container x 12..381, y bottom 14, height 60. Slot centres: TDY 43, MLS 93, LFT 144, `+` 196.5, CRD 249, WKS 299, SET 349 (y 811 in the mock frame, bottom anchored on the device).
`+` is 48 du, tap target 56. Active pill: 48 x 46.

## 3. Hold-to-open menu on the `+` button (design: `quickhold.png`)

Tap (shorter than 250 ms) still opens the QUICK ADD bottom sheet. That stays for accessibility. Add TalkBack custom actions on `+`: LOG MEAL, LOG SET, LOG CARDIO, LOG BODY WEIGHT, SNAP PHOTO.

**Hold** (250 ms): haptic `LongPress`, the menu opens and follows the finger until release.
- Scrim black 66 % over everything except the nav. Fade 120 ms.
- Half disc: centre C = (196.5, 781) (the top edge of the nav), radius 184.5 du (diameter = nav width 369), so it spans x 12..381 and rises to y 596.5.
  Fill `#0B0B0B` plus a radial white 5 % to 17 % gradient, 1 du white 20 % rim, dashed tick arc at radius 170 (stroke 10, `tickGrey`, dash 1.3 / 5.7), thin ring at radius 100 (`#1E1E1E`).
  It grows out of the `+` button: scale from C with a 220 ms spring.
- Five nodes on radius 128 from C, at 162, 126, 90, 54 and 18 degrees (angle from the +x axis, counter-clockwise):
  | Node | Angle | Centre (du) | Radius | Action |
  |---|---|---|---|---|
  | MEAL | 162 | (74.8, 741.4) | 28 | go to Meals, focus the smart-text field |
  | LOG SET | 126 | (121.2, 677.4) | 28 | go to Lift, focus WEIGHT |
  | SNAP (apex) | 90 | (196.5, 653) | 30 | start the snap flow (section 4) |
  | CARDIO | 54 | (271.8, 677.4) | 28 | go to Cardio |
  | WEIGHT | 18 | (318.2, 741.4) | 28 | go to Settings body-weight field |
  Normal node: `#0A0A0A` fill, 1.5 `#2A2A2A` stroke, orange stroke icon (1.9 du). Apex node has a 2 du orange ring.
  Hovered node: orange fill, black icon, radius 34 (scalloped dashed black stroke, like the knob), light haptic tick (`SegmentTick` if available), the 36 degree wedge behind it filled orange 20 % with 1.5 du orange edge lines.
  Labels mono 700 9, `#F2F2F2`, 1 du tracking; bottom nodes put the label above the node (y 705) so the nav does not cover it; hovered label is orange.
- Above the disc: two chevrons at y 574 and 586 and `FLICK UP · SNAP` (mono 700 10, orange, tracking 2.2) at y 556.
- Title text at y 420: `Release on <NODE>` (Inter Tight 700 26) and `SLIDE TO PICK · RELEASE ELSEWHERE TO CANCEL` (mono 10 muted). Before a node is hovered the title reads `Slide to pick`.
- The `+` button while held: fill orange 28 %, 2 du orange ring, orange glow (blur 22).
- Hit testing: the node whose centre is within 40 du of the finger, else none.
- Release: hovered node fires; no node = cancel. 120 ms retract.
- **Flick up = camera:** while held, if the finger has moved at least 110 du upward from the press point with upward speed of at least 1,400 du/s (use `VelocityTracker`), open the snap flow at once, whatever node is under the finger. Releasing on the apex node also starts it.
- Implementation: one `pointerInput` on the `+` button with `awaitEachGesture` (down, wait up to 250 ms, then track moves). The overlay is a visual-only composable at the top of the root `Box` (no pointer input) so the same pointer keeps driving it.
- Disable the tab pager (`userScrollEnabled = false`) and the scroll of the page below while the menu is open.
- Respect the system animator duration scale: if it is 0, no animation.
- Files: `GlassBottomNav.kt`, new `HoldMenuOverlay.kt`, `VitalNavigation.kt` (host and routing). Previews with parameters only.

## 4. Snap flow (photo to log) (design: `snap-treadmill.png`, `snap-meal.png`)

1. Camera: use the system camera with `ActivityResultContracts.TakePicture`. The manifest is frozen, so do NOT use FileProvider: create the output URI with `MediaStore.Images.Media.EXTERNAL_CONTENT_URI`
   insert (no permission needed for your own entries on API 29+), and delete that entry once the result is saved or discarded. The manifest already declares CAMERA, which means the camera intent
   fails unless the permission is granted: request the CAMERA permission first, and explain why in one line if it is denied. An in-app live viewfinder needs CameraX (a dependency): not in this step, ask first.
2. Read: downscale to at most 1,600 px on the long edge, then ML Kit text recognition (bundled model, on-device).
3. Classify as TREADMILL if at least two of these are found: speed (`km/h`, `kph`, `mph`), incline (`%`), time `m:ss` or `h:mm:ss`, distance (`km`), calories (`kcal`, `cal`). Otherwise treat it as FOOD.
4. TREADMILL: open the Snap result screen (`snap-treadmill.png`) with the parsed values in editable fields; calories from the display if read, else ACSM (`ACSM EST`, valid up to 6 km/h). `SAVE CARDIO` writes a `CardioSession`.
   `NOT A TREADMILL?` switches to FOOD.
5. FOOD: if a network and an AI key exist, send the photo to the existing photo-analysis service (SPEC section 6) and show `snap-meal.png`: items with grams and kcal, total and macros, `AI ESTIMATE`, `FOOD · n% SURE`.
   If confidence is below 70 %, offline, or no key: show the manual Meals flow with the photo attached (SPEC: fallback to manual entry). `LOG TO <slot>` writes `MealLogEntry` rows with snapshotted macros; the slot defaults from the time of day.
6. Photos are not kept unless the user taps keep. Never claim an estimate is exact; keep the line `PHOTO ESTIMATES ARE ROUGH. CHECK THE GRAMS BEFORE YOU LOG.`
7. Layout numbers: photo panel 12 98 369 236 (radius 18, `panelGrey`), tag pill row y 350 (height 30), fields from y 396 (48 high, 8 gap), bottom buttons 52 high at 34 du from the bottom, outline left, orange right.

## 5. Swipe between tabs
- Replace the tab content host with a `HorizontalPager` of six pages (TDY MLS LFT CRD WKS SET), `beyondViewportPageCount = 0`. `pagerState.currentPage` drives the nav highlight and tapping a tab calls `animateScrollToPage`.
  Route names for Summary, Coach, Progress and auth stay in the NavHost. Keep tab state across rotation. Back from any tab other than TDY goes to TDY first.
- Disable pager scrolling while the hold menu is open, while a text field is focused with the keyboard up, and on screens with horizontal drags (none right now). Test the system back-gesture edge on the device.
- Tell me before you change `VitalNavigation.kt` structure: show the current NavHost layout and the planned one, and wait for "go".
- A pager composes the neighbour page while you drag, so the dial must be cheap first (section 6).

## 6. Speed (the app is laggy)
Measure first, change nothing in this step:
```
adb shell dumpsys gfxinfo com.vital reset
# use the app for 20 seconds: swipe tabs, scroll Today and Meals
adb shell dumpsys gfxinfo com.vital | grep -E "Total frames|Janky|50th|90th|95th|99th"
adb logcat -d | grep -E "Skipped [0-9]+ frames|Choreographer"
```
Report the numbers. Then fix in this order, re-measuring after each, and keep only what helps:
1. Static dial layers (disc, tick tracks, fan, rings) are drawn once and cached: `Modifier.drawWithCache` plus a `GraphicsLayer` (`rememberGraphicsLayer`, available in Compose 1.7) or an `ImageBitmap` built on size change, then one `drawImage` per frame. The `DialTicks` data must not be iterated per frame.
2. No allocation in draw lambdas: hoist `Paint`, `Path`, `Brush`, `TextMeasurer` results into `remember`. `Modifier.insetShadow` uses `BlurMaskFilter`: cache the shadow as a bitmap per size.
3. Recomposition: read state as low as possible (pass lambdas for fast-changing values), mark chart inputs `@Immutable`/`@Stable` (a `List<Float>` parameter is unstable), use `key` in lazy lists, `collectAsStateWithLifecycle`, `derivedStateOf` for derived values. No work in composition.
4. Move seed loading, Room queries mapping and JSON work off the main thread.
5. A debug build is much slower than a release build because Compose in a debuggable app runs without optimisation. If 1 to 4 are not enough, propose a `release`-like build type (not debuggable, signed with the debug key, minify off) and a baseline profile. That needs a build file change: stop and ask.
Done means: Janky frames under 5 % and 95th percentile under 16 ms while scrolling Today and swiping tabs on the OnePlus 11R. The user confirms by feel.

## 7. Small quality-of-life items (do after the above, one per request)
1. Haptics: tab change, toggle, stepper, knob ticks, rest timer end (vibration pattern).
2. Undo snackbar after logging or deleting a meal, set or session.
3. Swipe a meal row or set left to delete (with undo), long-press for duplicate or move to another slot.
4. Meals: `RECENT` chips above search (most used foods, ranked by frequency) and `REPEAT YESTERDAY'S <slot>`, so a common meal is two taps.
5. Lift: steppers with long-press auto-repeat and quick `±2.5 kg`, tap `LAST 80 × 8` to copy it, keep the screen on while a session is active, `+30 s / -30 s` actions on the rest-timer notification.
6. Numeric fields use the number keypad and select all on focus.
7. Double-tap the active tab to scroll to top.
8. Water: a `+250 ml` action on the water reminder notification (widgets stay removed).
9. App lock: no PIN prompt again for 30 s after leaving the app; option to hide content in the recents screen.
10. PR moment: when a set beats the stored best, a light haptic and an orange flash on the `BENCH PR` row.

## 8. Order of work (stop after each and wait for "go")
F1 measure speed (section 6, no code). F2 scaffold top offset, hero label and value wiring, text-clip pattern, TRUE BLACK default, system-date chips (section 1: all tabs 1, 2, 4, 7, 8).
F3 nav (section 2) and QUICK ADD bottom sheet. F4 `OrangeDial` geometry, tick ring, letters, inner disc, FAB (section 1: all tabs 3). F5 Cardio. F6 Lift. F7 Meals. F8 Weeks. F9 Settings (including row icons removal).
F10 Today leftovers. F11 speed fixes (section 6, 1 to 4). F12 hold menu. F13 swipe tabs. F14 snap flow. F15 quality-of-life items.

## Paste-ready prompt for F1
> Read CLAUDE.md and FIXES_AND_GESTURES_BRIEF.md section 6. Do not read Notes/old. Manual mode. Step F1 only: do not change any file. Run the measurement commands in section 6 on the connected device
> while I use the app (tell me when to start and stop), and report the numbers. Then stop and wait for "go".
