# Architecture

Vital is a single-module Android app with a small, deliberate set of layers. The goal is
simple: every screen reads and writes local data, and the network is an optional background
concern.

## Layers

```
ui/          Compose screens, components, theme, navigation
  ↓ state / events
ViewModel    Hilt-injected, exposes StateFlow, survives configuration changes
  ↓
domain/      Pure calculations: nutrition, plate math, ACSM, adherence, 1RM, PRs
data/        Repositories, Room entities/DAOs, sync engine, updater, preferences
  ↓
Room         SQLite, the source of truth
```

Supporting pieces:

- `service/` — the rest-timer foreground service, the floating chip and the boot receiver.
- `workers/` — WorkManager jobs for meal, workout and water reminders.
- `auth/` — Google sign-in through Credential Manager and the account state machine.
- `ai/` — Gemini and OpenRouter clients. Optional; every feature has a manual fallback.

## Local-first write path

1. A screen calls a ViewModel method.
2. The ViewModel calls a repository.
3. The repository writes to Room and returns immediately.
4. The screen observes a `Flow` from Room, so the new value appears without a refresh.
5. If the user is signed in, the repository marks the row dirty (`updatedAt`) and the sync
   engine picks it up in the background.

There is no loading spinner on the critical path for local data. A network failure can make
sync show an error, but it cannot block logging a meal or a set.

## Data model

Every syncable row carries:

| Column | Purpose |
| --- | --- |
| `id` | UUID, stable across devices |
| `updatedAt` | Millisecond timestamp, used for last-writer-wins |
| `deleted` | Tombstone so a delete propagates instead of being resurrected |

The synced tables are `meal_log_entries`, `workout_sessions`, `workout_sets`,
`cardio_sessions`, `body_metrics`, `water_entries`, `recipes`, user-created `foods` and
`exercises`, `users` and `settings`.

Deliberately local-only:

- Progress photos (large binaries, not part of the text sync).
- Seeded catalogs (re-created on every install).
- `User.pinHash` and `Settings.aiApiKeyEncrypted` (device credentials).

## Sync

Firestore stores each row under `users/{uid}/{table}/{rowId}` as `{ json, updatedAt, deleted }`.
The security rules only allow a signed-in user to touch their own subtree.

The engine:

1. Pushes rows whose `updatedAt` is newer than the last successful push.
2. Pulls remote rows changed since the last pull.
3. Compares timestamps and applies last-writer-wins.
4. Writes a `SyncMeta` row with the last push/pull times and the last error.

On a device's first sync, the cloud copy of the profile and settings wins, so signing in on a
new phone restores real data instead of overwriting it with fresh defaults. Deletes travel as
tombstones and are not resurrected by a later pull.

Sync runs on sign-in, when the app comes to the foreground (throttled to five minutes) and
from **Settings → Account → Sync now**.

## Rest timer and Live Updates

The rest timer is a foreground service, not an activity-bound coroutine. That keeps the
countdown alive when the app is backgrounded and lets the bottom-nav drain, the floating chip
and the notification all read the same source.

On Android 16 the ongoing notification is promoted to a Live Update:

- `smallIcon` is `ic_vital_status`, an alpha-only vector of the nine-ring mark, so the system
  can tint it for the status-bar chip.
- `largeIcon` is the orange launcher foreground.
- `shortCriticalText` carries the countdown.
- The notification is `PROMOTED_ONGOING`, with `+15s` and `Done` actions.

The floating overlay is the fallback on devices or settings where promotion is unavailable.
It uses the same mark and countdown.

## Updater

The sideload updater is intentionally small:

1. Fetch `latest.json` from the URL baked into `BuildConfig`.
2. Compare `versionCode` with the installed build.
3. Show the version and `notes[]` in a Compose dialog.
4. On **Update now**, download the APK to `cache/updates`, verify its SHA-256 and hand it to
   the system installer through a `FileProvider`.
5. **Later** stores the dismissed version code so that release does not nag again. A manual
   check from Settings bypasses the dismissal and the throttle.

Android requires the user to confirm every install; no app can silently replace itself. The
same signing key must be used for every release, or Android rejects the update.

See [UPDATES.md](UPDATES.md) for the manifest schema and release checklist.

## Security and privacy

- AI keys are encrypted with Android Keystore-backed `EncryptedSharedPreferences`.
- Exports and cloud sync strip the AI key and PIN hash.
- Firebase config is not committed; the build is green without it.
- The release keystore lives outside the repository and is backed up by the maintainer.
- The update manifest is fetched over HTTPS in release builds; APKs are checksum-verified
  before the installer sees them.

## Testing

Pure logic lives in `domain/` and `data/` helpers with no Android dependencies, so it runs as
fast JVM unit tests:

```bash
./gradlew testDebugUnitTest
```

The suite covers the nutrition parser, plate calculator, ACSM estimate, adherence windows,
estimated 1RM, PR detection and rest-timer state. UI is verified manually on a device;
curated screenshots live in `docs/screenshots/`.

## Directory map

```
app/src/main/java/com/vital/
  VitalApplication.kt      Hilt entry point and seed bootstrap
  ai/                      Gemini/OpenRouter clients
  auth/                    Credential Manager sign-in, account state
  data/
    dao/                   Room DAOs
    database/              VitalDatabase, migrations
    entity/                Room entities
    repository/            Repositories
    seed/                  Catalogs and first-run seeding
    sync/                  Sync engine, table mapping, cursors
    update/                Manifest, preferences, update manager
  di/                      Hilt modules
  domain/                  Pure calculations and plan logic
  service/                 Rest timer service, overlay, boot receiver
  ui/
    components/            Shared editorial components
    navigation/            App shell and routes
    screens/               One file per screen
    theme/                 Palettes, typography, theme
  workers/                 Reminder workers
```
