# In-app updates (sideload channel)

Vital is distributed outside the Play Store. Android does not allow an app to replace itself
silently, so the updater does the next best thing:

1. Check a small release manifest.
2. Show the new version and its release notes.
3. Download and verify the APK.
4. Hand it to the system installer, which shows the final **Update** confirmation.

The user's data is preserved. The only one-time setup is allowing **Install unknown apps →
Vital** in system settings. The app deep-links to that screen when needed.

## How it behaves

- **On launch** — checks at most once every six hours, quietly. A failure never interrupts
  the app.
- **Update available** — a dialog shows `versionName`, the current version and `notes[]`,
  with **Update now** and **Later**.
- **Later** — stores that `versionCode`. The same release stays quiet on future launches;
  a newer release will prompt again.
- **Settings → About → Check for updates** — always checks, bypassing both the throttle and
  the dismissal. It reports "Up to date", "Update available" or a readable error.
- **Downloading** — shows progress. The APK goes to `cache/updates/` and is deleted if the
  checksum fails.
- **Installing** — checks `canRequestPackageInstalls()`. If permission is missing, it opens
  the system setting and resumes automatically when the user returns.
- **Verification** — the SHA-256 in the manifest must match the downloaded file before the
  installer is opened.

## Manifest schema

`latest.json` is served from a stable URL. With GitHub Releases, the recommended URL is:

```
https://github.com/OWNER/REPO/releases/latest/download/latest.json
```

The release workflow uploads `latest.json` as an asset of every release, so the
`releases/latest/download/...` URL always points at the newest one.

> The repository must be **public**. GitHub requires authentication for release assets on a
> private repository, and the app cannot present a token. If the source must stay private,
> host the manifest and APK at a public URL instead.

```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "apkUrl": "https://github.com/OWNER/REPO/releases/download/v1.1.0/Vital-1.1.0.apk",
  "sha256": "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
  "notes": [
    "In-app updates with release notes and a manual check.",
    "Android 16 Live Update chip for the rest timer.",
    "Tutorial welcome mark enlarged to 64dp."
  ],
  "publishedAt": "2026-11-06"
}
```

| Field | Required | Notes |
| --- | --- | --- |
| `versionCode` | Yes | Integer. Must be greater than the installed build. |
| `versionName` | Yes | Shown in the dialog. |
| `apkUrl` | Yes | HTTPS in release builds. Debug builds also accept HTTP for local testing. |
| `sha256` | Yes | 64 hex characters. Verified after download. |
| `notes` | No | Array of short bullet strings. |
| `publishedAt` | No | ISO date, shown as metadata. |

Keep the schema additive. Older installed builds ignore unknown keys, so new fields can be
added without breaking the updater that is already on a friend's phone.

## Configuring the URL

The URL is compiled into the app through `BuildConfig.UPDATE_MANIFEST_URL`.

```properties
# local.properties (gitignored)
vital.updateManifestUrl=https://github.com/OWNER/REPO/releases/latest/download/latest.json
```

Or pass it at build time:

```bash
./gradlew assembleRelease \
  -Pvital.updateManifestUrl=https://github.com/OWNER/REPO/releases/latest/download/latest.json
```

The environment variable `VITAL_UPDATE_MANIFEST_URL` is also read. Priority is:
Gradle property → `local.properties` → environment variable → empty.

A blank value disables checks. Settings → About shows the version but the manual check
reports that updates are not configured.

## Signing rules

Every build that can update another must be signed with the **same key**.

- Create a release keystore before distributing the first build.
- Put its details in `keystore.properties` (gitignored) or in the repository secrets for CI.
- Back up the keystore and passwords somewhere safe.
- Never ship a debug-signed build to someone who will later receive a release-signed update.
  Android will reject the update and the only fix is uninstall + reinstall (data loss).

Create a keystore:

```bash
keytool -genkeypair -v \
  -keystore vital-release.jks \
  -alias vital \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then `keystore.properties`:

```properties
storeFile=/absolute/path/to/vital-release.jks
storePassword=…
keyAlias=vital
keyPassword=…
```

The Gradle build picks it up automatically. Without the file, release builds fall back to the
debug key so local builds remain installable — convenient for development, not for shipping.

## Release checklist

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Update `updates/release-notes.json`.
3. Run `./gradlew testDebugUnitTest assembleRelease`.
4. Commit the version bump.
5. Tag and push:

   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

6. The **Release** workflow builds the signed APK, computes its SHA-256 and publishes a
   GitHub Release with:
   - `Vital-1.1.0.apk`
   - `latest.json`
7. Install the new build on a test device over the previous one to confirm the signature and
   data preservation.
8. Ask the friend to open Vital. The dialog appears on the next launch, or they can use
   **Settings → About → Check for updates**.

### Manual release (no Actions)

```bash
./gradlew assembleRelease
APK=app/build/outputs/apk/release/app-release.apk
VERSION=1.1.0
cp "$APK" "Vital-$VERSION.apk"
shasum -a 256 "Vital-$VERSION.apk"
```

Create a GitHub Release for tag `v$VERSION`, upload the APK and a `latest.json` built from
the schema above. The `apkUrl` must point at the uploaded asset.

## Local test recipe

Use a debug build and a local HTTP server. Debug builds allow cleartext HTTP; release builds
require HTTPS.

```bash
# 1. Serve a manifest and a test APK from the Mac.
python3 -m http.server 8765 --directory /tmp/vital-update

# 2. Forward the device's localhost to the Mac.
adb reverse tcp:8765 tcp:8765

# 3. Build with the local manifest URL.
./gradlew assembleDebug -Pvital.updateManifestUrl=http://127.0.0.1:8765/latest.json
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The manifest's `apkUrl` should be `http://127.0.0.1:8765/Vital-test.apk`. Set `versionCode` to
something higher than the installed build, compute the real SHA-256 and open Vital. The dialog
should appear; **Update now** downloads, verifies and opens the installer.

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| No dialog on launch | Manifest URL blank, build already current, release dismissed, or inside the six-hour throttle. Use Settings → Check for updates. |
| "Couldn't check for updates" | URL unreachable, HTTP on a release build, malformed JSON, or a bad `sha256`. |
| "The download was incomplete" | SHA-256 mismatch. Re-upload the APK and update the manifest. |
| Installer says "App not installed" | Different signing key, lower `versionCode`, or a corrupted APK. |
| "Install unknown apps" keeps appearing | The user denied the permission. Open Settings → Apps → Vital → Install unknown apps. |
| Update installs but the app looks old | The APK's `versionCode` did not increase. Bump it in `app/build.gradle.kts`. |
