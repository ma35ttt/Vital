# Changelog

All notable changes to Vital are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] — 2026-11-06

### Added

- In-app sideload updater: checks a release manifest, shows version and release notes, offers
  **Update now** or **Later**, downloads the APK, verifies its SHA-256 and opens the system
  installer. Settings → About includes a manual **Check for updates** entry.
- Android 16 Live Update chip for the rest timer, using the transparent nine-ring Vital mark.
- Rest timer notification actions (`+15s`, `Done`) and an orange large icon in the shade.
- Guided onboarding with metric/imperial units, body-goal plan and editable targets.
- Optional Google sign-in and Firestore sync, with tombstones and last-writer-wins conflicts.

### Changed

- Tutorial welcome page uses the same nine-ring mark as the launcher icon, at 64dp.
- Rest timer branding now matches the app icon across the status bar, shade and overlay.

### Fixed

- Rest drain stays in sync when the app is backgrounded.
- Cloud cleanup and local reset no longer leave stale test data behind.

## 1.0.0 — 2026-10-31

### Added

- Local-first meals, lift, cardio, today, weeks and progress screens.
- Food search, recipes, smart-text entry and macro totals.
- Workout sessions, sets, plate calculator, estimated 1RM and PR detection.
- ACSM cardio estimate and treadmill-console photo scan.
- Body metrics, progress photos and trend charts.
- Multiple editorial themes, including true black.
- Reminder workers and a foreground rest timer.

[Unreleased]: https://github.com/ma35ttt/Vital/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/ma35ttt/Vital/releases/tag/v1.1.0
