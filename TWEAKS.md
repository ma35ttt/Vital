# TWEAKS — parking lot

New ideas and rough edges go here, not into a build step. They are reviewed when the
daily-use milestone (M5) is reached, batched, and anything that changes a screen is
designed in Claude chat before it is built.

## How to log (2–3 days of real use)

Use the app normally: log real meals, sets, cardio and body weight. When something feels
wrong, slow, ugly, missing or surprising, add one line under **During daily use** below.

Format (keep it short — a line is enough):

```
- [date] [screen] what happened / what you expected. (severity: blocker | annoying | nice-to-have)
```

Examples:

```
- 11.06 Meals: had to tap the grams field twice to get a keyboard. (annoying)
- 11.06 Lift: no way to fix a typo'd weight, only delete the set. (annoying)
- 11.07 Today: wanted yesterday's numbers, no day picker. (nice-to-have)
```

Screenshot anything visual and drop it in `tmp/` with a matching name.

## Feature decisions (2026-11-05)

- **Dropped for now, revisit after M5 testing:** Cloud sync / backup, Wear OS companion,
  home-screen widgets (Glance). Not building.
- **AI providers stay REST** (OkHttp), not vendor SDKs. Keeps the app small and means adding
  providers is a code change, not a dependency change.
- **No Wear OS companion** — use the standard notification (foreground service) instead, which
  mirrors the rest timer to essentially any watch.
- **Wanted candidate:** "Snap watch stats → prefill cardio" (photo the watch, OCR the numbers,
  confirm and save). Works with any watch brand; shares one OCR pipeline with the planned
  treadmill and food scans. Also the "comprehensive exercise library" and multi-provider AI
  coach are confirmed-wanted.

## During daily use

- 05.11 Live island: closing the app makes the rest island vanish; it should stay visible at the
  top of the phone outside the app and keep counting. (annoying) — known gap, no foreground
  service or notification yet; M6 has "rest timer foreground service".

## Carried in from M4 (known follow-ups, not new)

These were already deferred while building; they are candidates for the M5 batch, not
surprises.

- **Date pickers.** Meals, Lift, Cardio, Today, Summary and Weeks are all today-only (or
  "trailing window" only). Back-logging or editing a past day is not possible. Design first.
- **Light theme coverage.** Progress, AI coach and Debug showcase are the old dark-only
  designs. Porcelain covers the five tabs plus Summary, Weeks and Settings.
- **Accent colour.** `Settings.themeColor` is stored but unused; there is no independent
  accent picker and no auto/system light-dark mode.
- **Body weight / rest preset** are shown in Settings but not editable there.
- **Export / Import JSON** buttons are drawn but unwired; PIN and biometric are read-only
  until the M6 auth gate.
- **More dot** only watches meal and workout reminders, not the daily or water reminders.
- **Live island** only tracks the rest timer (not cardio or water) and does not survive the
  app being backgrounded or killed (no foreground service yet). The 30 s auto-expand is
  code-only and has not been watched end to end.
- **Lift** has no per-set edit (delete only), no warm-up/RPE controls and no plate-inventory
  setting.
- **Meals** WEB and RECIPE are labels only; the query is not cleared after a smart-text add.
- **Cardio** has no edit, only soft delete; `SCAN TREADMILL` / OCR is M7.
- **Weeks** bars have no tooltip and trends do not tap through to a detail.
- **Today** week chart has no scrub/tooltip and no week/month switch.
- **Summary** ring has no body-weight marker; More's quick-add jumps to a tab rather than
  opening that tab's add flow directly.
- **Small-text ink bias** (≤ 3 du, downward) is the known app-wide residual from the Settings
  measurement pass; not chased.
- **Untested areas** from earlier notes: exact notification timing on Android 14+, OCR and AI
  accuracy, biometric/PIN (not built), keyboard-open states, rotation on every tab.

## Review log

_(date each review; list what was batched, designed or dropped)_
